package com.fileupload.fileproject.repository;

import com.fileupload.fileproject.entity.TenantInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantInvitationRepository extends JpaRepository<TenantInvitation,Long> {

    Optional<TenantInvitation> findByToken(String token);
}
