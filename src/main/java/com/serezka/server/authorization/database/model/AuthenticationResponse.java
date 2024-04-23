package com.serezka.server.authorization.database.model;

import lombok.Getter;

@Getter
public class AuthenticationResponse {
    private boolean error = false;
    private final String token;

    public AuthenticationResponse(String token) {
        this.token = token;
    }

    public AuthenticationResponse(boolean error, String token) {
        this.error = error;
        this.token = token;
    }
}
