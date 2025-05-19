package dev.gavin.card_service.service;

import dev.gavin.card_service.entity.Card;
import dev.gavin.card_service.enums.CardType;
import dev.gavin.card_service.repository.CardRepository;
import dev.gavin.card_service.utils.CardNumberGenerator;
import dev.gavin.common.dto.CardDTO;
import dev.gavin.common.exception.BadRequestException;
import dev.gavin.common.exception.InternalErrorException;
import dev.gavin.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CardService {
    private final CardRepository cardRepository;

    private final RestTemplate restTemplate;

    private final CardNumberGenerator cardNumberGenerator;


    @Value("${account-service.url}")
    private String accountServiceUrl;


    public CardService(CardRepository cardRepository, RestTemplate restTemplate, CardNumberGenerator cardNumberGenerator) {
        this.cardRepository = cardRepository;
        this.restTemplate = restTemplate;
        this.cardNumberGenerator = cardNumberGenerator;
    }

    public CardDTO getCardById(Long cardId, Long accountId, boolean showSensitiveData) {
        Card card = cardRepository.findByIdAndAccountId(cardId, accountId)
                .orElseThrow(() -> new BadRequestException("Card not found or does not belong to this account"));

        return mapToDTO(card, showSensitiveData);
    }

    public Page<CardDTO> fetchCards(CardDTO filter, boolean showSensitiveData, Pageable pageable) {
        Page<Card> cards = cardRepository.findWithFilters(
                filter.getCardAlias() != null ? filter.getCardAlias().trim().toLowerCase() : null,
                filter.getPan() != null ? filter.getPan().trim().toLowerCase() : null,
                filter.getCardType() != null ? CardType.fromString(filter.getCardType()) : null,
                filter.getAccountId(),
                pageable
        );

        return cards.map(card -> mapToDTO(card, showSensitiveData));
    }

    public CardDTO createCard(CardDTO cardDTO) {
        if (cardDTO.getAccountId() == null)
            throw new BadRequestException("Account ID is required");
        if (cardDTO.getCardType() == null || cardDTO.getCardType().isBlank())
            throw new BadRequestException("Card type is required");
        if (cardDTO.getCardAlias() == null || cardDTO.getCardAlias().isBlank())
            throw new BadRequestException("Card alias is required");

        boolean accountExists;
        try {

            String url = accountServiceUrl + "/api/accounts/exists/" + cardDTO.getAccountId();
            accountExists = Boolean.TRUE.equals(restTemplate.getForObject(url, Boolean.class));
        } catch (Exception e) {
            throw new InternalErrorException("Failed to verify account existence: " + e.getMessage());
        }

        if (!accountExists) {
            throw new ResourceNotFoundException("Account", "id", String.valueOf(cardDTO.getAccountId()));
        }

        // An account can have only 2 cards but of different types
        int count = cardRepository.countByAccountId(cardDTO.getAccountId());
        if (count >= 2)
            throw new BadRequestException("Cannot create more than 2 cards for this account");

        CardType cardType = CardType.fromString(cardDTO.getCardType());

        List<Card> existingCards = cardRepository.findByAccountIdAndCardType(cardDTO.getAccountId(), cardType);
        if (!existingCards.isEmpty())
            throw new BadRequestException("Account already has a card of type " + cardType.getDisplayName());

        String pan = cardNumberGenerator.generatePAN();
        String cvv = cardNumberGenerator.generateCVV();

        //if exists, regenerate PAN and CVV
        if (cardRepository.existsByPan(pan)) {
            pan = cardNumberGenerator.generatePAN();
            cvv = cardNumberGenerator.generateCVV();
        }

        Card card = new Card();
        card.setCreatedAt(LocalDateTime.now());
        card.setCardAlias(cardDTO.getCardAlias());
        card.setAccountId(cardDTO.getAccountId());
        card.setCardType(cardType);
        card.setPan(pan);
        card.setCvv(cvv);

        Card savedCard = cardRepository.save(card);

        return mapToDTO(savedCard, false);
    }

    public CardDTO updateCardAlias(Long cardId, Long accountId, String newAlias) {
        Card card = cardRepository.findByIdAndAccountId(cardId, accountId)
                .orElseThrow(() -> new BadRequestException("Card not found or does not belong to this account"));

        card.setCardAlias(newAlias);
        card.setUpdatedAt(LocalDateTime.now());
        Card updatedCard = cardRepository.save(card);

        return mapToDTO(updatedCard, false); // Don't show sensitive data on update
    }

    public void deleteCard(Long cardId, Long accountId) {
        Card card = cardRepository.findByIdAndAccountId(cardId, accountId)
                .orElseThrow(() -> new BadRequestException("Card not found or does not belong to this account"));

        cardRepository.delete(card);
    }

    private CardDTO mapToDTO(Card card, boolean showSensitiveData) {
        CardDTO dto = new CardDTO();
        dto.setId(card.getId());
        dto.setCardAlias(card.getCardAlias());
        dto.setAccountId(card.getAccountId());
        dto.setCardType(card.getCardType().getDisplayName());
        dto.setCreatedAt(card.getCreatedAt());

        // Mask PAN and CVV if required
        if (showSensitiveData) {
            dto.setPan(card.getPan());
            dto.setCvv(card.getCvv());
        } else {
            dto.setPan(maskPAN(card.getPan()));
            dto.setCvv(maskCVV());
        }

        return dto;
    }

    private String maskPAN(String pan) {
        if (pan == null || pan.length() < 4) {
            return "****";
        }
        // Keep the first 6 and last 4 digits, mask the rest
        String firstSix = pan.substring(0, 6);
        String lastFour = pan.substring(pan.length() - 4);
        String masked = "*".repeat(pan.length() - 10);
        return firstSix + masked + lastFour;
    }

    private String maskCVV() {
        return "***";
    }

    public Integer getCardCountByAccountId(Long accountId) {
        return cardRepository.countByAccountId(accountId);
    }
}
