package com.fileupload.fileproject.service;



import com.fileupload.fileproject.entity.Users;
import com.fileupload.fileproject.repository.UsersRepository;
import com.fileupload.fileproject.util.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;



@Slf4j
@Service
public class SecurityCustomService implements UserDetailsService {


    @Autowired
    private UsersRepository userRepo;

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<Users> user = userRepo.findByEmail(username);

        if(user.isPresent()){

            return  new CustomUserDetails(user.get());
        }
       else{

            throw new UsernameNotFoundException("Enter a Valid Email id");
        }
    }
}
