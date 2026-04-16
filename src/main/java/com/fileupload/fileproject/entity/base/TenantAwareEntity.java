package com.fileupload.fileproject.entity.base;

import com.fileupload.fileproject.context.TenantContext;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@MappedSuperclass
public abstract class TenantAwareEntity extends BaseEntity {

    @Column(name = "tenant_key", nullable = false , updatable = false)
    private String tenantKey;


    @PrePersist
    @PreUpdate
    private void setTenantId(){

        if(this.tenantKey == null)
        {
            String currentTenantKey = TenantContext.getTenantKey();

            if(currentTenantKey == null)
            {
                throw new IllegalStateException("Current tenant id is null");
            }

            this.tenantKey = currentTenantKey;

        }
    }

}
