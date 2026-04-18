package com.fileupload.fileproject.requestDto;

import lombok.Data;

import java.util.List;

@Data
public class InviteRequestDto {
    private List<String> emails;
    private String role;
}
