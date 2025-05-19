package dev.gavin.account_service.service;

import dev.gavin.account_service.dto.AccountRequest;
import dev.gavin.account_service.entity.Account;
import dev.gavin.account_service.repository.AccountRepository;
import dev.gavin.common.dto.AccountDTO;
import dev.gavin.common.exception.BadRequestException;
import dev.gavin.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private Account account;
    private AccountRequest accountRequest;
    private AccountDTO accountDTO;

    @BeforeEach
    void setUp() {
        // Setup test data
        LocalDateTime now = LocalDateTime.now();

        account = Account.builder()
                .id(1L)
                .iban("DE89370400440532013000")
                .bicSwift("DEUTDEFF")
                .customerId(1L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        accountRequest = AccountRequest.builder()
                .iban("DE89370400440532013000")
                .bicSwift("DEUTDEFF")
                .customerId(1L)
                .build();

        accountDTO = AccountDTO.builder()
                .id(1L)
                .iban("DE89370400440532013000")
                .bicSwift("DEUTDEFF")
                .customerId(1L)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Test
    void createAccount_Success() {
        // Given
        when(accountRepository.findByIban(anyString())).thenReturn(Optional.empty());
        when(restTemplate.getForObject(anyString(), eq(Boolean.class))).thenReturn(true);
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // When
        AccountDTO result = accountService.createAccount(accountRequest);

        // Then
        assertNotNull(result);
        assertEquals(accountDTO.getId(), result.getId());
        assertEquals(accountDTO.getIban(), result.getIban());
        assertEquals(accountDTO.getBicSwift(), result.getBicSwift());
        assertEquals(accountDTO.getCustomerId(), result.getCustomerId());

        verify(accountRepository, times(1)).findByIban(accountRequest.getIban());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void createAccount_WithNonExistingCustomer_ThrowsError() {
        // Given
        when(accountRepository.findByIban(anyString())).thenReturn(Optional.empty());
        when(restTemplate.getForObject(anyString(), eq(Boolean.class))).thenReturn(false);

        // When
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> accountService.createAccount(accountRequest)
        );

        // Then
        assertEquals(String.format("Customer with id, %s cannot be found", accountDTO.getCustomerId()), exception.getMessage());
        verify(restTemplate, times(1)).getForObject(anyString(), eq(Boolean.class));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void createAccount_DuplicateIban_ThrowsException() {
        // Given
        when(accountRepository.findByIban(anyString())).thenReturn(Optional.of(account));

        // When
        assertThrows(BadRequestException.class, () -> accountService.createAccount(accountRequest));

        // Then
        verify(accountRepository, times(1)).findByIban(accountRequest.getIban());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void getAccountById_Success() {
        // Given
        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(account));

        // When
        AccountDTO result = accountService.getAccountById(1L);

        // Then
        assertNotNull(result);
        assertEquals(accountDTO.getId(), result.getId());
        assertEquals(accountDTO.getIban(), result.getIban());

        verify(accountRepository, times(1)).findById(1L);
    }

    @Test
    void getAccountById_NotFound_ThrowsException() {
        // Given
        when(accountRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When
        assertThrows(ResourceNotFoundException.class, () -> accountService.getAccountById(1L));

        // Then
        verify(accountRepository, times(1)).findById(1L);
    }

    @Test
    void getAccountsByCustomerId_Success() {
        // Given
        List<Account> accounts = Collections.singletonList(account);
        when(accountRepository.findByCustomerId(anyLong())).thenReturn(accounts);

        // When
        List<AccountDTO> results = accountService.getAccountsByCustomerId(1L);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(accountDTO.getId(), results.get(0).getId());

        verify(accountRepository, times(1)).findByCustomerId(1L);
    }

    @Test
    void getAccountsWithFilters_Success() {
        // Given
        Page<Account> accountPage = new PageImpl<>(Collections.singletonList(account));
        AccountRequest filter = new AccountRequest("DE89", "DEUT", null, null);
        Pageable pageable = PageRequest.of(0, 10);

        when(accountRepository.findWithFilters(anyString(), anyString(), any(Pageable.class)))
                .thenReturn(accountPage);

        // When
        Page<AccountDTO> results = accountService.getAccountsWithFilters(filter, pageable);

        // Then
        assertNotNull(results);
        assertEquals(1, results.getTotalElements());
        assertEquals(accountDTO.getId(), results.getContent().get(0).getId());

        verify(accountRepository, times(1)).findWithFilters(filter.getIban(), filter.getBicSwift(), pageable);
    }

    @Test
    void updateAccount_Success() {
        // Given
        AccountRequest updateRequest = AccountRequest.builder()
                .iban("GB29NWBK60161331926819")
                .bicSwift("NWBKGB2L")
                .customerId(1L)
                .build();

        Account updatedAccount = Account.builder()
                .id(1L)
                .iban("GB29NWBK60161331926819")
                .bicSwift("NWBKGB2L")
                .customerId(1L)
                .createdAt(account.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(account));
        when(accountRepository.findByIban(updateRequest.getIban())).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenReturn(updatedAccount);

        // When
        AccountDTO result = accountService.updateAccount(1L, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals(updatedAccount.getId(), result.getId());
        assertEquals(updatedAccount.getIban(), result.getIban());
        assertEquals(updatedAccount.getBicSwift(), result.getBicSwift());

        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).findByIban(updateRequest.getIban());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void updateAccount_NotFound_ThrowsException() {
        // Given
        when(accountRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When
        assertThrows(ResourceNotFoundException.class, () -> accountService.updateAccount(1L, accountRequest));

        // Then
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void updateAccount_DuplicateIban_ThrowsException() {
        // Given
        Account existingAccount = Account.builder()
                .id(2L)
                .iban("GB29NWBK60161331926819")
                .bicSwift("NWBKGB2L")
                .customerId(2L)
                .build();

        AccountRequest updateRequest = AccountRequest.builder()
                .iban("GB29NWBK60161331926819")
                .bicSwift("NWBKGB2L")
                .customerId(1L)
                .build();

        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(account));
        when(accountRepository.findByIban(updateRequest.getIban())).thenReturn(Optional.of(existingAccount));

        // When
        assertThrows(BadRequestException.class, () -> accountService.updateAccount(1L, updateRequest));

        // Then
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).findByIban(updateRequest.getIban());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void deleteAccount_Success() {
        // Given
        when(accountRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(accountRepository).deleteById(anyLong());

        // When
        accountService.deleteAccount(1L);

        // Then
        verify(accountRepository, times(1)).existsById(1L);
        verify(accountRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteAccount_NotFound_ThrowsException() {
        // Given
        when(accountRepository.existsById(anyLong())).thenReturn(false);

        // When
        assertThrows(ResourceNotFoundException.class, () -> accountService.deleteAccount(1L));

        // Then
        verify(accountRepository, times(1)).existsById(1L);
        verify(accountRepository, never()).deleteById(anyLong());
    }

    @Test
    void isAccountExistsById_True() {
        // Given
        when(accountRepository.existsById(anyLong())).thenReturn(true);

        // When
        boolean result = accountService.isAccountExistsById(1L);

        // Then
        assertTrue(result);
        verify(accountRepository, times(1)).existsById(1L);
    }

    @Test
    void isAccountExistsById_False() {
        // Given
        when(accountRepository.existsById(anyLong())).thenReturn(false);

        // When
        boolean result = accountService.isAccountExistsById(1L);

        // Then
        assertFalse(result);
        verify(accountRepository, times(1)).existsById(1L);
    }

    @Test
    void countAccountsByCustomerId_Success() {
        // Given
        when(accountRepository.countByCustomerId(anyLong())).thenReturn(2);

        // When
        long result = accountService.countAccountsByCustomerId(1L);

        // Then
        assertEquals(2L, result);
        verify(accountRepository, times(1)).countByCustomerId(1L);
    }
}