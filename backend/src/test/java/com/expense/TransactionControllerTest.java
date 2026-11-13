package com.expense;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {
    @Mock private TransactionRepository transactions;
    @InjectMocks private TransactionController controller;

    @Test
    void summaryAddsIncomeAndExpensesForAuthenticatedUser() {
        when(transactions.findAllByUserIdOrderByDateDesc("user-1"))
            .thenReturn(List.of(transaction("1200.50", Transaction.Type.INCOME), transaction("200.25", Transaction.Type.EXPENSE)));

        var summary = controller.summary(authentication("user-1"));

        assertEquals(new BigDecimal("1200.50"), summary.get("totalIncome"));
        assertEquals(new BigDecimal("200.25"), summary.get("totalExpenses"));
        assertEquals(new BigDecimal("1000.25"), summary.get("balance"));
        verify(transactions).findAllByUserIdOrderByDateDesc("user-1");
    }

    @Test
    void transactionListUsesAuthenticatedUserId() {
        when(transactions.findAllByUserIdOrderByDateDesc("user-2")).thenReturn(List.of());

        controller.list(authentication("user-2"));

        verify(transactions).findAllByUserIdOrderByDateDesc("user-2");
    }

    private UsernamePasswordAuthenticationToken authentication(String userId) {
        return new UsernamePasswordAuthenticationToken(userId, null);
    }

    private Transaction transaction(String amount, Transaction.Type type) {
        Transaction transaction = new Transaction();
        transaction.setUserId("user-1");
        transaction.setTitle("Test entry");
        transaction.setAmount(new BigDecimal(amount));
        transaction.setType(type);
        transaction.setCategory("Other");
        transaction.setDate(LocalDate.of(2026, 1, 1));
        return transaction;
    }
}
