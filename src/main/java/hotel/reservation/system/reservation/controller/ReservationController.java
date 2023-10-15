package hotel.reservation.system.reservation.controller;

import hotel.reservation.system.reservation.model.Payment;
import hotel.reservation.system.reservation.model.Reservation;
import hotel.reservation.system.reservation.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;
    private final RestTemplate restTemplate;
    private final String paymentsBaseUrl;
    private final String notificationsBaseUrl;
    private final String hotelsBaseUrl;

    public ReservationController(RestTemplate restTemplate, @Value("${payments.base-url}") String paymentsBaseUrl, @Value("${notifications.base-url}") String notificationsBaseUrl, @Value("${hotels.base-url}") String hotelsBaseUrl) {
        this.restTemplate = restTemplate;
        this.paymentsBaseUrl = paymentsBaseUrl;
        this.notificationsBaseUrl = notificationsBaseUrl;
        this.hotelsBaseUrl = hotelsBaseUrl;
    }

    @PostMapping
    public ResponseEntity<Reservation> makeReservation(@Valid @RequestBody Reservation reservation) {
        try {
            return new ResponseEntity<>(reservationService.makeReservation(reservation), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(@PathVariable Long id) {
        Reservation reservation = reservationService.getReservationById(id);
        if (reservation != null) {
            return new ResponseEntity<>(reservation, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/reserve-room")
    public ResponseEntity<String> reserveRoom(@RequestBody Reservation reservation) {
        try {
            ResponseEntity<Boolean> roomAvailabilityResponse = restTemplate.exchange(
                    hotelsBaseUrl + "/" + reservation.getHotelId() + "/rooms-available",
                    HttpMethod.GET,
                    null,
                    Boolean.class
            );

            boolean isRoomAvailable = roomAvailabilityResponse.getBody();

            if (isRoomAvailable) {
                Payment paymentRequest = new Payment();
                paymentRequest.setCustomerId(reservation.getCustomerId());
                paymentRequest.setAmount(reservation.getBookingAmount());
                paymentRequest.setPaymentStatus("paid");

                ResponseEntity<Payment> paymentResponse = restTemplate.exchange(
                        paymentsBaseUrl,
                        HttpMethod.POST,
                        new HttpEntity<>(paymentRequest, new HttpHeaders()),
                        Payment.class
                );

                if (paymentResponse != null) {

                    Reservation reservationResponse = reservationService.makeReservation(reservation);

                    if (reservationResponse != null) {
                        restTemplate.put(hotelsBaseUrl + "/" + reservationResponse.getHotelId() + "/decrement-rooms", null);
                        Map<String, String> requestBody = new HashMap<>();
                        requestBody.put("message", "Your room reservation is confirmed. Enjoy your stay!");
                        restTemplate.postForEntity(notificationsBaseUrl + "/send", requestBody, String.class);
                        return new ResponseEntity<>("Reservation successful", HttpStatus.CREATED);
                    } else {
                        restTemplate.put(paymentsBaseUrl + "/" + paymentResponse.getBody().getId() + "/refunded", null);
                        return new ResponseEntity<>("Reservation failed. Refund processed!", HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                } else {
                    return new ResponseEntity<>("Payment failed", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                return new ResponseEntity<>("Room not available", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Reservation failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/cancel-room-reservation/{reservationId}")
    public ResponseEntity<String> cancelRoomReservation(@PathVariable Long reservationId) {
        try {
            Reservation reservation = reservationService.getReservationById(reservationId);

            if (reservation == null) {
                return new ResponseEntity<>("Reservation not found", HttpStatus.NOT_FOUND);
            }

            ResponseEntity<Payment> paymentResponse = restTemplate.exchange(
                    paymentsBaseUrl + "/" + reservationId,
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    Payment.class
            );

            if (paymentResponse == null) {
                return new ResponseEntity<>("Payment not found", HttpStatus.NOT_FOUND);
            }

            restTemplate.put(paymentsBaseUrl + "/" + paymentResponse.getBody().getId() + "/refunded", null);
            restTemplate.put(hotelsBaseUrl + "/" + reservation.getHotelId() + "/increment-rooms", null);

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("message", "Your room reservation has been canceled, and a refund has been processed.");
            restTemplate.postForEntity(notificationsBaseUrl + "/send", requestBody, String.class);
            return new ResponseEntity<>("Room reservation canceled and refund processed!", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Cancellation and refund failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
