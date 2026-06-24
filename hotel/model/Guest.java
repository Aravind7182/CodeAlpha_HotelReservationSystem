package hotel.model;
import java.io.Serializable;

public class Guest implements Serializable {
    private static final long serialVersionUID = 1L;
 
    private final String guestId;
    private String name;
    private String email;
    private String phone;
    private String idProof; // Passport / Aadhaar / PAN
 
    public Guest(String guestId, String name, String email, String phone, String idProof) {
        this.guestId  = guestId;
        this.name     = name;
        this.email    = email;
        this.phone    = phone;
        this.idProof  = idProof;
    }
 
    public String getGuestId() { return guestId; }
    public String getName()    { return name; }
    public String getEmail()   { return email; }
    public String getPhone()   { return phone; }
    public String getIdProof() { return idProof; }
    
    public void setName(String name)   { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
 
    @Override
    public String toString() {
        return String.format("Guest[%s] %s | %s | %s | ID: %s",
                guestId, name, email, phone, idProof);
    }
}