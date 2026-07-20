package com.fileupload.fileproject.requestDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class FileShareRequestDto {

    private Long fileId;
    List<Long> userIds;
}
