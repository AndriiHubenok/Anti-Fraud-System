package org.example.controller;

import org.example.model.StolenCard;
import org.example.service.CardService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/antifraud")
public class StolenCardController {
    @Autowired
    CardService cardService;

    private record StolenCardRequest(String number) {}
    private record StolenCardResponse(String status) {}

    @GetMapping("/stolencard")
    public ResponseEntity<Iterable<StolenCard>> getListOfStolenCards() {
        return ResponseEntity.ok(cardService.getListOfStolenCards());
    }

    @PostMapping("/stolencard")
    public ResponseEntity<StolenCard> handleStolenCard(@RequestBody @Valid StolenCardRequest request) {
        if(!cardService.isValidCardNumber(request.number())){
            return ResponseEntity.badRequest().build();
        }
        StolenCard card = cardService.addStolenCard(request.number());
        return card == null ? ResponseEntity.status(409).build() : ResponseEntity.ok(card);
    }

    @DeleteMapping("/stolencard/{number}")
    public ResponseEntity<?> deleteStolenCard(@PathVariable String number) {
        if (!cardService.isValidCardNumber(number)) {
            return ResponseEntity.badRequest().build();
        }
        return cardService.deleteStolenCard(number) ?
                ResponseEntity.ok(new StolenCardResponse("Card " + number + " successfully removed!")) :
                ResponseEntity.notFound().build();
    }
}
