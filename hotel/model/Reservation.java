package hotel.model;
 
import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
 
public class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;
 
    private final String reservationId;
    private final Guest guest;
    private final Room room;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private ReservationStatus status;
    private final double totalAmount;
    private boolean isPaid;
    private final LocalDate bookingDate;
 
    public Reservation(String reservationId, Guest guest, Room room,
                       LocalDate checkIn, LocalDate checkOut) {
        this.reservationId = reservationId;
        this.guest         = guest;
        this.room          = room;
        this.checkIn       = checkIn;
        this.checkOut      = checkOut;
        this.status        = ReservationStatus.CONFIRMED;
        this.isPaid        = false;
        this.bookingDate   = LocalDate.now();
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        this.totalAmount   = nights * room.getPricePerNight();
    }
    public String getReservationId()   { return reservationId; }
    public Guest getGuest()            { return guest; }
    public Room getRoom()              { return room; }
    public LocalDate getCheckIn()      { return checkIn; }
    public LocalDate getCheckOut()     { return checkOut; }
    public ReservationStatus getStatus(){ return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }
    public double getTotalAmount()     { return totalAmount; }
    public boolean isPaid()            { return isPaid; }
    public void setPaid(boolean paid)  { this.isPaid = paid; }
    public LocalDate getBookingDate()  { return bookingDate; }
 
    public long getNumberOfNights() {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }
    public String getSummary() {
        return String.format("""
                ╔══════════════════════════════════════════════════════╗
                ║            BOOKING CONFIRMATION                      ║
                ╠══════════════════════════════════════════════════════╣
                ║  Booking ID   : %-36s║
                ║  Guest Name   : %-36s║
                ║  Room No.     : %-36s║
                ║  Category     : %-36s║
                ║  Check-In     : %-36s║
                ║  Check-Out    : %-36s║
                ║  Nights       : %-36s║
                ║  Rate/Night   : ₹%-35.2f║
                ║  Total Amount : ₹%-35.2f║
                ║  Status       : %-36s║
                ║  Payment      : %-36s║
                ║  Booked On    : %-36s║
                ╚══════════════════════════════════════════════════════╝
                """,
                reservationId, guest.getName(),
                room.getRoomNumber(), room.getCategory().getDisplayName(),
                checkIn, checkOut, getNumberOfNights() + " night(s)",
                room.getPricePerNight(), totalAmount,
                status, isPaid ? "PAID" : "PENDING",
                bookingDate);
    }
 
    @Override
    public String toString() {
        return String.format("[%s] Room %-3d | %s | %s → %s | ₹%-8.2f | %-11s | %s",
                reservationId, room.getRoomNumber(), guest.getName(),
                checkIn, checkOut, totalAmount,
                status, isPaid ? "PAID" : "PENDING");
    }
}
 