package com.fileupload.fileproject.requestDto;


import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data
public class PresignedUrlRequestDto {

   private String partNumber;
   private String uploadId;
   private String s3Key;
}
