package com.fileupload.fileproject.requestDto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class InviteRequestDto {
    private List<String> emails;
    private String role;
}
