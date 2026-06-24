package hotel.payment;
 
public enum PaymentMethod {
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    UPI("UPI"),
    NET_BANKING("Net Banking"),
    CASH("Cash");
 
    private final String display;
    PaymentMethod(String display) { this.display = display; }
    public String getDisplay() { return display; }
 
    @Override
    public String toString() { return display; }
}
