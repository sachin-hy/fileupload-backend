package com.fileupload.fileproject.requestDto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class FileRequestDto {

    private String fileName;
    private String fileSize;
    private String fileType;

}
