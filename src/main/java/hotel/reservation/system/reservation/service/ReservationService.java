package hotel.reservation.system.reservation.service;

import hotel.reservation.system.reservation.model.Reservation;
import hotel.reservation.system.reservation.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    @Autowired
    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Reservation getReservationById(Long id) {
        Optional<Reservation> reservation = reservationRepository.findById(id);
        return reservation.orElse(null);
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public Reservation updateReservation(Long id, Reservation updatedReservation) {
        Optional<Reservation> existingReservation = reservationRepository.findById(id);
        if (existingReservation.isPresent()) {
            Reservation reservation = existingReservation.get();
            reservation.setBookingAmount(updatedReservation.getBookingAmount());
            reservation.setEndDate(updatedReservation.getEndDate());
            reservation.setStartDate(updatedReservation.getStartDate());
            reservation.setHotelId(updatedReservation.getHotelId());
            reservation.setCustomerId(updatedReservation.getCustomerId());
            return reservationRepository.save(reservation);
        } else {
            return null;
        }
    }

    public boolean deleteReservation(Long id) {
        Optional<Reservation> reservation = reservationRepository.findById(id);
        if (reservation.isPresent()) {
            reservationRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    public Reservation makeReservation(Reservation reservation) {
        return reservationRepository.save(reservation);
    }
}


