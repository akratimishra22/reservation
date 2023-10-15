package hotel.reservation.system.reservation.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class Payment {
    private Long id;
    private Long customerId;
    private BigDecimal amount;
    private String paymentStatus;

}