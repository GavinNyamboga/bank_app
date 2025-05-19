package dev.gavin.account_service.repository;

import dev.gavin.account_service.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Page<Account> findByCustomerId(Long customerId, Pageable pageable);

    Page<Account> findByIbanContaining(String iban, Pageable pageable);

    Page<Account> findByBicSwiftContaining(String bicSwift, Pageable pageable);

    Optional<Account> findByIban(String iban);

    boolean existsByIban(String iban);

    List<Account> findByCustomerId(Long customerId);

    @Query("""
            SELECT a FROM Account a
                WHERE (:iban IS NULL OR a.iban LIKE %:iban%) AND
                (:bicSwift IS NULL OR a.bicSwift LIKE %:bicSwift%)
            """)
    Page<Account> findWithFilters(
            @Param("iban") String iban, @Param("bicSwift") String bicSwift, Pageable pageable);

    int countByCustomerId(Long customerId);
}
