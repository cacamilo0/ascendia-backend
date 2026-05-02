package com.ascendia.ascendia;

import com.ascendia.ascendia.user.UserEntity;
import com.ascendia.ascendia.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;

@SpringBootApplication
public class AscendiaApplication {

	public static void main(String[] args) {
		SpringApplication.run(AscendiaApplication.class, args);
	}

	/*@Bean
	CommandLineRunner init(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {

			if (userRepository.findByEmail("").isEmpty()) {
				UserEntity user = new UserEntity();
				user.setEmail("");
				user.setPassword(passwordEncoder.encode(""));
				user.setCreatedAt(OffsetDateTime.now());

				userRepository.save(user);

				System.out.println("Usuario guardado: " + user.getEmail());
			} else {
				System.out.println("Usuario ya existe");
			}
		};
	}*/

}
