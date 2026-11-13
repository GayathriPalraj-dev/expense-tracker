package com.expense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RestController
public class TransactionController {
    public record TransactionRequest(@NotBlank @Size(max=100) String title, @NotNull @DecimalMin(value="0.01") BigDecimal amount, @NotNull Transaction.Type type, @NotBlank @Size(max=60) String category, @Size(max=500) String description, @NotNull LocalDate date) {}
    private final TransactionRepository transactions;
    public TransactionController(TransactionRepository transactions) { this.transactions=transactions; }
    @GetMapping("/api/transactions") public List<Transaction> list(Authentication auth) { return transactions.findAllByUserIdOrderByDateDesc(userId(auth)); }
    @PostMapping("/api/transactions") @ResponseStatus(HttpStatus.CREATED)
    public Transaction create(@Valid @RequestBody TransactionRequest body, Authentication auth) { Transaction t=new Transaction(); apply(t,body); t.setUserId(userId(auth)); return transactions.save(t); }
    @GetMapping("/api/transactions/{id}") public Transaction get(@PathVariable String id, Authentication auth) { return owned(id,auth); }
    @PutMapping("/api/transactions/{id}") public Transaction update(@PathVariable String id,@Valid @RequestBody TransactionRequest body,Authentication auth) { Transaction t=owned(id,auth); apply(t,body); return transactions.save(t); }
    @DeleteMapping("/api/transactions/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id,Authentication auth) { transactions.delete(owned(id,auth)); }
    @GetMapping("/api/dashboard/summary")
    public Map<String,BigDecimal> summary(Authentication auth) {
        BigDecimal income=BigDecimal.ZERO, expense=BigDecimal.ZERO;
        for (Transaction t:transactions.findAllByUserIdOrderByDateDesc(userId(auth))) { if(t.getType()==Transaction.Type.INCOME) income=income.add(t.getAmount()); else expense=expense.add(t.getAmount()); }
        return Map.of("totalIncome",income,"totalExpenses",expense,"balance",income.subtract(expense));
    }
    private Transaction owned(String id,Authentication auth) { return transactions.findByIdAndUserId(id,userId(auth)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Transaction not found")); }
    private String userId(Authentication auth) { return (String)auth.getPrincipal(); }
    private void apply(Transaction t,TransactionRequest b) { t.setTitle(b.title().trim()); t.setAmount(b.amount()); t.setType(b.type()); t.setCategory(b.category().trim()); t.setDescription(b.description()==null?"":b.description().trim()); t.setDate(b.date()); }
}
