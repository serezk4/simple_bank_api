package com.serezka.server.authorization.controller;

import com.serezka.server.authorization.database.model.AuthenticationResponse;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthenticationResponseTest {

    @Test
    void testConstructorWithTokenOnly() {
        String token = "sampleToken";
        AuthenticationResponse response = new AuthenticationResponse(token);

        assertFalse(response.isError(), "Error should be false by default when only token is provided");
        assertEquals(token, response.getToken(), "Token should match the provided token");
    }

    @Test
    void testConstructorWithErrorAndToken() {
        String token = "sampleToken";
        boolean error = true;
        AuthenticationResponse response = new AuthenticationResponse(error, token);

        assertTrue(response.isError(), "Error should be true as it was set to true");
        assertEquals(token, response.getToken(), "Token should match the provided token");
    }

    @Test
    void testGetToken() {
        String token = "testToken";
        AuthenticationResponse response = new AuthenticationResponse(token);

        assertEquals(token, response.getToken(), "getToken should return the correct token");
    }

    @Test
    void testIsErrorDefault() {
        String token = "anotherToken";
        AuthenticationResponse response = new AuthenticationResponse(token);

        assertFalse(response.isError(), "isError should return false when not explicitly set");
    }

    @Test
    void testIsErrorExplicit() {
        String token = "anotherToken";
        boolean error = true;
        AuthenticationResponse response = new AuthenticationResponse(error, token);

        assertTrue(response.isError(), "isError should return true when set to true");
    }
}
