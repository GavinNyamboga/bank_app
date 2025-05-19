package dev.gavin.account_service.entity;

import dev.gavin.common.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Table(name = "accounts")
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted")
    private boolean deleted = false;

    @Version
    @Column(name = "version")
    @ColumnDefault("0")
    private Integer version = 0;

    @Column(nullable = false, unique = true)
    private String iban;

    @Column(nullable = false)
    private String bicSwift;

    @Column(nullable = false, updatable = false)
    private Long customerId;

    @Column
    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    @Column
    private String rejectionReason;


}