package com.fileupload.fileproject.controller;

import com.fileupload.fileproject.context.TenantContext;
import com.fileupload.fileproject.entity.Users;
import com.fileupload.fileproject.repository.TenantRepository;
import com.fileupload.fileproject.repository.UsersRepository;
import com.fileupload.fileproject.responseDto.UserDto;
import com.fileupload.fileproject.service.TenantService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/private")
@AllArgsConstructor
public class TenantMemberController {

    private final TenantService tenantService;

    @GetMapping("/members")
    public ResponseEntity<List<UserDto>> getMyTeam() {

        Long currentTenantId = TenantContext.getTenantId();

        List<UserDto> team = tenantService.getTeamMembers(currentTenantId);

        return ResponseEntity.ok(team);
    }
}