package com.fileupload.fileproject.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.HeadBucketRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.PartETag;
import com.fileupload.fileproject.Exception.FileNotReadyException;
import com.fileupload.fileproject.context.TenantContext;
import com.fileupload.fileproject.entity.FileMetadata;
import com.fileupload.fileproject.entity.Tenant;
import com.fileupload.fileproject.entity.TenantUsage;
import com.fileupload.fileproject.entity.Users;
import com.fileupload.fileproject.enums.AuditAction;
import com.fileupload.fileproject.enums.PlanType;
import com.fileupload.fileproject.enums.UploadStatus;
import com.fileupload.fileproject.enums.UserRole;
import com.fileupload.fileproject.repository.FileMetadataRepository;
import com.fileupload.fileproject.repository.TenantRepository;
import com.fileupload.fileproject.repository.TenantUsageRepository;
import com.fileupload.fileproject.repository.UsersRepository;
import com.fileupload.fileproject.support.TestData;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FileServiceUnitTest {

    private static final long MB = 1024L * 1024L;
    private static final long GB = 1024L * MB;
    private static final String IP = "10.0.0.1";

    @Mock private TenantRepository tenantRepo;
    @Mock private TenantUsageRepository tenantUsageRepo;
    @Mock private AmazonS3 s3Client;
    @Mock private FileMetadataRepository fileMetadataRepo;
    @Mock private AuditLogService auditLogService;
    @Mock private UsersRepository usersRepo;

    @InjectMocks private FileService fileService;

    private Tenant tenant;
    private Users alice;
    private TenantUsage usage;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileService, "bucketName", "test-bucket");

        tenant = TestData.tenant(PlanType.FREE); // 10 GB quota
        alice = TestData.user(10L, TestData.USER_EMAIL, tenant, UserRole.MEMBER);
        usage = new TenantUsage();
        usage.setTenant(tenant);

        TenantContext.setContext(TestData.TENANT_ID, TestData.TENANT_KEY);
        TestData.authenticateAs(TestData.USER_EMAIL);

        when(usersRepo.findByEmailAndTenant_Tenantid(TestData.USER_EMAIL, 1L)).thenReturn(Optional.of(alice));
        when(tenantRepo.findById(1L)).thenReturn(Optional.of(tenant));
        when(tenantUsageRepo.findAndLockByTenantId(1L)).thenReturn(Optional.of(usage));
        when(tenantUsageRepo.findByTenant_Tenantid(1L)).thenReturn(usage);
        when(fileMetadataRepo.getSumOfFileSizesByTenantAndStatus(1L, UploadStatus.INITIATED)).thenReturn(0L);
    }

    @AfterEach
    void tearDown() {
        TestData.clearAllContexts();
    }

    private void s3ReturnsUploadId(String uploadId) {
        when(s3Client.initiateMultipartUpload(any(InitiateMultipartUploadRequest.class))).thenAnswer(inv -> {
            InitiateMultipartUploadResult result = new InitiateMultipartUploadResult();
            result.setUploadId(uploadId);
            return result;
        });
    }

    // =====================================================================
    // uploadId / checkTenantSizeLimit
    // =====================================================================

    @Test
    void uploadId_success_returnsUploadDetailsAndPersistsInitiatedFile() {
        s3ReturnsUploadId("upload-123");

        Map<String, Object> response = fileService.uploadId("report.pdf", String.valueOf(10 * MB), "application/pdf", IP);

        assertEquals("upload-123", response.get("uploadId"));
        assertEquals("report.pdf", response.get("fileName"));
        String s3Key = (String) response.get("s3Key");
        assertTrue(s3Key.startsWith(TestData.TENANT_KEY + "/"), "key must be namespaced by tenant: " + s3Key);
        assertTrue(s3Key.endsWith("_report.pdf"));
        assertEquals(5 * MB, response.get("chunkSize"));
        assertEquals(2, response.get("totalChunk"));

        ArgumentCaptor<FileMetadata> saved = ArgumentCaptor.forClass(FileMetadata.class);
        verify(fileMetadataRepo).save(saved.capture());
        FileMetadata file = saved.getValue();
        assertEquals(UploadStatus.INITIATED, file.getUploadStatus());
        assertEquals("upload-123", file.getUploadId());
        assertEquals(s3Key, file.getStorageKey());
        assertEquals("test-bucket", file.getBucketName());
        assertEquals(10 * MB, file.getFileSize());
        assertEquals("application/pdf", file.getContentType());
        assertSame(alice, file.getUploadedBy());
        assertSame(tenant, file.getTenant());

        verify(auditLogService).log(eq(1L), eq(TestData.USER_EMAIL), eq(AuditAction.UPLOAD_INITIATED),
                eq(IP), any(), any());
    }

    @Test
    void uploadId_partialLastChunk_roundsPartCountUp() {
        s3ReturnsUploadId("u");

        Map<String, Object> response = fileService.uploadId("a.bin", String.valueOf(12 * MB), "x/y", IP);

        assertEquals(5 * MB, response.get("chunkSize"));
        assertEquals(3, response.get("totalChunk")); // 12MB / 5MB = 2.4 -> 3
    }

    @Test
    void uploadId_tinyFile_stillUsesSingleChunk() {
        s3ReturnsUploadId("u");

        Map<String, Object> response = fileService.uploadId("tiny.txt", "1", "text/plain", IP);

        assertEquals(1, response.get("totalChunk"));
    }

    @Test
    void uploadId_veryLargeFile_neverExceedsS3PartLimit() {
        tenant.setStorageQuotaGB(PlanType.ENTERPRISE.getStorageQuotaGB()); // 500 GB
        s3ReturnsUploadId("u");

        Map<String, Object> response = fileService.uploadId("big.iso", String.valueOf(400 * GB), "x/y", IP);

        int parts = (Integer) response.get("totalChunk");
        long chunkSize = (Long) response.get("chunkSize");
        assertTrue(parts <= 10_000, "S3 allows at most 10,000 parts but got " + parts);
        assertTrue(chunkSize >= 5 * MB, "S3 minimum part size is 5 MB");
    }

    @Test
    void uploadId_unknownUser_throwsUsernameNotFound_andNeverTouchesS3() {
        when(usersRepo.findByEmailAndTenant_Tenantid(TestData.USER_EMAIL, 1L)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> fileService.uploadId("a.pdf", "100", "application/pdf", IP));

        verifyNoInteractions(s3Client);
        verify(fileMetadataRepo, never()).save(any());
    }

    @Test
    void uploadId_unknownTenant_throws() {
        when(tenantRepo.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fileService.uploadId("a.pdf", "100", "application/pdf", IP));

        assertEquals("No Tenant present", ex.getMessage());
        verifyNoInteractions(s3Client);
    }

    @Test
    void uploadId_missingTenantUsageRow_throws() {
        when(tenantUsageRepo.findAndLockByTenantId(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fileService.uploadId("a.pdf", "100", "application/pdf", IP));

        assertEquals("No TenantUsage present", ex.getMessage());
    }

    @Test
    void uploadId_fileLargerThanQuota_isRejectedBeforeAnyS3Call() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fileService.uploadId("huge.bin", String.valueOf(11 * GB), "x/y", IP));

        assertEquals("Storage Limit Exceeded", ex.getMessage());
        verifyNoInteractions(s3Client);
        verify(fileMetadataRepo, never()).save(any());
    }

    @Test
    void uploadId_fileExactlyFillingQuota_isAllowed() {
        s3ReturnsUploadId("u");

        assertDoesNotThrow(() -> fileService.uploadId("full.bin", String.valueOf(10 * GB), "x/y", IP));
    }

    @Test
    void uploadId_countsAlreadyUsedStorage() {
        usage.setUsedStorageBytes(8 * GB);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fileService.uploadId("a.bin", String.valueOf(3 * GB), "x/y", IP));

        assertEquals("Storage Limit Exceeded", ex.getMessage());
    }

    @Test
    void uploadId_countsInFlightUploadsFromOtherUsers() {
        when(fileMetadataRepo.getSumOfFileSizesByTenantAndStatus(1L, UploadStatus.INITIATED)).thenReturn(6 * GB);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fileService.uploadId("a.bin", String.valueOf(5 * GB), "x/y", IP));

        assertEquals("Storage Limit Exceeded", ex.getMessage());
    }

    @Test
    void uploadId_nonNumericSize_throwsNumberFormatException() {
        assertThrows(NumberFormatException.class,
                () -> fileService.uploadId("a.pdf", "abc", "application/pdf", IP));

        verifyNoInteractions(s3Client);
    }

    @Test
    void uploadId_s3BucketUnreachable_cleansUpAndThrowsInternalError() {
        AmazonServiceException s3Down = new AmazonServiceException("bad region");
        s3Down.setStatusCode(400);
        doThrow(s3Down).when(s3Client).headBucket(any(HeadBucketRequest.class));


        assertThrows(InternalError.class,
                () -> fileService.uploadId("a.pdf", "100", "application/pdf", IP));

        verify(fileMetadataRepo).delete(any(FileMetadata.class));
        verify(fileMetadataRepo, never()).save(any());
        verify(auditLogService).log(eq(1L), any(), eq(AuditAction.UPLOAD_FAILED), eq(IP), any(), any());
    }

    @Test
    void uploadId_s3InitiateFails_cleansUpAndThrowsInternalError() {
        when(s3Client.initiateMultipartUpload(any(InitiateMultipartUploadRequest.class)))
                .thenThrow(new AmazonServiceException("S3 exploded"));

        assertThrows(InternalError.class,
                () -> fileService.uploadId("a.pdf", "100", "application/pdf", IP));

        verify(fileMetadataRepo).delete(any(FileMetadata.class));
    }

    @Test
    void uploadId_auditLogFailure_doesNotBreakUpload() {
        s3ReturnsUploadId("u");
        doThrow(new RuntimeException("audit db down")).when(auditLogService)
                .log(any(), any(), eq(AuditAction.UPLOAD_INITIATED), any(), any(), any());

        Map<String, Object> response = assertDoesNotThrow(
                () -> fileService.uploadId("a.pdf", "100", "application/pdf", IP));

        assertEquals("u", response.get("uploadId"));
    }



    // =====================================================================
    // preSignedUrl
    // =====================================================================

    @Test
    void preSignedUrl_success_buildsPutRequestForPartAndRewritesInternalHost() throws Exception {
        URL internal = URI.create("http://minio:9000/test-bucket/TENANT_KEY_1/abc_report.pdf?X-Amz-Signature=sig").toURL();
        when(s3Client.generatePresignedUrl(any(GeneratePresignedUrlRequest.class))).thenReturn(internal);

        Map<String, Object> response = fileService.preSignedUrl("3", "upload-123", "TENANT_KEY_1/abc_report.pdf");

        assertEquals("http://localhost:9000/test-bucket/TENANT_KEY_1/abc_report.pdf?X-Amz-Signature=sig",
                response.get("url"));

        ArgumentCaptor<GeneratePresignedUrlRequest> captor = ArgumentCaptor.forClass(GeneratePresignedUrlRequest.class);
        verify(s3Client).generatePresignedUrl(captor.capture());
        GeneratePresignedUrlRequest request = captor.getValue();
        assertEquals("test-bucket", request.getBucketName());
        assertEquals("TENANT_KEY_1/abc_report.pdf", request.getKey());
        assertEquals(HttpMethod.PUT, request.getMethod());
        assertEquals("3", request.getRequestParameters().get("partNumber"));
        assertEquals("upload-123", request.getRequestParameters().get("uploadId"));
        long msUntilExpiry = request.getExpiration().getTime() - System.currentTimeMillis();
        assertTrue(msUntilExpiry > 59 * 60 * 1000 && msUntilExpiry <= 60 * 60 * 1000,
                "URL should be valid for about 1 hour, was " + msUntilExpiry + " ms");
    }

    @Test
    void preSignedUrl_keyBelongingToAnotherTenant_isDenied() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fileService.preSignedUrl("1", "u", "TENANT_KEY_2/secret.pdf"));

        assertEquals("Access Denied", ex.getMessage());
        verifyNoInteractions(s3Client);
    }

    @Test
    void preSignedUrl_tenantKeyPrefixWithoutSlash_isDenied() {
        // "TENANT_KEY_1" must not match "TENANT_KEY_10/..." (prefix-collision attack)
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fileService.preSignedUrl("1", "u", "TENANT_KEY_10/secret.pdf"));

        assertEquals("Access Denied", ex.getMessage());
    }

    @Test
    void preSignedUrl_s3Failure_isWrappedInGenericException() {
        when(s3Client.generatePresignedUrl(any(GeneratePresignedUrlRequest.class)))
                .thenThrow(new AmazonServiceException("boom"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fileService.preSignedUrl("1", "u", "TENANT_KEY_1/a.pdf"));

        assertEquals("SomeThing Went Wrong", ex.getMessage());
    }

    // =====================================================================
    // completeMultipartUpload
    // =====================================================================

    @Test
    void completeMultipartUpload_success_sortsPartsMarksCompleteAndUpdatesUsage() {
        FileMetadata file = TestData.file(99L, tenant, alice, UploadStatus.INITIATED, 5 * MB);
        when(fileMetadataRepo.findByStorageKeyAndTenant_Tenantid("TENANT_KEY_1/obj", 1L)).thenReturn(file);

        List<Map<String, Object>> etags = List.of(
                Map.of("partNumber", 2, "etag", "etag-2"),
                Map.of("partNumber", 3, "etag", "etag-3"),
                Map.of("partNumber", 1, "etag", "etag-1"));

        Map<String, Object> response = fileService.completeMultipartUpload(etags, "TENANT_KEY_1/obj", "upload-123", IP);

        assertEquals(99L, response.get("fileId"));

        ArgumentCaptor<CompleteMultipartUploadRequest> captor = ArgumentCaptor.forClass(CompleteMultipartUploadRequest.class);
        verify(s3Client).completeMultipartUpload(captor.capture());
        CompleteMultipartUploadRequest request = captor.getValue();
        assertEquals("test-bucket", request.getBucketName());
        assertEquals("TENANT_KEY_1/obj", request.getKey());
        assertEquals("upload-123", request.getUploadId());
        List<PartETag> parts = request.getPartETags();
        assertEquals(List.of(1, 2, 3), parts.stream().map(PartETag::getPartNumber).toList());
        assertEquals("etag-1", parts.get(0).getETag());

        assertEquals(UploadStatus.COMPLETED, file.getUploadStatus());
        assertEquals(5 * MB, usage.getUsedStorageBytes());
        assertEquals(1, usage.getFileCount());
        assertEquals(1L, usage.getTotalUploads());
        verify(fileMetadataRepo).save(file);
        verify(tenantUsageRepo).save(usage);
        verify(auditLogService).log(eq(1L), any(), eq(AuditAction.UPLOAD_COMPLETED), eq(IP), any(), any());
    }

    @Test
    void completeMultipartUpload_s3Failure_logsFailureAndDoesNotTouchUsage() {
        when(s3Client.completeMultipartUpload(any(CompleteMultipartUploadRequest.class)))
                .thenThrow(new AmazonServiceException("EntityTooSmall"));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                fileService.completeMultipartUpload(List.of(Map.of("partNumber", 1, "etag", "e")),
                        "TENANT_KEY_1/obj", "u", IP));

        assertEquals("SomeThing Went Wrong", ex.getMessage());
        verify(auditLogService).log(eq(1L), any(), eq(AuditAction.UPLOAD_FAILED), eq(IP), any(), any());
        verify(fileMetadataRepo, never()).save(any());
        verify(tenantUsageRepo, never()).save(any());
        assertEquals(0L, usage.getUsedStorageBytes());
    }

    @Test
    void completeMultipartUpload_malformedEtagPayload_isReportedAsFailure() {
        // partNumber sent as a String instead of an Integer -> ClassCastException internally
        List<Map<String, Object>> bad = List.of(Map.of("partNumber", "1", "etag", "e"));

        assertThrows(RuntimeException.class,
                () -> fileService.completeMultipartUpload(bad, "TENANT_KEY_1/obj", "u", IP));

        verifyNoInteractions(s3Client);
    }



    // =====================================================================
    // getTenantFileList
    // =====================================================================

    @Test
    void getTenantFileList_returnsOnlyCompletedFilesAsMaps() {
        FileMetadata file = TestData.file(5L, tenant, alice, UploadStatus.COMPLETED, 123L);
        when(fileMetadataRepo.findByTenant_TenantidAndUploadStatus(1L, UploadStatus.COMPLETED))
                .thenReturn(List.of(file));

        List<Map<String, Object>> result = fileService.getTenantFileList();

        assertEquals(1, result.size());
        Map<String, Object> row = result.get(0);
        assertEquals(5L, row.get("id"));
        assertEquals("report.pdf", row.get("name"));
        assertEquals(123L, row.get("size"));
        assertEquals("First10 Last10", row.get("uploadedBy"));
    }

    @Test
    void getTenantFileList_noFiles_returnsEmptyList() {
        when(fileMetadataRepo.findByTenant_TenantidAndUploadStatus(1L, UploadStatus.COMPLETED))
                .thenReturn(List.of());

        assertTrue(fileService.getTenantFileList().isEmpty());
    }

    // =====================================================================
    // downloadFile
    // =====================================================================

    @Test
    void downloadFile_success_returnsPresignedGetUrl_andIncrementsCounter() throws Exception {
        FileMetadata file = TestData.file(5L, tenant, alice, UploadStatus.COMPLETED, 2048L);
        file.setDownloadCount(0);
        when(fileMetadataRepo.findByIdAndTenant(5L, tenant)).thenReturn(Optional.of(file));
        URL url = URI.create("https://s3.example.com/test-bucket/key?sig=1").toURL();
        when(s3Client.generatePresignedUrl(any(GeneratePresignedUrlRequest.class))).thenReturn(url);

        Map<String, Object> response = fileService.downloadFile(5L, IP);

        assertEquals(url.toString(), response.get("downloadUrl"));
        assertEquals("report.pdf", response.get("fileName"));
        assertEquals(2048L, response.get("fileSize"));
        assertEquals(1, file.getDownloadCount());
        verify(fileMetadataRepo).save(file);
        verify(auditLogService).log(eq(1L), any(), eq(AuditAction.FILE_DOWNLOADED), eq(IP), any(), any());

        ArgumentCaptor<GeneratePresignedUrlRequest> captor = ArgumentCaptor.forClass(GeneratePresignedUrlRequest.class);
        verify(s3Client).generatePresignedUrl(captor.capture());
        assertEquals(HttpMethod.GET, captor.getValue().getMethod());
        assertEquals(file.getStorageKey(), captor.getValue().getKey());
    }

    @Test
    void downloadFile_fileNotInCallersTenant_throwsEntityNotFound() {
        when(fileMetadataRepo.findByIdAndTenant(404L, tenant)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> fileService.downloadFile(404L, IP));

        verifyNoInteractions(s3Client);
    }

    @Test
    void downloadFile_unknownTenant_throwsEntityNotFound() {
        when(tenantRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> fileService.downloadFile(5L, IP));
    }

    @Test
    void downloadFile_uploadStillInProgress_failsWithoutIssuingUrl() {
        FileMetadata file = TestData.file(5L, tenant, alice, UploadStatus.INITIATED, 2048L);
        when(fileMetadataRepo.findByIdAndTenant(5L, tenant)).thenReturn(Optional.of(file));

        assertThrows(RuntimeException.class, () -> fileService.downloadFile(5L, IP));

        verify(s3Client, never()).generatePresignedUrl(any(GeneratePresignedUrlRequest.class));
        verify(fileMetadataRepo, never()).save(any());
        verify(auditLogService).log(eq(1L), any(), eq(AuditAction.DOWNLOAD_FAILED), eq(IP), any(), any());
    }



    @Test
    void downloadFile_s3Failure_logsDownloadFailed() {
        FileMetadata file = TestData.file(5L, tenant, alice, UploadStatus.COMPLETED, 2048L);
        file.setDownloadCount(0);
        when(fileMetadataRepo.findByIdAndTenant(5L, tenant)).thenReturn(Optional.of(file));
        when(s3Client.generatePresignedUrl(any(GeneratePresignedUrlRequest.class)))
                .thenThrow(new AmazonServiceException("boom"));

        assertThrows(RuntimeException.class, () -> fileService.downloadFile(5L, IP));

        assertEquals(0, file.getDownloadCount());
        verify(auditLogService).log(eq(1L), any(), eq(AuditAction.DOWNLOAD_FAILED), eq(IP), any(), any());
    }
}
