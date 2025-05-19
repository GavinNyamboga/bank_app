package dev.gavin.card_service.enums;

import dev.gavin.common.exception.BadRequestException;
import lombok.Getter;

@Getter
public enum CardType {
    VIRTUAL("Virtual"),
    PHYSICAL("Physical"),
    ;

    private final String displayName;

    CardType(String displayName) {
        this.displayName = displayName;
    }

    public static CardType fromString(String type) {
        for (CardType cardType : CardType.values()) {
            if (cardType.name().equalsIgnoreCase(type)) {
                return cardType;
            }
        }
        throw new BadRequestException("Unknown card type: " + type);
    }
}
