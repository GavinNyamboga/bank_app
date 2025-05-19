package dev.gavin.card_service.utils;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class CardNumberGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generatePAN() {
        StringBuilder pan = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            pan.append(RANDOM.nextInt(10));
        }
        return pan.toString();
    }

    public String generateCVV() {
        StringBuilder cvv = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            cvv.append(RANDOM.nextInt(10));
        }
        return cvv.toString();
    }
}