package dev.gavin.common.dto;

import dev.gavin.common.enums.AccountStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountDTO {
    private Long id;
    private String iban;
    private String bicSwift;
    private Long customerId;
    private CustomerDTO customer;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private AccountStatus status;
    private String description;
    private List<CardDTO> cards;
}
