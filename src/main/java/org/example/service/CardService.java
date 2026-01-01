package org.example.service;

import org.example.model.StolenCard;
import org.example.repository.StolenCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardService {
    @Autowired
    private StolenCardRepository stolenCardRepository;

    public List<StolenCard> getListOfStolenCards() {
        return stolenCardRepository.findAllByOrderByIdAsc();
    }

    public StolenCard getStolenCard(String cardNumber) {
        return stolenCardRepository.findByNumber(cardNumber).orElse(null);
    }

    public StolenCard addStolenCard(String cardNumber) {
        try {
            return stolenCardRepository.save(new StolenCard(cardNumber));
        } catch (Exception e) {
            return null;
        }
    }

    public boolean deleteStolenCard(String cardNumber) {
        StolenCard entity = stolenCardRepository.findByNumber(cardNumber).orElse(null);
        if (entity == null) {
            return false;
        }
        stolenCardRepository.delete(entity);
        return true;
    }

    public boolean isValidCardNumber(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }
}
