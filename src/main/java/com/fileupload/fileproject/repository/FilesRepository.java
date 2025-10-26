package com.fileupload.fileproject.repository;

import com.fileupload.fileproject.entity.Files;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FilesRepository extends JpaRepository<Files, Long> {
    Files findByFileId(String s3Key);
}
