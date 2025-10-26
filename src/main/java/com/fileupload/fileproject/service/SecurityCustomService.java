package com.fileupload.fileproject.service;



import com.fileupload.fileproject.entity.Users;
import com.fileupload.fileproject.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;



@Slf4j
@Service
public class SecurityCustomService implements UserDetailsService {


    @Autowired
    private UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<Users> user = userRepo.findByEmail(username);

        if(user.isPresent()){

            return  User.builder()
                    .username(user.get().getEmail())
                    .password(user.get().getPassword())
                    .build();
        }
       else{

            throw new UsernameNotFoundException("Enter a Valid Email id");
        }
    }
}
