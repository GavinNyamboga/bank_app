package dev.gavin.common.events;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerVerificationRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long accountId;
    private Long customerId;
    private UUID requestId;

    public CustomerVerificationRequest(Long accountId, Long customerId) {
        this.accountId = accountId;
        this.customerId = customerId;
    }

    @Override
    public String toString() {
        return "CustomerVerificationRequest{" +
                "accountId=" + accountId +
                ", customerId=" + customerId +
                ", requestId=" + requestId +
                '}';
    }
}

