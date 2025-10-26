package com.fileupload.fileproject.requestDto;


import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data
public class GoogleRequestDto {

    private String code;
    private String state;
    private String scope;
}
