package com.fileupload.fileproject.service;

import com.fileupload.fileproject.Exception.UserAlreadyPresent;
import com.fileupload.fileproject.context.TenantContext;
import com.fileupload.fileproject.entity.Tenant;
import com.fileupload.fileproject.entity.Users;
import com.fileupload.fileproject.repository.TenantRepository;
import com.fileupload.fileproject.repository.UsersRepository;
import com.fileupload.fileproject.requestDto.RegisterRequestDto;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsersService {

    @Autowired
    public TenantRepository tenantRepository;

    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @Transactional
    public Optional<Users> findByEmail(String email) {

        return usersRepository.findByEmail(email);
    }


    @Transactional
    public List<Map<String, Object>> findAll()
    {
        Long currentTenantId = TenantContext.getTenantId();
        Tenant tenant = tenantRepository.findById(currentTenantId).get();

       List<Users> usersList =  usersRepository.findAllByTenant(tenant);

        return usersList.stream().map( (user) -> {
            HashMap<String, Object> map = new HashMap<>();
            map.put("id",user.getId());
            map.put("email",user.getEmail());
            map.put("firstName",user.getFirstName());
            map.put("lastName",user.getLastName());
            return map;
        }).collect(Collectors.toList());
    }


    @Transactional
    public Users saveUser(RegisterRequestDto registerRequest) {

        Optional<Users> user1 = usersRepository.findByEmail(registerRequest.getEmail());

        if(user1.isEmpty())
        {
            Users user = new Users();
            user.setEmail(registerRequest.getEmail());
            user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));

            return usersRepository.save(user);
        }else
        {
            throw new UserAlreadyPresent("User with user name already present");
        }


    }
}
