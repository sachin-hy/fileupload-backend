package com.fileupload.fileproject.service;

import com.fileupload.fileproject.context.TenantContext;
import com.fileupload.fileproject.entity.FileMetadata;
import com.fileupload.fileproject.entity.FileShare;
import com.fileupload.fileproject.entity.Tenant;
import com.fileupload.fileproject.entity.Users;
import com.fileupload.fileproject.enums.AuditAction;
import com.fileupload.fileproject.enums.Permission;
import com.fileupload.fileproject.enums.UploadStatus;
import com.fileupload.fileproject.repository.FileMetadataRepository;
import com.fileupload.fileproject.repository.FileShareRepository;
import com.fileupload.fileproject.repository.TenantRepository;
import com.fileupload.fileproject.repository.UsersRepository;
import com.fileupload.fileproject.requestDto.FileShareRequestDto;
import com.fileupload.fileproject.responseDto.RecentShareDto;
import com.fileupload.fileproject.responseDto.SharedFileDto;
import com.fileupload.fileproject.util.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileShareService {

    private final FileMetadataRepository fileMetadataRepo;
    private final UsersRepository usersRepo;
    private final FileShareRepository fileShareRepo;
    private final AuditLogService auditLogService;
    private final TenantRepository tenantRepo;


    @Transactional
    public void shareFile(FileShareRequestDto dto,   String ip)
    {
         Long fileId = dto.getFileId();

         // get the current userid to whom the file need to be shared
         List<Long> userIds = dto.getUserIds();


         Long currentTenantId= TenantContext.getTenantId();
         String email = SecurityContextHolder.getContext().getAuthentication().getName();
         //CustomUserDetails details = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
         // get the current user
         Users currentUser = usersRepo.findByEmailAndTenant_Tenantid(email,currentTenantId).orElseThrow(() -> new UsernameNotFoundException("username does not exist"));

         //get the tennt id
         Tenant tenant = tenantRepo.findById(currentTenantId).orElseThrow(() -> new EntityNotFoundException("Tenant not found with id: " + currentTenantId));


         FileMetadata fileMetadata = fileMetadataRepo.findByIdAndTenant_Tenantid(fileId,currentTenantId);


         if(userIds != null && !userIds.isEmpty())
         {
             //get all the users
             List<Users> targets = usersRepo.findAllByIdInAndTenant_Tenantid(userIds,currentTenantId);

             List<FileShare> shareToSave = new ArrayList<>();


             for(Users target : targets)
             {
                 FileShare share = new FileShare();
                 share.setFile(fileMetadata);
                 share.setSharedWith(target);
                 share.setSharedBy(currentUser);
                 share.setTenant(tenant);
                 share.setPermission(Permission.DOWNLOAD);
                 shareToSave.add(share);
             }

              // save all the sharefile to database
             fileShareRepo.saveAll(shareToSave);

             try {
                     auditLogService.log(currentTenantId, currentUser.getEmail(), AuditAction.FILE_SHARED, ip, "File ID: " + fileId + " shared with User" ,null);
                 }
             catch (Exception e) {
                     log.error("Failed to audit log FILE_SHARED: {}", e.getMessage());
                 }
         }
    }




    /*
    *
    * return the list of ShareFileDto with the user
    * */

    @Transactional
    public List<SharedFileDto> getFilesSharedWithMe()
    {
        Long currentTenantId = TenantContext.getTenantId();
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();


        Users currentUser = usersRepo.findByEmailAndTenant_Tenantid(currentUserEmail,currentTenantId).orElseThrow(() -> new UsernameNotFoundException("User Not found with email"));

        return fileShareRepo.findBySharedWithAndTenant_Tenantid(currentUser,currentTenantId).stream()
                .map((fileShare) -> new SharedFileDto(
                        fileShare.getFile().getId(),
                        fileShare.getFile().getOriginalFileName(),
                        fileShare.getFile().getContentType(),
                        fileShare.getSharedBy().getFullName(),
                        fileShare.getFile().getFileSize()
                 )).toList();


    }







    @Transactional
    public List<RecentShareDto> recentActivity(String email) {

        Long tenantId = TenantContext.getTenantId();

        List<RecentShareDto> result = fileShareRepo.findRecentActiveSharesByEmailAndTenant_Tenantid(email,tenantId);

        //System.out.println("result length " + result.size());
        return result;
//        return list.stream().map( (fileshare) ->
//            new RecentShareDto(
//                    fileshare.getId(),
//                    fileshare.getFile().getId(),
//                    fileshare.getFile().getOriginalFileName(),
//                    fileshare.getSharedBy().getFullName(),
//                    fileshare.getSharedBy().getEmail(),
//                    fileshare.getCreatedAt()
//            )
//        ).toList();
    }

    public List<RecentShareDto> getFilesShareInfo() {

        Long tenantId = TenantContext.getTenantId();

        return fileShareRepo.findShareOnOrganisation(tenantId);
    }
}
