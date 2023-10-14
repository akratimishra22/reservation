package hotel.reservation.system.reservation.repository;


import hotel.reservation.system.reservation.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

}
