package org.example.controller;

import org.example.model.Transaction;
import org.example.model.TransactionResponse;
import org.example.service.CardService;
import org.example.service.TransactionService;
import org.example.validation.ValidResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/antifraud")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private CardService cardService;

    private record AddFeedbackRequest(@NotNull Long transactionId, @NotBlank @ValidResult String feedback) {}

    @PostMapping("/transaction")
    public ResponseEntity<TransactionResponse> handleTransaction(@RequestBody @Valid Transaction transaction) {
        if (!transactionService.isValidTransactionInfo(transaction)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(transactionService.saveTransaction(transaction));
    }

    @PutMapping("/transaction")
    public ResponseEntity<Transaction> putTransactionFeedback(@RequestBody @Valid AddFeedbackRequest request) {
        Transaction transaction = transactionService.findTransactionById(request.transactionId());
        if (transaction == null) {
            return ResponseEntity.notFound().build();
        } else if (!transaction.getFeedback().isEmpty()) {
            return ResponseEntity.status(409).build();
        } else if(transaction.getResult().equals(request.feedback())) {
            return ResponseEntity.status(422).build();
        }
        transaction.setFeedback(request.feedback());
        return ResponseEntity.ok(transactionService.putTransactionFeedback(transaction));
    }

    @GetMapping("/history")
    public ResponseEntity<List<Transaction>> getTransactionHistory() {
        return ResponseEntity.ok(transactionService.getTransactionHistory());
    }

    @GetMapping("/history/{number}")
    public ResponseEntity<List<Transaction>> getTransactionHistoryByCardNumber(@PathVariable String number) {
        if (!cardService.isValidCardNumber(number)) {
            return ResponseEntity.badRequest().build();
        }
        List<Transaction> transactions = transactionService.getTransactionHistoryByCardNumber(number);
        return transactions.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(transactions);
    }
}
