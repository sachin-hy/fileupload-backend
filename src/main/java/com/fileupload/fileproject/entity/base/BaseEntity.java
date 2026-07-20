package com.fileupload.fileproject.entity.base;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;


@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDate updatedAt;

    @PrePersist
    public void onCreate()
    {
        if(createdAt == null)
        {
            createdAt = LocalDate.now();
        }

        if(updatedAt == null)
        {
            updatedAt = LocalDate.now();

        }
    }

    @PreUpdate
    public void onUpdate()
    {
        updatedAt = LocalDate.now();
    }
}
