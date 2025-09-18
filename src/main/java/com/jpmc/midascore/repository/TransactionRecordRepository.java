package com.jpmc.midascore.repository;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;

import java.math.BigDecimal;

import org.springframework.data.repository.CrudRepository;

public interface TransactionRecordRepository extends CrudRepository<TransactionRecord, Long> {
    boolean existsBySenderAndRecipientAndAmount(UserRecord sender, UserRecord recipient, BigDecimal amount);

}
