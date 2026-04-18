package com.fileupload.fileproject.controller;


import com.fileupload.fileproject.entity.Users;
import com.fileupload.fileproject.requestDto.InvitationRegistrationRequestDto;
import com.fileupload.fileproject.requestDto.InviteRequestDto;
import com.fileupload.fileproject.service.InvitationService;
import com.fileupload.fileproject.service.UsersService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tenants")
@AllArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;
    private final UsersService usersService;

    @PostMapping("invite")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> inviteTeamMambers(@RequestBody InviteRequestDto inviteRequest)
    {

         invitationService.sendInvites(inviteRequest.getEmails(),
                                       inviteRequest.getRole());

         Map<String,String> map = new HashMap<>();

         map.put("message", "Invitations sent successfully to " + inviteRequest.getEmails().size() + "users.");

         return ResponseEntity.ok(map);
    }

    @PostMapping("/complete")
    public ResponseEntity<?> completeRegistration(@RequestBody InvitationRegistrationRequestDto dto) {

        Users newUser = invitationService.completeRegistration(dto.getToken(),
                                               dto.getFullName(),
                                               dto.getPassword());


       HashMap<String,String> map = new HashMap<>();
       map.put("message " , "Welcome aboard!");
       map.put("OrganisationName",newUser.getTenant().getOrganisationName());

       return new ResponseEntity<>(map, HttpStatus.OK);


    }



}
