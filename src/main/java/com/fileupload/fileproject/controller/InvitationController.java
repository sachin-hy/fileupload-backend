package com.fileupload.fileproject.controller;


import com.fileupload.fileproject.entity.Users;
import com.fileupload.fileproject.requestDto.InvitationRegistrationRequestDto;
import com.fileupload.fileproject.requestDto.InviteRequestDto;
import com.fileupload.fileproject.service.InvitationService;
import com.fileupload.fileproject.service.UsersService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;
    private final UsersService usersService;


    private String getClientIp(HttpServletRequest request)
    {

        String xfheader = request.getHeader("X-Forwarded-For");

        if(xfheader == null)
        {
            return request.getRemoteAddr();
        }

        return xfheader.split(",")[0];
    }


    @PostMapping("/private/invitations/send")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<?> inviteTeamMambers(@RequestBody InviteRequestDto inviteRequest,
                                               HttpServletRequest request)
    {

         String ip = getClientIp(request);

         invitationService.sendInvites(inviteRequest.getEmails(),
                                       inviteRequest.getRole(),
                                       ip);

         Map<String,String> map = new HashMap<>();

         map.put("message", "Invitations sent successfully to " + inviteRequest.getEmails().size() + "users.");

         return ResponseEntity.ok(map);
    }



    // registration for the invited team members
    @PostMapping("/public/invitation/register/{token}")
    public ResponseEntity<?> completeRegistration(@RequestBody InvitationRegistrationRequestDto dto,
                                                  @PathVariable("token") String token,
                                                  HttpServletRequest request) {

        String ip = getClientIp(request);

        String organisationName = invitationService.completeRegistration(token,
                                               dto.getFirstName(),
                                               dto.getLastName(),
                                               dto.getPassword(),
                                               ip);


       HashMap<String,String> map = new HashMap<>();
       map.put("message" , "Welcome aboard!");
       map.put("OrganisationName",organisationName);

       return new ResponseEntity<>(map, HttpStatus.OK);
    }



}
