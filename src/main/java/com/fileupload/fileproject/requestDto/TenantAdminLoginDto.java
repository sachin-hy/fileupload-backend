package com.fileupload.fileproject.requestDto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantAdminLoginDto {

    private String email;

    private String password;
    private String subdomain;

}
