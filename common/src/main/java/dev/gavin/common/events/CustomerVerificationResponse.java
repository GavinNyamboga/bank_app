package dev.gavin.common.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerVerificationResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long accountId;
    private Long customerId;
    private UUID requestId;
    private boolean customerExists;

    @Override
    public String toString() {
        return "CustomerVerificationResponse{" +
                "accountId=" + accountId +
                ", customerId=" + customerId +
                ", requestId=" + requestId +
                ", customerExists=" + customerExists +
                '}';
    }
}
