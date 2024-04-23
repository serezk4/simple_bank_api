package com.serezka.server.authorization.controller.dto;

import com.serezka.server.authorization.database.model.User;

public record UserRegistrationDto(String login, String password, String mail) {
    public User toUser() {
        return User.builder()
                .username(login())
                .password(password())
                .mail(mail())
                .build();
    }
}
