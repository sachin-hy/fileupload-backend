package com.fileupload.fileproject.service;

import com.fileupload.fileproject.context.TenantContext;
import com.fileupload.fileproject.entity.FileMetadata;
import com.fileupload.fileproject.entity.FileShare;
import com.fileupload.fileproject.entity.Users;
import com.fileupload.fileproject.enums.Permission;
import com.fileupload.fileproject.enums.UploadStatus;
import com.fileupload.fileproject.repository.FileMetadataRepository;
import com.fileupload.fileproject.repository.FileShareRepository;
import com.fileupload.fileproject.repository.UsersRepository;
import com.fileupload.fileproject.responseDto.SharedFileDto;
import com.fileupload.fileproject.util.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FileShareService {

    private final FileMetadataRepository fileMetadataRepo;
    private final UsersRepository usersRepo;
    private final FileShareRepository fileShareRepo;


    @Transactional
    public void shareFile(Long fileId, List<Long> userIds)
    {
         Long currentTenantId= TenantContext.getTenantId();
         CustomUserDetails details = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
         Users currentUser = details.getUserEntity();


        FileMetadata file = fileMetadataRepo.findById(fileId)
                .filter(f -> f.getTenant().getTenantid().equals(currentTenantId))
                .filter(f -> f.getUploadStatus() == UploadStatus.COMPLETED)
                .orElseThrow(() -> new RuntimeException("File not found..."));

         if(userIds != null && !userIds.isEmpty())
         {
             List<Users> targets = usersRepo.findAllById(userIds);

             for(Users target : targets)
             {
                 if(!target.getTenant().getTenantid().equals(currentTenantId))
                 {
                     throw new RuntimeException("Secutrity Breach: Cannot share outside organisation ");

                 }

                 FileShare share = new FileShare();
                 share.setFile(file);
                 share.setSharedWith(target);
                 share.setSharedBy(currentUser);
                 share.setTenant(file.getTenant());
                 share.setPermission(Permission.DOWNLOAD);
                 fileShareRepo.save(share);

             }
         }
    }


    @Transactional
    public List<SharedFileDto> getFilesSharedWithMe()
    {
        Long currentTenantId = TenantContext.getTenantId();
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        Users currentUser = usersRepo.findByEmail(currentUserEmail).get();

        return fileShareRepo.findBySharedWithAndTenant_Tenantid(currentUser,currentTenantId).stream()
                .map((fileShare) -> new SharedFileDto(
                        fileShare.getId(),
                        fileShare.getFile().getId(),
                        fileShare.getFile().getOriginalFileName(),
                        fileShare.getSharedBy().getFullName(),
                        fileShare.getCreatedAt()
                 )).toList();


    }
}
