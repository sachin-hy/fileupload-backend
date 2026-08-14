package com.fileupload.fileproject.service;


import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Async
    public void sendInvite(String email,String inviteUrl,String organisationName,String subdomain)
    {
        System.out.println("invitation send");
        System.out.println("email : = " + email);
        System.out.println("inviteUrl : = " + inviteUrl);
        System.out.println("organisationName : = " + organisationName);
        System.out.println("subdomain := " + subdomain);

    }
}
