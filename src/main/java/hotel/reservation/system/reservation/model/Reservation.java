package hotel.reservation.system.reservation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reservations")
public class Reservation {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "customerId", nullable = false)
	private Long customerId;

	@Column(name = "hotelId", nullable = false)
	private Long hotelId;

	@Column(name = "startDate", nullable = false)
	private LocalDate startDate;

	@Column(name = "endDate", nullable = false)
	private LocalDate endDate;

	@Column(name = "bookingAmount", nullable = false)
	private BigDecimal bookingAmount;
}