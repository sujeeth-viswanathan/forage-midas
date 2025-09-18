package com.jpmc.midascore.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;


@Entity
@Table(name = "transactions")
public class TransactionRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private UserRecord sender;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private UserRecord recipient;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
    
    // getters and setters
    public Long getId() {
        return id;
    }
    public UserRecord getSender() {
        return sender;
    }
    public void setSender(UserRecord sender) {
        this.sender = sender;
    }
    public UserRecord getRecipient() {
        return recipient;
    }
    public void setRecipient(UserRecord recipient) {
        this.recipient = recipient;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
