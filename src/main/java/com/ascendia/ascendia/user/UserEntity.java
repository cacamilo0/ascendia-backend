package com.ascendia.ascendia.user;

import com.ascendia.ascendia.common.Area;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
public class UserEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = true)
    private OffsetDateTime lastLoginAt;

    @Enumerated(EnumType.STRING)
    private Area area;

}
