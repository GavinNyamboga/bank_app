package dev.gavin.card_service.repository;

import dev.gavin.card_service.entity.Card;
import dev.gavin.card_service.enums.CardType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByAccountId(Long accountId);

    Optional<Card> findByIdAndAccountId(Long id, Long accountId);

    List<Card> findByAccountIdAndCardType(Long accountId, CardType cardType);

    boolean existsByPan(String pan);

    int countByAccountId(Long accountId);

    @Query("""
            SELECT c FROM Card c WHERE (:cardAlias IS NULL OR LOWER(c.cardAlias) LIKE %:cardAlias%)
                        AND (:pan IS NULL OR LOWER(c.pan) LIKE %:pan%)
                        AND (:cardType IS NULL OR c.cardType = :cardType)
                        AND (:accountId IS NULL OR c.accountId = :accountId)
            """)
    Page<Card> findWithFilters(@Param("cardAlias") String cardAlias, @Param("pan") String pan, @Param("cardType") CardType cardType,
                               @Param("accountId") Long accountId, Pageable pageable);

}
