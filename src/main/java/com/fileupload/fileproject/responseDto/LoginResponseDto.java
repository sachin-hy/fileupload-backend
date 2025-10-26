package com.fileupload.fileproject.responseDto;


import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginResponseDto {

    private String token;
    private String username;
}
