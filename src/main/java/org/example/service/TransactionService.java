package org.example.service;

import org.example.model.CardLimit;
import org.example.model.Transaction;
import org.example.model.TransactionResponse;
import org.example.repository.CardLimitRepository;
import org.example.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {
    @Autowired
    private CardService cardService;
    @Autowired
    private IpService ipService;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private CardLimitRepository limitRepository;

    private enum TransactionResult {
        ALLOWED,
        MANUAL_PROCESSING,
        PROHIBITED
    }

    private CardLimit getLimits() {
        return limitRepository.findById(1L)
                .orElseGet(() -> limitRepository.save(new CardLimit(200L, 1500L)));
    }

    public Transaction findTransactionById(Long id) {
        return transactionRepository.findById(id).orElse(null);
    }

    public List<Transaction> getTransactionHistory() {
        return transactionRepository.findAllByOrderByIdAsc();
    }

    public List<Transaction> getTransactionHistoryByCardNumber(String cardNumber) {
        return transactionRepository.findAllByNumberOrderByIdAsc(cardNumber);
    }

    public TransactionResponse saveTransaction(Transaction transaction) {
        transactionRepository.save(transaction);
        List<String> reasons = new ArrayList<>(getDangerousReasonsOfTransaction(transaction));
        reasons.addAll(getCorrelationReasonsOfTransaction(transaction, 2));
        String amountStatus = resultTransaction(transaction);


        if (reasons.isEmpty()) {
            reasons.addAll(getCorrelationReasonsOfTransaction(transaction, 1));
            if(reasons.isEmpty()) {
                if ("ALLOWED".equals(amountStatus)) {
                    transaction.setResult("ALLOWED");
                    transactionRepository.save(transaction);
                    return new TransactionResponse("ALLOWED", "none");
                } else {
                    transaction.setResult(amountStatus);
                    transactionRepository.save(transaction);
                    return new TransactionResponse(amountStatus, "amount");
                }
            } else {
                transaction.setResult("MANUAL_PROCESSING");
                transactionRepository.save(transaction);
                reasons.sort(String::compareTo);
                return new TransactionResponse("MANUAL_PROCESSING", String.join(", ", reasons));
            }

        } else {
            if ("PROHIBITED".equals(amountStatus)) {
                reasons.add("amount");
            }
            transaction.setResult("PROHIBITED");
            transactionRepository.save(transaction);
            reasons.sort(String::compareTo);
            return new TransactionResponse("PROHIBITED", String.join(", ", reasons));
        }
    }

    public Transaction putTransactionFeedback(Transaction transaction) {
        TransactionResult originalResult = TransactionResult.valueOf(transaction.getResult());
        TransactionResult newResult = TransactionResult.valueOf(transaction.getFeedback());

        CardLimit limit = getLimits();
        updateLimits(limit, originalResult, newResult, transaction.getAmount());
        limitRepository.save(limit);
        return transactionRepository.save(transaction);
    }

    private void updateLimits(CardLimit limit, TransactionResult original, TransactionResult feedback, long amount) {
        if (original == TransactionResult.ALLOWED && feedback == TransactionResult.MANUAL_PROCESSING) {
            limit.setAllowedLimit(decreaseLimit(limit.getAllowedLimit(), amount));
        } else if (original == TransactionResult.ALLOWED && feedback == TransactionResult.PROHIBITED) {
            limit.setAllowedLimit(decreaseLimit(limit.getAllowedLimit(), amount));
            limit.setManualLimit(decreaseLimit(limit.getManualLimit(), amount));
        } else if (original == TransactionResult.MANUAL_PROCESSING && feedback == TransactionResult.ALLOWED) {
            limit.setAllowedLimit(increaseLimit(limit.getAllowedLimit(), amount));
        } else if (original == TransactionResult.MANUAL_PROCESSING && feedback == TransactionResult.PROHIBITED) {
            limit.setManualLimit(decreaseLimit(limit.getManualLimit(), amount));
        } else if (original == TransactionResult.PROHIBITED && feedback == TransactionResult.ALLOWED) {
            limit.setAllowedLimit(increaseLimit(limit.getAllowedLimit(), amount));
            limit.setManualLimit(increaseLimit(limit.getManualLimit(), amount));
        } else if (original == TransactionResult.PROHIBITED && feedback == TransactionResult.MANUAL_PROCESSING) {
            limit.setManualLimit(increaseLimit(limit.getManualLimit(), amount));
        }
    }

    private long increaseLimit(long currentLimit, long amount) {
        return (long) Math.ceil(0.8 * currentLimit + 0.2 * amount);
    }

    private long decreaseLimit(long currentLimit, long amount) {
        return (long) Math.ceil(0.8 * currentLimit - 0.2 * amount);
    }

    public boolean isValidTransactionInfo(Transaction transaction) {
        return cardService.isValidCardNumber(transaction.getNumber()) &&
               ipService.isValidIp(transaction.getIp());
    }

    public List<String> getDangerousReasonsOfTransaction(Transaction transaction) {
        String cardNumber = transaction.getNumber();
        String ip = transaction.getIp();

        List<String> reasons = new ArrayList<>();
        if(cardService.getStolenCard(cardNumber) != null){
            reasons.add("card-number");
        }
        if(ipService.getSuspiciousIp(ip) != null){
            reasons.add("ip");
        }

        return reasons;
    }

    public List<String> getCorrelationReasonsOfTransaction(Transaction transaction, int previousTransactionCount) {
        long previousRegions = countPreviousRegions(transaction);
        long previousIPs = countPreviousIps(transaction);
        List<String> reasons = new ArrayList<>();

        if(previousRegions > previousTransactionCount){
            reasons.add("region-correlation");
        }
        if(previousIPs > previousTransactionCount){
            reasons.add("ip-correlation");
        }
        return reasons;
    }

    public String resultTransaction(Transaction transaction) {
        CardLimit limit = getLimits();
        long amount = transaction.getAmount();
        if (amount <= limit.getAllowedLimit()) {
            return TransactionResult.ALLOWED.name();
        } else if (amount <= limit.getManualLimit()) {
            return TransactionResult.MANUAL_PROCESSING.name();
        } else {
            return TransactionResult.PROHIBITED.name();
        }
    }

    public long countPreviousRegions(Transaction transaction) {
        List<Transaction> history = transactionRepository.findAllByNumberAndDateBetween(
                transaction.getNumber(),
                transaction.getDate().minusHours(1),
                transaction.getDate()
        );
        return history.stream()
                .map(Transaction::getRegion)
                .filter(region -> !region.equals(transaction.getRegion()))
                .distinct()
                .count();
    }

    public long countPreviousIps(Transaction transaction) {
        List<Transaction> history = transactionRepository.findAllByNumberAndDateBetween(
                transaction.getNumber(),
                transaction.getDate().minusHours(1),
                transaction.getDate()
        );
        return history.stream()
                .map(Transaction::getIp)
                .filter(ip -> !ip.equals(transaction.getIp()))
                .distinct()
                .count();
    }
}
