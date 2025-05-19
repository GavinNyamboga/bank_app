package dev.gavin.account_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequest {

    private String iban;
    private String bicSwift;
    private Long customerId;
    private String cardAlias;
}
