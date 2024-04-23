package com.serezka.server.authorization.service;

import com.serezka.server.authorization.database.model.AuthenticationResponse;
import com.serezka.server.authorization.database.model.User;
import com.serezka.server.authorization.database.repository.UserRepository;
import com.serezka.server.authorization.database.service.UserService;
import com.serezka.server.money.database.service.BalanceService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor
public class AuthenticationService {
    UserService userService;
    BalanceService balanceService;
    PasswordEncoder passwordEncoder;
    JwtService jwtService;
    AuthenticationManager authenticationManager;

    public AuthenticationResponse register(User request) {
        request.setPassword(passwordEncoder.encode(request.getPassword())); // encode pass
        User newUser = userService.save(request); // create user
        balanceService.create(newUser); // create bank balance
        return new AuthenticationResponse(jwtService.generateToken(newUser));
    }

    public AuthenticationResponse authenticate(String username, String password) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password)
        );

        Optional<User> user = userService.findByUsername(username);
        if (user.isEmpty()) return new AuthenticationResponse(true, "incorrect username/password");

        String token = jwtService.generateToken(user.get());

        return new AuthenticationResponse(token);
    }

}
