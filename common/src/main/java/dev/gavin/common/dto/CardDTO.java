package dev.gavin.common.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardDTO {
    private Long id;

    private LocalDateTime createdAt;

    private String cardAlias;

    private Long accountId;

    private String cardType;

    private String pan;

    private String cvv;
}
