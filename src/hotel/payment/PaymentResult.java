package hotel.payment;
 
import java.time.LocalDateTime;
 
public class PaymentResult {
    private final boolean success;
    private final String transactionId;
    private final double amount;
    private final PaymentMethod method;
    private final LocalDateTime timestamp;
    private final String message;
 
    public PaymentResult(boolean success, String transactionId,
                         double amount, PaymentMethod method, String message) {
        this.success       = success;
        this.transactionId = transactionId;
        this.amount        = amount;
        this.method        = method;
        this.message       = message;
        this.timestamp     = LocalDateTime.now();
    }
 
    public boolean isSuccess()         { return success; }
    public String getTransactionId()   { return transactionId; }
    public double getAmount()          { return amount; }
    public PaymentMethod getMethod()   { return method; }
    public LocalDateTime getTimestamp(){ return timestamp; }
    public String getMessage()         { return message; }
 
    public String getReceipt() {
        if (!success) return "  ✗ Payment FAILED: " + message;
        return String.format("""
                ┌─────────────────────────────────────────┐
                │           PAYMENT RECEIPT               │
                ├─────────────────────────────────────────┤
                │  Transaction ID : %-22s│
                │  Amount Paid    : ₹%-21.2f│
                │  Method         : %-22s│
                │  Date & Time    : %-22s│
                │  Status         : %-22s│
                └─────────────────────────────────────────┘
                """,
                transactionId, amount, method.getDisplay(),
                timestamp.toString().substring(0, 19),
                "SUCCESS ✓");
    }
}