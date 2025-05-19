package dev.gavin.account_service.dto;

import dev.gavin.common.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateAccountResponse {

    private Long accountId;

    private AccountStatus status;

    private String description;
}
