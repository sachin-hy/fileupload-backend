package com.fileupload.fileproject.service;

import com.fileupload.fileproject.Exception.UserAlreadyPresent;
import com.fileupload.fileproject.entity.Users;
import com.fileupload.fileproject.repository.UserRepository;
import com.fileupload.fileproject.requestDto.RegisterRequestDto;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @Transactional
    public Optional<Users> findByEmail(String email) {

        return userRepository.findByEmail(email);
    }



    @Transactional
    public Users saveUser(RegisterRequestDto registerRequest) {

        Optional<Users> user1 = userRepository.findByEmail(registerRequest.getEmail());

        if(user1.isEmpty())
        {
            Users user = new Users();
            user.setEmail(registerRequest.getEmail());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

            return userRepository.save(user);
        }else
        {
            throw new UserAlreadyPresent("User with user name already present");
        }


    }
}
