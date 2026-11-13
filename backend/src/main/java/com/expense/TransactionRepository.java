package com.expense;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TransactionRepository extends MongoRepository<Transaction, String> {
    List<Transaction> findAllByUserIdOrderByDateDesc(String userId);
    Optional<Transaction> findByIdAndUserId(String id, String userId);
}
