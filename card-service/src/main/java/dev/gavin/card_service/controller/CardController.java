package dev.gavin.card_service.controller;


import dev.gavin.card_service.service.CardService;
import dev.gavin.common.dto.CardDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    @Autowired
    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<CardDTO> getCardById(
            @PathVariable Long cardId,
            @RequestParam Long accountId,
            @RequestParam(defaultValue = "false") boolean showSensitiveData) {

        CardDTO cardDTO = cardService.getCardById(cardId, accountId, showSensitiveData);
        return ResponseEntity.ok(cardDTO);
    }

    @GetMapping
    public ResponseEntity<Page<CardDTO>> fetchCards(
            @RequestParam(required = false) String cardAlias,
            @RequestParam(required = false) String pan,
            @RequestParam(required = false) String cardType,
            @RequestParam(required = false) Long accountId,
            @RequestParam(defaultValue = "false") boolean showSensitiveData,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        CardDTO filter = CardDTO.builder()
                .accountId(accountId)
                .cardAlias(cardAlias)
                .pan(pan)
                .cardType(cardType)
                .build();

        Page<CardDTO> cards = cardService.fetchCards(filter, showSensitiveData, pageable);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/count/account/{accountId}")
    public ResponseEntity<Integer> getCardCountByAccountId(@PathVariable Long accountId) {
        Integer cardCount = cardService.getCardCountByAccountId(accountId);
        return ResponseEntity.ok(cardCount);
    }

    @PostMapping
    public ResponseEntity<CardDTO> createCard(@RequestBody CardDTO cardDTO) {
        CardDTO createdCard = cardService.createCard(cardDTO);
        return new ResponseEntity<>(createdCard, HttpStatus.CREATED);
    }

    @PatchMapping("/{cardId}/alias")
    public ResponseEntity<CardDTO> updateCardAlias(
            @PathVariable Long cardId,
            @RequestParam Long accountId,
            @RequestBody String newAlias) {

        CardDTO updatedCard = cardService.updateCardAlias(cardId, accountId, newAlias);
        return ResponseEntity.ok(updatedCard);
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteCard(
            @PathVariable Long cardId,
            @RequestParam Long accountId) {

        cardService.deleteCard(cardId, accountId);
        return ResponseEntity.noContent().build();
    }
}
