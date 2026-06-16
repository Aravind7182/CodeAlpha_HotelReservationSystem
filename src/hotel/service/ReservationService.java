package hotel.service;
 
import hotel.model.*;
import hotel.payment.*;
import hotel.storage.FileStorageManager;
 
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
 
public class ReservationService {
 
    private final Map<String, Reservation> reservations;
    private final RoomService roomService;
    private final FileStorageManager storage;
    private final PaymentProcessor paymentProcessor;
    private int idCounter;
 
    public ReservationService(RoomService roomService, FileStorageManager storage) {
        this.roomService       = roomService;
        this.storage           = storage;
        this.paymentProcessor  = new PaymentProcessor();
        this.reservations      = storage.loadReservations();
        this.idCounter         = reservations.size() + 1;
    }
 
    /**
     * Creates a reservation and marks room as occupied.
     * Does NOT process payment — call processPayment() afterwards.
     */
    public Reservation createReservation(Guest guest, int roomNumber,
            LocalDate checkIn, LocalDate checkOut) {
if (checkIn.isAfter(checkOut) || checkIn.isEqual(checkOut))
throw new IllegalArgumentException("Check-out must be after check-in.");
if (checkIn.isBefore(LocalDate.now()))
throw new IllegalArgumentException("Check-in date cannot be in the past.");

Room room = roomService.findRoom(roomNumber)
.orElseThrow(() -> new IllegalArgumentException("Room " + roomNumber + " not found."));
if (!room.isAvailable())
throw new IllegalStateException("Room " + roomNumber + " is not available.");

String id = String.format("RES-%04d", idCounter++);
Reservation reservation = new Reservation(id, guest, room, checkIn, checkOut);
reservations.put(id, reservation);
roomService.markOccupied(roomNumber);
storage.saveReservations(reservations);
return reservation;
}

/**
* Processes payment for a reservation.
* @return PaymentResult with success/failure details.
*/
    public PaymentResult processPayment(String reservationId, PaymentMethod method,
            String paymentDetail) {
Reservation r = getReservation(reservationId)
.orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));
if (r.isPaid())
throw new IllegalStateException("Reservation " + reservationId + " is already paid.");

PaymentResult result;
if (method == PaymentMethod.UPI) {
result = paymentProcessor.processUpi(r.getTotalAmount(), paymentDetail);
} else if (method == PaymentMethod.CREDIT_CARD || method == PaymentMethod.DEBIT_CARD) {
result = paymentProcessor.processCard(r.getTotalAmount(), method, paymentDetail);
} else {
result = paymentProcessor.processPayment(r.getTotalAmount(), method, paymentDetail);
}

if (result.isSuccess()) {
r.setPaid(true);
r.setStatus(ReservationStatus.CONFIRMED);
storage.saveReservations(reservations);
}
return result;
}
    /**
     * Cancels a reservation and frees the room.
     * @return cancellation message.
     */
    public String cancelReservation(String reservationId) {
        Reservation r = getReservation(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));
        if (r.getStatus() == ReservationStatus.CANCELLED)
            throw new IllegalStateException("Reservation is already cancelled.");
        if (r.getStatus() == ReservationStatus.CHECKED_OUT)
            throw new IllegalStateException("Cannot cancel a completed stay.");
 
        r.setStatus(ReservationStatus.CANCELLED);
        roomService.markAvailable(r.getRoom().getRoomNumber());
        storage.saveReservations(reservations);
 
        double refund = r.isPaid() ? r.getTotalAmount() * 0.80 : 0;
        return String.format("Reservation %s cancelled.%s",
                reservationId,
                r.isPaid() ? String.format(" Refund of ₹%.2f (80%%) will be processed in 5-7 business days.", refund) : "");
    }
 
    public String checkIn(String reservationId) {
        Reservation r = getReservation(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found."));
        if (r.getStatus() != ReservationStatus.CONFIRMED)
            throw new IllegalStateException("Only CONFIRMED reservations can be checked in.");
        if (!r.isPaid())
            throw new IllegalStateException("Payment must be completed before check-in.");
        r.setStatus(ReservationStatus.CHECKED_IN);
        storage.saveReservations(reservations);
        return "✓ Check-in successful for " + r.getGuest().getName() + " — Room " + r.getRoom().getRoomNumber();
    }
    public String checkOut(String reservationId) {
        Reservation r = getReservation(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found."));
        if (r.getStatus() != ReservationStatus.CHECKED_IN)
            throw new IllegalStateException("Guest is not currently checked in.");
        r.setStatus(ReservationStatus.CHECKED_OUT);
        roomService.markAvailable(r.getRoom().getRoomNumber());
        storage.saveReservations(reservations);
        return "✓ Check-out successful. Thank you, " + r.getGuest().getName() + "! We hope to see you again.";
    }
 
    public Optional<Reservation> getReservation(String reservationId) {
        return Optional.ofNullable(reservations.get(reservationId));
    }
 
    public List<Reservation> getAllReservations() {
        return new ArrayList<>(reservations.values());
    }
 
    public List<Reservation> getActiveReservations() {
        return reservations.values().stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED
                          || r.getStatus() == ReservationStatus.CHECKED_IN)
                .collect(Collectors.toList());
    }
 
    public List<Reservation> getReservationsByGuest(String guestName) {
        String q = guestName.toLowerCase();
        return reservations.values().stream()
                .filter(r -> r.getGuest().getName().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }
    public boolean exportToText() {
        return storage.exportReservationsToText(reservations);
    }
}
 