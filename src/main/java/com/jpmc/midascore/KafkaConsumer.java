package com.jpmc.midascore;

import com.jpmc.midascore.foundation.Transaction;  
import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import com.jpmc.midascore.foundation.Incentive;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;


@Component
public class KafkaConsumer {

    private final UserRepository  userRepository;
    private final TransactionRecordRepository recordRepository;
    private final RestTemplate restTemplate;

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    public KafkaConsumer(UserRepository userRepository, TransactionRecordRepository recordRepository,
                         RestTemplate restTemplate ) {
        this.userRepository = userRepository;
        this.recordRepository = recordRepository;
        this.restTemplate = restTemplate;
    }

    /**
     * Listens to the topic from application.yml and:
     * - validates sender/recipient/amount
     * - checks sender has sufficient balance
     * - updates balances and persists a TransactionRecord
     * If invalid, does nothing.
     * Transactional => all-or-nothing (balances + record).
     */
    
    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-core")
    @Transactional
    public void listen(Transaction t) {  
        log.info("Transaction Received: sender={}, recipient={}, amount={}",
                t.getSenderId(), t.getRecipientId(), t.getAmount());
        log.info("Transaction Hash: {}", t.hashCode());
        
        // 1) Basic validation

        java.math.BigDecimal amount ;
        try {
            // if getAmount() returns BigDecimal, this cast works; if it's float/double,
            // fall back
            Object raw = t.getAmount();
            if (raw instanceof java.math.BigDecimal bd) {
                amount = bd;
            } else if (raw instanceof Number n) {
                amount = java.math.BigDecimal.valueOf(n.doubleValue());
            } else {
                amount = new java.math.BigDecimal(raw.toString());
            }
        } catch (Exception e) {
            return; // bad amount -> discard
        }
        if (amount.signum() <= 0)
            return;
        
        Long senderId = toLong(t.getSenderId());
        Long recipientId = toLong(t.getRecipientId());
        if (senderId == null || recipientId == null || senderId.equals(recipientId)) return;

        // 2) Load users
        Optional<UserRecord> senderOpt = userRepository.findById(senderId);
        Optional<UserRecord> recipientOpt = userRepository.findById(recipientId);
        if (senderOpt.isEmpty() || recipientOpt.isEmpty()) return;

        UserRecord sender = senderOpt.get();
        UserRecord recipient = recipientOpt.get();

        // 3) Check for duplicate transaction
        if (recordRepository.existsBySenderAndRecipientAndAmount(sender, recipient, amount)) {
            log.warn("Duplicate transaction detected. Skipping sender={}, recipient={}, amount={}",
                    sender.getName(), recipient.getName(), amount);
            return;
        }

    // 4) Sufficient funds?
        if (sender.getBalance() == null || recipient.getBalance() == null) return;
        if (sender.getBalance().compareTo(amount) < 0) return;
     
        // 5) Call Incentive API
        Incentive incentive;
        try {
            incentive = restTemplate.postForObject(
                    "http://localhost:8080/incentive",
                    t,
                    Incentive.class);
        } catch (Exception e) {
            log.error("Failed to call Incentive API", e);
            return;
        }

        // Handle missing/null incentive
        java.math.BigDecimal incentiveAmount = (incentive != null && incentive.getAmount() != null)
                ? incentive.getAmount()
                : java.math.BigDecimal.ZERO;

        log.info("INCENTIVE_AMOUNT={}", incentiveAmount);


    // 6) Update balances
        sender.setBalance(sender.getBalance().subtract(amount));
        java.math.BigDecimal totalCredit = amount;

        if (incentiveAmount.compareTo(java.math.BigDecimal.ZERO) > 0) {
            log.info("Applying incentive of {}", incentiveAmount);
            totalCredit = totalCredit.add(incentiveAmount);
        }

        recipient.setBalance(recipient.getBalance().add(totalCredit));
 
    // 7) Persist transaction record
        
        TransactionRecord rec = new TransactionRecord();
        rec.setSender(sender);
        rec.setRecipient(recipient);
        rec.setAmount(amount);
        recordRepository.save(rec);
        
        var w = java.util.stream.StreamSupport
                .stream(userRepository.findAll().spliterator(), false)
                .filter(u -> "waldorf".equals(u.getName()))
                .findFirst()
                .orElse(null);
        log.info("WALDORF_BALANCE={}", w != null ? w.getBalance() : null);
        log.info("Processed transaction: senderId={}, recipientId={}, amount={}",
                 sender.getId(), recipient.getId(), amount);

        var wilbur = java.util.stream.StreamSupport
                .stream(userRepository.findAll().spliterator(), false)
                .filter(u -> "wilbur".equals(u.getName()))
                .findFirst()
                .orElse(null);
        log.info("WILBUR_BALANCE={}", wilbur != null ? wilbur.getBalance() : null);
    }
       
        
    private Long toLong(Object id) {
       if (id == null) return null;
       if (id instanceof Number n) return n.longValue();
       try { return Long.valueOf(id.toString()); } catch (Exception e) { return null; }
    }

        
}