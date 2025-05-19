package dev.gavin.account_service.service;

import dev.gavin.account_service.dto.AccountRequest;
import dev.gavin.account_service.entity.Account;
import dev.gavin.account_service.repository.AccountRepository;
import dev.gavin.common.dto.AccountDTO;
import dev.gavin.common.enums.AccountStatus;
import dev.gavin.common.exception.BadRequestException;
import dev.gavin.common.exception.InternalErrorException;
import dev.gavin.common.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;

    private final RestTemplate restTemplate;

    @Value("${customer-service.url}")
    private String customerServiceUrl;

    @Value("${card-service.url}")
    private String cardServiceUrl;

    public AccountService(AccountRepository accountRepository, RestTemplate restTemplate) {
        this.accountRepository = accountRepository;
        this.restTemplate = restTemplate;
    }

    public AccountDTO createAccount(AccountRequest accountRequest) {
        // Check if IBAN is already in use
        Optional<Account> existingAccount = accountRepository.findByIban(accountRequest.getIban());
        if (existingAccount.isPresent()) {
            throw new BadRequestException("Account with IBAN " + accountRequest.getIban() + " already exists");
        }

        boolean customerExists;
        try {
            String url = customerServiceUrl + "/api/customers/exists/" + accountRequest.getCustomerId();
            customerExists = Boolean.TRUE.equals(restTemplate.getForObject(url, Boolean.class));
        } catch (Exception e) {
            throw new InternalErrorException("Failed to verify customer details: " + e.getMessage());
        }

        if (!customerExists) {
            throw new ResourceNotFoundException("Customer", "id", String.valueOf(accountRequest.getCustomerId()));
        }

        Account account = Account.builder()
                .iban(accountRequest.getIban())
                .bicSwift(accountRequest.getBicSwift())
                .customerId(accountRequest.getCustomerId())
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        Account savedAccount = accountRepository.save(account);
        log.info("Created new account with ID: {}", savedAccount.getId());
        return mapToDTO(savedAccount);
    }


    public AccountDTO getAccountById(Long id) {
        return accountRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", String.valueOf(id)));
    }

    public AccountDTO getAccountByIban(String iban) {
        return accountRepository.findByIban(iban)
                .map(this::mapToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "IBAN", String.valueOf(iban)));
    }


    public List<AccountDTO> getAccountsByCustomerId(Long customerId) {
        return accountRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Page<AccountDTO> getAccountsWithFilters(AccountRequest filter, Pageable pageable) {
        return accountRepository.findWithFilters(filter.getIban(), filter.getBicSwift(), pageable)
                .map(this::mapToDTO);
    }


    @Transactional
    public AccountDTO updateAccount(Long id, AccountRequest accountRequest) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", String.valueOf(id)));

        if (!account.getIban().equals(accountRequest.getIban())) {
            Optional<Account> existingAccount = accountRepository.findByIban(accountRequest.getIban());
            if (existingAccount.isPresent()) {
                throw new BadRequestException("Account with IBAN " + accountRequest.getIban() + " already exists");
            }
        }

        account.setIban(accountRequest.getIban());
        account.setBicSwift(accountRequest.getBicSwift());
        account.setUpdatedAt(LocalDateTime.now());
        Account updatedAccount = accountRepository.save(account);
        log.info("Updated account with ID: {}", updatedAccount.getId());

        return mapToDTO(updatedAccount);
    }


    public void deleteAccount(Long id) {
        if (!accountRepository.existsById(id)) {
            throw new ResourceNotFoundException("Account", "id", String.valueOf(id));
        }

        int cardsCount;
        try {
            Integer response = restTemplate.getForObject(cardServiceUrl + "/api/cards/count/account/" + id, Integer.class);
            cardsCount = response != null ? response : 0;
        } catch (Exception e) {
            throw new InternalErrorException("Failed to fetch card details: " + e.getMessage());
        }

        if (cardsCount > 0) {
            throw new BadRequestException("Account has " + cardsCount + " card(s) and cannot be deleted");
        }

        accountRepository.deleteById(id);
        log.info("Deleted account with ID: {}", id);
    }


    public boolean isAccountExistsById(Long id) {
        return accountRepository.existsById(id);
    }


    public long countAccountsByCustomerId(Long customerId) {
        return accountRepository.countByCustomerId(customerId);
    }

    private AccountDTO mapToDTO(Account account) {
        return AccountDTO.builder()
                .id(account.getId())
                .iban(account.getIban())
                .bicSwift(account.getBicSwift())
                .customerId(account.getCustomerId())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .status(account.getStatus())
                .build();
    }

    private Account mapToEntity(Account account, AccountDTO accountDTO) {
        if (account == null)
            account = new Account();

        account.setIban(accountDTO.getIban());
        account.setBicSwift(accountDTO.getBicSwift());
        account.setCustomerId(accountDTO.getCustomerId());
        account.setCreatedAt(accountDTO.getCreatedAt());
        account.setUpdatedAt(accountDTO.getUpdatedAt());
        return account;
    }
}
