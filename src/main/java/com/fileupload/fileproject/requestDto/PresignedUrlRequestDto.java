package com.fileupload.fileproject.requestDto;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class PresignedUrlRequestDto {

   private String partNumber;
   private String uploadId;
   private String s3Key;
}
