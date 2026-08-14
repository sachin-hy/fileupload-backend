package com.fileupload.fileproject.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@Entity
@Table(
        name = "tenant_lookup",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_tenant_lookup_email_subdomain",
                        columnNames = {"email", "subdomain"}
                )
        }
)
public class TenantLookup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "subdomain", nullable = false)
    private String subdomain;
}
