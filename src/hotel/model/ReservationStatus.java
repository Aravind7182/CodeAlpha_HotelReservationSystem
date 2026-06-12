package hotel.model;
 
public enum ReservationStatus {
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled"),
    CHECKED_IN("Checked-In"),
    CHECKED_OUT("Checked-Out");
 
    private final String display;
 
    ReservationStatus(String display) { this.display = display; }
 
    public String getDisplay() { return display; }
 
    @Override
    public String toString() { return display; }
}