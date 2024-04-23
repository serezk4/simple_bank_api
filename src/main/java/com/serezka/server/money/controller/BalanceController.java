package com.serezka.server.money.controller;

import com.serezka.server.authorization.database.model.User;
import com.serezka.server.authorization.database.service.UserService;
import com.serezka.server.money.controller.dto.TransferDto;
import com.serezka.server.money.database.model.Balance;
import com.serezka.server.money.database.service.BalanceService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Controller for balance operations
 *
 * @author serezk4
 * @version 1.0
 */

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Log4j2
public class BalanceController {
    BalanceService balanceService;
    UserService userService;

    /**
     * Get user balance
     *
     * @param user authenticated user
     * @return user balance
     */
    @GetMapping("/money")
    public ResponseEntity<Double> getBalance(@AuthenticationPrincipal User user) {
        Optional<Balance> balance = balanceService.findByUser(user);

        if (balance.isEmpty()) {
            log.error("Failed to get balance: balance not found for user {}", user.getId());
            return ResponseEntity.badRequest()
                    .body(0D);
        }

        log.info("User {} balance is {}$", user.getUsername(), balance.get().getBalance());

        return ResponseEntity.ok().body(balance.get().getBalance().doubleValue());
    }

    /**
     * Add money to user balance
     *
     * @param user     authenticated user
     * @param transfer transfer data
     * @return response with error message if transfer failed and success message if succeeded
     */
    @PostMapping("/money")
    public ResponseEntity<String> addMoney(@AuthenticationPrincipal User user, @RequestBody TransferDto transfer) {
        Optional<User> toUser = userService.findByUsername(transfer.to());

        if (toUser.isEmpty())
            return ResponseEntity.badRequest()
                    .body(String.format("User %s not exists", transfer.to()));

        Optional<Balance> fromBalance = balanceService.findByUser(user);
        Optional<Balance> toBalance = balanceService.findByUser(toUser.get());

        if (fromBalance.isEmpty()) return ResponseEntity.badRequest()
                .body(String.format("Balance not found for user %s", user.getId()));
        if (toBalance.isEmpty()) return ResponseEntity.badRequest()
                .body(String.format("Balance not found for user %s", toUser.get().getId()));

        try {
            balanceService.withdraw(fromBalance.get(), toBalance.get(), BigDecimal.valueOf(transfer.amount()));
            log.info("User {} was sent {}$ to the user {}", user.getUsername(), transfer.amount(), toUser.get().getUsername());
            return ResponseEntity.ok().body("Money transferred successfully");
        } catch (Exception e) {
            log.error("Failed to transfer money from {} to {} with amount {}: {}", user.getUsername(), toUser.get().getUsername(), transfer.amount(), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
