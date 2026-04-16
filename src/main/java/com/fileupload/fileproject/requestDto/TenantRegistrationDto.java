package com.fileupload.fileproject.requestDto;


import com.fileupload.fileproject.enums.PlanType;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data
public class TenantRegistrationDto {

    private String name;
    private String subdomain;
    private String organisationName;
    private String description;
    private String firstName;
    private String lastName;
    private String adminEmail;
    private String password;
    private String phone;
    private String jobTitle;
}
