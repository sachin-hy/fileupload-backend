package com.fileupload.fileproject.requestDto;


import lombok.*;
import org.hibernate.annotations.SecondaryRow;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data
public class RegisterRequestDto {


    private String password;
    private String email;
    private String firstName;
    private String lastName;
}
