package com.fileupload.fileproject.service;



import com.fileupload.fileproject.context.LookupContext;
import com.fileupload.fileproject.context.TenantContext;
import com.fileupload.fileproject.entity.TenantLookup;
import com.fileupload.fileproject.entity.Users;
import com.fileupload.fileproject.repository.TenantLookupRepository;
import com.fileupload.fileproject.repository.UsersRepository;
import com.fileupload.fileproject.util.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;



@Slf4j
@Service
public class SecurityCustomService implements UserDetailsService {


    @Autowired
    private UsersRepository userRepo;
    @Autowired
    private TenantLookupRepository tenantLookupRepository;

    @Override
    @Transactional
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

           String subdomain = LookupContext.getSubdomain();

           TenantLookup tenantLookup =  tenantLookupRepository.findByEmailAndSubdomain(username,subdomain);

          if(tenantLookup == null){
              throw new UsernameNotFoundException("Invalid email or subdomain in tenant_lookup");
          }
          Long tenantId = tenantLookup.getTenantId();

         Optional<Users> user = userRepo.findByEmailWithTenantDetails(username,tenantId);

         if(user.isPresent()){

            return  new CustomUserDetails(user.get());
         }
         else{

            throw new UsernameNotFoundException("Enter a Valid Email id or subdomain");
         }
    }
}
