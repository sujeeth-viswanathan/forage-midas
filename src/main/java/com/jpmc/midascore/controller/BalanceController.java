package com.jpmc.midascore.controller;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Balance;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
public class BalanceController {
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/balance")
    public Balance getBalance(@RequestParam("userId") Long userId) {
        Optional<UserRecord> userOpt = userRepository.findById(userId);

        BigDecimal balance = userOpt.map(UserRecord::getBalance)
                .orElse(BigDecimal.ZERO);

        return new Balance(balance.floatValue());
    }
}
