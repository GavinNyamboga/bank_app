package dev.gavin.card_service.entity;

import dev.gavin.card_service.enums.CardType;
import dev.gavin.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "cards")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Card extends BaseEntity {

    @Column(nullable = false, updatable = false)
    private String cardAlias;

    @Column(nullable = false, updatable = false)
    private Long accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private CardType cardType;

    @Column(nullable = false)
    private String pan;

    @Column(nullable = false, length = 3)
    private String cvv;
}
