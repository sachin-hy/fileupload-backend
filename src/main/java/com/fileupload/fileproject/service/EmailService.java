package com.fileupload.fileproject.service;


import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public void sendInvite(String email,String inviteUrl,String organisationName)
    {
        System.out.println("invitation send");
        System.out.println("email : = " + email);
        System.out.println("inviteUrl : = " + inviteUrl);
        System.out.println("organisationName : = " + organisationName);
    }
}
