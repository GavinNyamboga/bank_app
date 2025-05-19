package dev.gavin.account_service.controller;

import dev.gavin.account_service.dto.AccountRequest;
import dev.gavin.account_service.service.AccountService;
import dev.gavin.common.dto.AccountDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountDTO> createAccount(@RequestBody AccountRequest accountRequest) {
        AccountDTO createdAccount = accountService.createAccount(accountRequest);
        return new ResponseEntity<>(createdAccount, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDTO> getAccountById(@PathVariable Long id) {
        AccountDTO account = accountService.getAccountById(id);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/iban/{iban}")
    public ResponseEntity<AccountDTO> getAccountByIban(@PathVariable String iban) {
        AccountDTO account = accountService.getAccountByIban(iban);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AccountDTO>> getAccountsByCustomerId(@PathVariable Long customerId) {
        List<AccountDTO> accounts = accountService.getAccountsByCustomerId(customerId);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping
    public ResponseEntity<Page<AccountDTO>> getAccountsWithFilters(
            @RequestParam(required = false) String iban,
            @RequestParam(required = false) String bicSwift,
            @RequestParam(required = false) String cardAlias,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        AccountRequest filter = AccountRequest.builder()
                .iban(iban)
                .bicSwift(bicSwift)
                .cardAlias(cardAlias)
                .build();

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<AccountDTO> accounts = accountService.getAccountsWithFilters(filter, pageable);
        return ResponseEntity.ok(accounts);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountDTO> updateAccount(
            @PathVariable Long id,
            @RequestBody AccountRequest accountRequest) {
        AccountDTO updatedAccount = accountService.updateAccount(id, accountRequest);
        return ResponseEntity.ok(updatedAccount);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists/{id}")
    public ResponseEntity<Boolean> isAccountExistsById(@PathVariable Long id) {
        boolean exists = accountService.isAccountExistsById(id);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/count/customer/{customerId}")
    public ResponseEntity<Long> countAccountsByCustomerId(@PathVariable Long customerId) {
        long count = accountService.countAccountsByCustomerId(customerId);
        return ResponseEntity.ok(count);
    }
}