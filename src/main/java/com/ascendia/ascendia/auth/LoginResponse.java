package com.ascendia.ascendia.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String message;
    private String email;
    private UUID userId;
}