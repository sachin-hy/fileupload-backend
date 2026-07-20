package com.fileupload.fileproject.requestDto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class GoogleRequestDto {

    private String code;
    private String state;
    private String scope;
}
