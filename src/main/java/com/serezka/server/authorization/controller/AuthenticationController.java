package com.serezka.server.authorization.controller;

import com.serezka.server.authorization.controller.dto.UserLoginDto;
import com.serezka.server.authorization.controller.dto.UserRegistrationDto;
import com.serezka.server.authorization.database.model.AuthenticationResponse;
import com.serezka.server.authorization.database.model.User;
import com.serezka.server.authorization.database.service.UserService;
import com.serezka.server.authorization.service.AuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for user authentication
 *
 * @author serezk4
 * @version 1.0
 */

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Log4j2
public class AuthenticationController {
    AuthenticationService authService;
    UserService userService;

    /**
     * Register new user
     *
     * @param registrationDto user registration data
     * @return response with error message if registration failed and token if succeeded
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody UserRegistrationDto registrationDto) {
        log.info("New request to register from (login: {} | mail: {})", registrationDto.login(), registrationDto.mail());

        // Check if required fields are not null
        if (registrationDto.login() == null || registrationDto.password() == null || registrationDto.mail() == null) {
            log.info("Failed to process signup query: missing required fields");
            return ResponseEntity.badRequest()
                    .body(new AuthenticationResponse(true, "missing required fields"));
        }

        // Check if user with this mail or username already exists
        if (userService.existsByMail(registrationDto.mail())) {
            log.info("Failed to process signup query: user with this mail already registered: {}", registrationDto.mail());
            return ResponseEntity.ok()
                    .body(new AuthenticationResponse(true, "user with this mail already registered"));
        }

        // Check if user with this username already exists
        if (userService.existsByUsername(registrationDto.login())) {
            log.info("Failed to process signup query: user with this username already exists: {}", registrationDto.login());
            return ResponseEntity.ok()
                    .body(new AuthenticationResponse(true, "user with this username already exists"));
        }

        // try to register user
        try {
            ResponseEntity<AuthenticationResponse> response = ResponseEntity.ok(authService.register(registrationDto.toUser()));
            log.info("User successfully registered: {}", registrationDto.login());
            return response;
        } catch (Exception ex) {
            log.warn("Failed to register user: {}", ex.getMessage());
            return ResponseEntity.ok()
                    .body(new AuthenticationResponse(true, ex.getMessage()));
        }
    }

    /**
     * Authenticate user
     *
     * @param loginDto user login data
     * @return response with error message if authentication failed and token if succeeded
     */
    @PostMapping("/signin")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody UserLoginDto loginDto) {
        log.info("New request to authenticate from (login: {})", loginDto.login());
        try {
            ResponseEntity<AuthenticationResponse> response = ResponseEntity.ok(authService.authenticate(loginDto.login(), loginDto.password()));
            log.info("User successfully authenticated: {}", loginDto.login());
            return response;
        } catch (Exception ex) {
            log.warn("Failed to authenticate user: {}", ex.getMessage());
            return ResponseEntity.ok(new AuthenticationResponse(true, "incorrect username/password"));
        }
    }

}
