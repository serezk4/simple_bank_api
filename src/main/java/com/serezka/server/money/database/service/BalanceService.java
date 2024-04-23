package com.serezka.server.money.database.service;

import com.serezka.server.authorization.database.model.User;
import com.serezka.server.money.database.model.Balance;
import com.serezka.server.money.database.repository.BalanceRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class BalanceService {
    BalanceRepository balanceRepository;
    EntityManager entityManager;

    /**
     * Save balance
     *
     * @param balance balance to save
     * @return saved balance
     * @throws IllegalArgumentException if user is null or balance already exists
     */
    @Transactional
    public Balance save(Balance balance) {
        if (balance.getUser() == null) throw new IllegalArgumentException("User cannot be null");
        if (balanceRepository.existsByUser(balance.getUser())) return balance;

        return balanceRepository.save(balance);
    }

    @Transactional
    public Balance create(User user) {
        if (user == null) throw new IllegalArgumentException("User can't be null!");
        if (existsByUser(user)) throw new IllegalArgumentException("User already has bank account!");

        return save(Balance.builder()
                .user(user)
                .build());
    }

    /**
     * Withdraw money from one balance and add to another
     * Use this method if you want to transfer money between two balances
     *
     * @param from   balance to withdraw
     * @param to     balance to add
     * @param amount amount of money to withdraw
     * @throws IllegalArgumentException if from or to is null, from equals to, amount is null or not enough money
     */
    @Transactional
    public void withdraw(Balance from, Balance to, BigDecimal amount) {
        // check if balances are null
        if (from == null || to == null) throw new IllegalArgumentException("Balances cannot be null");
        // check if balances different
        if (from.equals(to)) throw new IllegalArgumentException("Balances cannot be the same");
        // check if amount is null
        if (amount == null) throw new IllegalArgumentException("Amount cannot be null");
        // check if amount > 0
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount can't be <= 0");
        // Checking if there is enough money
        if (from.getBalance().compareTo(amount) < 0)
            throw new IllegalArgumentException("Not enough money for operation");

        // lock balances
        lockBalance(from);
        lockBalance(to);

        // withdraw money and add to another balance
        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        // save balances
        save(from);
        save(to);
    }

    /**
     * Withdraw money from balance
     * Use this method if you want to withdraw money from one balance
     *
     * @param from   balance to withdraw
     * @param amount amount of money to withdraw
     * @throws IllegalArgumentException if from is null, amount is null or not enough money
     */
    @Transactional
    public void withdraw(Balance from, BigDecimal amount) {
        // check if balance is null
        if (from == null) throw new IllegalArgumentException("Balance cannot be null");
        // check if amount is null
        if (amount == null) throw new IllegalArgumentException("Amount cannot be null");
        // checking if there is enough money
        if (from.getBalance().compareTo(amount) < 0)
            throw new IllegalArgumentException("Not enough money for operation");
        // check if amount > 0
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount can't be <= 0");

        // lock balance
        lockBalance(from);

        // withdraw money
        from.setBalance(from.getBalance().subtract(amount));

        // save balance
        save(from);
    }

    /**
     * Lock balance
     *
     * @param balance balance to lock
     */
    private void lockBalance(Balance balance) {
        entityManager.lock(balance, LockModeType.PESSIMISTIC_WRITE);
    }

    /**
     * Find balance by user
     *
     * @param user user to find balance
     * @return optional balance
     */
    @Transactional
    public Optional<Balance> findByUser(User user) {
        return balanceRepository.findByUser(user);
    }

    /**
     * Check if balance exists by user
     *
     * @param user user to check
     * @return true if balance exists
     */
    @Transactional
    public boolean existsByUser(User user) {
        return balanceRepository.existsByUser(user);
    }
}
