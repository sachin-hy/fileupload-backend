package com.fileupload.fileproject.repository;


import com.fileupload.fileproject.entity.Tenant;
import com.fileupload.fileproject.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;


public interface UsersRepository extends JpaRepository<Users,Long> {
   Optional<Users> findByEmail(String username);

    //List<Users> findAllByTenant(Tenant tenant);

    boolean existsByEmail(String email);

    long countByTenant_Tenantid(Long tenantid);

   // Optional<Users> findByEmailAndTenant(String currentUserEmail, Tenant tenant);

    List<Users> findByTenant_Tenantid(Long tenantId);


    List<Users> findAllByIdInAndTenant_Tenantid(List<Long> userIds, Long currentTenantId);

    Optional<Users> findByEmailAndTenant_Tenantid(String currentUserEmail, Long currentTenantId);


    @Query("SELECT u FROM Users u JOIN FETCH u.tenant WHERE u.email = :email And u.tenant.tenantid = :tenantId")
    Optional<Users> findByEmailWithTenantDetails(@Param("email") String email,@Param("tenantId") Long tenantId);

    List<Users> findAllByTenant_Tenantid(Long currentTenantId);

    @Query("SELECT COUNT(u) > 0 FROM Users u WHERE u.email IN :emails AND u.tenant.tenantid = :tenantId")
    boolean existsByEmailInAndTenantId(@Param("emails") List<String> emails, @Param("tenantId") Long tenantId);

}
