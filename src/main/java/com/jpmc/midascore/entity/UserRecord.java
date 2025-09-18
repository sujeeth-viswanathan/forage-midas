package com.jpmc.midascore.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class UserRecord {

    @Id
    @GeneratedValue()
    private long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    protected UserRecord() {
    }

    public UserRecord(String name, BigDecimal balance) {
        this.name = name;
        this.balance = balance;
    }

    @Override
    public String toString() {
        return String.format("User[id=%d, name='%s', balance='%f'", id, name, balance);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
