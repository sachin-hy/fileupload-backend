package com.fileupload.fileproject.requestDto;


import lombok.Data;

@Data
public class InvitationRegistrationRequestDto {

    String token;
    String fullName;
    String password;
}
