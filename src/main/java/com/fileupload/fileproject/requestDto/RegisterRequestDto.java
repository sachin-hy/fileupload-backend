package com.fileupload.fileproject.requestDto;


import lombok.*;
import org.hibernate.annotations.SecondaryRow;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class RegisterRequestDto {


    private String password;
    private String email;
    private String firstName;
    private String lastName;
}
