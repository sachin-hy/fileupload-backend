package com.fileupload.fileproject.entity;


import com.fileupload.fileproject.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Setter
@Getter
@Table(
        name = "files",
        indexes = {
                @Index(name = "idx_file_id", columnList = "fileId")
        }
)
public class Files {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true, nullable = false)
    private String fileId;
    private String fileName;
    private String fileSize;
    private String fileType;
    private Date uploaded_at;
    private Date expires_at;
    private int download_count;
    private Status status;


}
