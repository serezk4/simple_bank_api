package com.serezka.server.money.controller;

import com.serezka.server.authorization.database.model.User;
import com.serezka.server.authorization.database.service.UserService;
import com.serezka.server.money.controller.dto.TransferDto;
import com.serezka.server.money.database.model.Balance;
import com.serezka.server.money.database.service.BalanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class BalanceControllerTest {
    @Mock
    private BalanceService balanceService;

    @Mock
    private UserService userService;

    @InjectMocks
    private BalanceController balanceController;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        balanceService.create(user);

        Authentication auth = new UsernamePasswordAuthenticationToken(user, null);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testGetBalance_BalanceNotFound() {
        when(balanceService.findByUser(user)).thenReturn(Optional.empty());

        ResponseEntity<Double> response = balanceController.getBalance(user);

        assertEquals(HttpStatusCode.valueOf(400), response.getStatusCode());
        assertEquals(0D, response.getBody());
    }

    @Test
    void testGetBalance_BalanceFound() {
        Balance balance = new Balance();
        balance.setBalance(BigDecimal.valueOf(1500.0));
        when(balanceService.findByUser(user)).thenReturn(Optional.of(balance));

        ResponseEntity<Double> response = balanceController.getBalance(user);

        assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
        assertEquals(1500.0, response.getBody());
    }

    @Test
    void testAddMoney_UserNotFound() {
        TransferDto transfer = new TransferDto("otherUser", 200);
        when(userService.findByUsername("otherUser")).thenReturn(Optional.empty());

        ResponseEntity<String> response = balanceController.addMoney(user, transfer);

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("not exists"));
    }

    @Test
    void testAddMoney_TransferMoney() {
        User toUser = new User();
        toUser.setId(2L);
        toUser.setUsername("otherUser");

        TransferDto transfer = new TransferDto("otherUser", 200);
        when(userService.findByUsername("otherUser")).thenReturn(Optional.of(toUser));
        when(balanceService.findByUser(user)).thenReturn(Optional.of(new Balance()));
        when(balanceService.findByUser(toUser)).thenReturn(Optional.of(new Balance()));

        ResponseEntity<String> response = balanceController.addMoney(user, transfer);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("successfully"));
    }

    @Test
    void testAddMoney_FailureInTransfer() {
        User toUser = new User();
        toUser.setId(2L);
        toUser.setUsername("otherUser");

        TransferDto transfer = new TransferDto("otherUser", 200);
        when(userService.findByUsername("otherUser")).thenReturn(Optional.of(toUser));
        when(balanceService.findByUser(user)).thenReturn(Optional.of(new Balance()));
        when(balanceService.findByUser(toUser)).thenReturn(Optional.of(new Balance()));
        doThrow(new RuntimeException("Insufficient funds")).when(balanceService).withdraw(any(Balance.class), any(Balance.class), any(BigDecimal.class));

        ResponseEntity<String> response = balanceController.addMoney(user, transfer);

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("Insufficient funds"));
    }
}
