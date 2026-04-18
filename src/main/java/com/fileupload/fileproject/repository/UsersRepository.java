package com.fileupload.fileproject.repository;


import com.fileupload.fileproject.entity.Tenant;
import com.fileupload.fileproject.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface UsersRepository extends JpaRepository<Users,Long> {
   Optional<Users> findByEmail(String username);

    List<Users> findAllByTenant(Tenant tenant);
}
