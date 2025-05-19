package dev.gavin.card_service.service;

import dev.gavin.card_service.entity.Card;
import dev.gavin.card_service.enums.CardType;
import dev.gavin.card_service.repository.CardRepository;
import dev.gavin.card_service.utils.CardNumberGenerator;
import dev.gavin.common.dto.CardDTO;
import dev.gavin.common.exception.BadRequestException;
import dev.gavin.common.exception.InternalErrorException;
import dev.gavin.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CardNumberGenerator cardNumberGenerator;

    @InjectMocks
    private CardService cardService;

    private Card testvirtualCard;
    private Card testPhysicalCard;
    private CardDTO testCardDTO;
    private final Long ACCOUNT_ID = 1L;
    private final Long CARD_ID = 1L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cardService, "accountServiceUrl", "http://localhost:8080");

        testvirtualCard = new Card();
        testvirtualCard.setId(CARD_ID);
        testvirtualCard.setAccountId(ACCOUNT_ID);
        testvirtualCard.setCardAlias("My Virtual Card");
        testvirtualCard.setCardType(CardType.VIRTUAL);
        testvirtualCard.setPan("1234567890123456");
        testvirtualCard.setCvv("123");

        testPhysicalCard = new Card();
        testPhysicalCard.setId(2L);
        testPhysicalCard.setAccountId(ACCOUNT_ID);
        testPhysicalCard.setCardAlias("My Physical Card");
        testPhysicalCard.setCardType(CardType.PHYSICAL);
        testPhysicalCard.setPan("9876543210987654");
        testPhysicalCard.setCvv("456");

        testCardDTO = new CardDTO();
        testCardDTO.setAccountId(ACCOUNT_ID);
        testCardDTO.setCardAlias("New Test Card");
        testCardDTO.setCardType("Virtual");
    }

    @Test
    void testGetCardById_WithValidCardIdAndAccountId_ReturnsCardDTO() {
        // Given
        when(cardRepository.findByIdAndAccountId(CARD_ID, ACCOUNT_ID)).thenReturn(Optional.of(testvirtualCard));

        // When
        CardDTO result = cardService.getCardById(CARD_ID, ACCOUNT_ID, false);

        // Then
        assertNotNull(result);
        assertEquals(CARD_ID, result.getId());
        assertEquals(ACCOUNT_ID, result.getAccountId());
        assertEquals("My Virtual Card", result.getCardAlias());
        assertEquals("Virtual", result.getCardType());

        assertTrue(result.getPan().startsWith("123456"));
        assertTrue(result.getPan().endsWith("3456"));
        assertEquals("***", result.getCvv());
    }

    @Test
    void testGetCardById_WithSensitiveData_ReturnsUnmaskedData() {
        // Given
        when(cardRepository.findByIdAndAccountId(CARD_ID, ACCOUNT_ID)).thenReturn(Optional.of(testvirtualCard));

        // When
        CardDTO result = cardService.getCardById(CARD_ID, ACCOUNT_ID, true);

        // Then
        assertNotNull(result);
        assertEquals("1234567890123456", result.getPan());
        assertEquals("123", result.getCvv());
    }

    @Test
    void testGetCardById_WithInvalidCardId_ThrowsBadRequestException() {
        // Given
        when(cardRepository.findByIdAndAccountId(CARD_ID, ACCOUNT_ID)).thenReturn(Optional.empty());

        // When
        BadRequestException exception = assertThrows(BadRequestException.class, () -> cardService.getCardById(CARD_ID, ACCOUNT_ID, false));

        // Then
        assertEquals("Card not found or does not belong to this account", exception.getMessage());
    }

    @Test
    void testFetchCards_WithFilters_ReturnsMappedPage() {
        // Given
        CardDTO filter = new CardDTO();
        filter.setCardAlias("Test Alias");
        filter.setPan("1234567890123456");
        filter.setCardType("Virtual");
        filter.setAccountId(ACCOUNT_ID);

        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        Card card = new Card();
        card.setId(10L);
        card.setCardAlias("Test Alias");
        card.setAccountId(ACCOUNT_ID);
        card.setCardType(CardType.VIRTUAL);
        card.setPan("1234567890123456");
        card.setCvv("123");

        Page<Card> cardPage = new org.springframework.data.domain.PageImpl<>(Collections.singletonList(card));
        when(cardRepository.findWithFilters(
                anyString(), anyString(), any(CardType.class), any(Long.class), any(Pageable.class)
        )).thenReturn(cardPage);

        // When
        Page<CardDTO> result = cardService.fetchCards(filter, false, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        CardDTO dto = result.getContent().getFirst();
        assertEquals("Test Alias", dto.getCardAlias());
        assertEquals("Virtual", dto.getCardType());
        assertTrue(dto.getPan().startsWith("123456"));
        assertTrue(dto.getPan().endsWith("3456"));
        assertEquals("***", dto.getCvv());
    }

    @Test
    void testCreateCard_WithValidInput_ReturnsCreatedCardDTO() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(Boolean.class))).thenReturn(true);
        when(cardRepository.countByAccountId(ACCOUNT_ID)).thenReturn(0);
        when(cardRepository.findByAccountIdAndCardType(ACCOUNT_ID, CardType.VIRTUAL)).thenReturn(Collections.emptyList());
        when(cardNumberGenerator.generatePAN()).thenReturn("9999888877776666");
        when(cardNumberGenerator.generateCVV()).thenReturn("999");
        when(cardRepository.existsByPan(anyString())).thenReturn(false);

        Card savedCard = new Card();
        savedCard.setId(3L);
        savedCard.setAccountId(ACCOUNT_ID);
        savedCard.setCardAlias("New Test Card");
        savedCard.setCardType(CardType.VIRTUAL);
        savedCard.setPan("9999888877776666");
        savedCard.setCvv("999");

        when(cardRepository.save(any(Card.class))).thenReturn(savedCard);

        // When
        CardDTO result = cardService.createCard(testCardDTO);

        // Then
        assertNotNull(result);
        assertEquals("New Test Card", result.getCardAlias());
        assertEquals("Virtual", result.getCardType());

        verify(cardRepository).save(any(Card.class));
    }


    @Test
    void testCreateCard_WithNonExistentAccount_ThrowsResourceNotFoundException() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(Boolean.class))).thenReturn(false);

        // When
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> cardService.createCard(testCardDTO));

        // Then
        assertEquals(String.format("Account with id, %d cannot be found", testCardDTO.getAccountId()), exception.getMessage());
    }

    @Test
    void testCreateCard_WithAccountServiceFailure_ThrowsInternalErrorException() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(Boolean.class))).thenThrow(new RuntimeException("Connection refused"));

        // When & Then
        assertThrows(InternalErrorException.class, () -> cardService.createCard(testCardDTO));
    }

    @Test
    void testCreateCard_WithMaxCards_ThrowsBadRequestException() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(Boolean.class))).thenReturn(true);
        when(cardRepository.countByAccountId(ACCOUNT_ID)).thenReturn(2);

        // When
        BadRequestException exception = assertThrows(BadRequestException.class, () -> cardService.createCard(testCardDTO));

        // Then
        assertEquals("Cannot create more than 2 cards for this account", exception.getMessage());
    }

    @Test
    void testCreateCard_WithExistingCardType_ThrowsBadRequestException() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(Boolean.class))).thenReturn(true);
        when(cardRepository.countByAccountId(ACCOUNT_ID)).thenReturn(1);
        when(cardRepository.findByAccountIdAndCardType(ACCOUNT_ID, CardType.VIRTUAL)).thenReturn(Collections.singletonList(testvirtualCard));

        // When
        BadRequestException exception = assertThrows(BadRequestException.class, () -> cardService.createCard(testCardDTO));

        // Then
        assertEquals("Account already has a card of type " + CardType.VIRTUAL.getDisplayName(), exception.getMessage());
    }

    @Test
    void testCreateCard_WithNullAccountId_ThrowsBadRequestException() {
        // Given
        testCardDTO.setAccountId(null);

        // When
        BadRequestException exception = assertThrows(BadRequestException.class, () -> cardService.createCard(testCardDTO));

        // Then
        assertEquals("Account ID is required", exception.getMessage());
    }

    @Test
    void testCreateCard_WithNullCardType_ThrowsBadRequestException() {
        // Given
        testCardDTO.setCardType(null);

        // When
        BadRequestException exception = assertThrows(BadRequestException.class, () -> cardService.createCard(testCardDTO));

        // Then
        assertEquals("Card type is required", exception.getMessage());
    }

    @Test
    void testCreateCard_WithInvalidCardType_ThrowsBadRequestException() {
        // Given
        String invalidCardType = "type c";
        testCardDTO.setCardType(invalidCardType);
        when(restTemplate.getForObject(anyString(), eq(Boolean.class))).thenReturn(true);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> cardService.createCard(testCardDTO));

        // Then
        assertEquals("Unknown card type: " + invalidCardType, exception.getMessage());
    }

    @Test
    void testUpdateCardAlias_WithValidInput_ReturnsUpdatedCardDTO() {
        // Given
        String newAlias = "Updated Card Alias";
        when(cardRepository.findByIdAndAccountId(CARD_ID, ACCOUNT_ID)).thenReturn(Optional.of(testvirtualCard));

        Card updatedCard = new Card();
        updatedCard.setId(CARD_ID);
        updatedCard.setAccountId(ACCOUNT_ID);
        updatedCard.setCardAlias(newAlias);
        updatedCard.setCardType(CardType.VIRTUAL);
        updatedCard.setPan("1234567890123456");
        updatedCard.setCvv("123");

        when(cardRepository.save(any(Card.class))).thenReturn(updatedCard);

        // When
        CardDTO result = cardService.updateCardAlias(CARD_ID, ACCOUNT_ID, newAlias);

        // Then
        assertNotNull(result);
        assertEquals(newAlias, result.getCardAlias());
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void testUpdateCardAlias_WithInvalidCardId_ThrowsBadRequestException() {
        // Given
        when(cardRepository.findByIdAndAccountId(CARD_ID, ACCOUNT_ID)).thenReturn(Optional.empty());

        // When
        BadRequestException exception = assertThrows(BadRequestException.class, () -> cardService.updateCardAlias(CARD_ID, ACCOUNT_ID, "New Alias"));

        // Then
        assertEquals("Card not found or does not belong to this account", exception.getMessage());
    }

    @Test
    void testDeleteCard_WithValidInput_DeletesCard() {
        // Given
        when(cardRepository.findByIdAndAccountId(CARD_ID, ACCOUNT_ID)).thenReturn(Optional.of(testvirtualCard));

        // When
        cardService.deleteCard(CARD_ID, ACCOUNT_ID);

        // Then
        verify(cardRepository).delete(testvirtualCard);
    }


    @Test
    void testGetCardCountByAccountId_ReturnsCorrectCount() {
        // Given
        when(cardRepository.countByAccountId(ACCOUNT_ID)).thenReturn(2);

        // When
        Integer result = cardService.getCardCountByAccountId(ACCOUNT_ID);

        // Then
        assertEquals(2, result);
    }
}