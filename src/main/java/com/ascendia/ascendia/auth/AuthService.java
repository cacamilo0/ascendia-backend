package com.ascendia.ascendia.auth;

import com.ascendia.ascendia.user.UserEntity;
import com.ascendia.ascendia.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {

        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthException("Credenciales incorrectas");
        }

        // actualizar lastLogin
        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);

        return new LoginResponse("Login exitoso", user.getEmail(), user.getId());
    }
}