package hotel.payment;
 
import java.util.Random;
import java.util.UUID;
 
/**
 * Simulates a payment gateway. In a real system this would call
 * an external API (Razorpay, Stripe, etc.).
 */
public class PaymentProcessor {
 
    private static final Random RANDOM = new Random();
 
    /**
     * Simulates processing a payment.
     * 95% success rate to mimic real gateway behaviour.
     */
    public PaymentResult processPayment(double amount, PaymentMethod method,
                                         String paymentDetail) {
        System.out.println("\n  ⏳ Processing payment via " + method.getDisplay() + "...");
        simulateNetworkDelay();
 
        // Validate inputs
        if (amount <= 0) {
            return new PaymentResult(false, null, amount, method,
                    "Invalid amount: " + amount);
        }
        if (paymentDetail == null || paymentDetail.isBlank()) {
            return new PaymentResult(false, null, amount, method,
                    "Payment details cannot be empty");
        }
     // Simulate 5% random failure
        if (RANDOM.nextInt(100) < 5) {
            return new PaymentResult(false, null, amount, method,
                    "Transaction declined by bank. Please try again.");
        }
 
        String txnId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new PaymentResult(true, txnId, amount, method,
                "Payment successful");
    }
 
    /** Simulates UPI payment flow */
    public PaymentResult processUpi(double amount, String upiId) {
        System.out.println("  ⏳ Sending payment request to UPI ID: " + upiId + "...");
        simulateNetworkDelay();
        if (!upiId.contains("@")) {
            return new PaymentResult(false, null, amount, PaymentMethod.UPI,
                    "Invalid UPI ID format. Expected format: user@bank");
        }
        return processPayment(amount, PaymentMethod.UPI, upiId);
    }

    /** Simulates card payment flow */
    public PaymentResult processCard(double amount, PaymentMethod cardType,
                                      String maskedCard) {
        System.out.println("  ⏳ Verifying card ending in " +
                maskedCard.substring(maskedCard.length() - 4) + "...");
        simulateNetworkDelay();
        return processPayment(amount, cardType, maskedCard);
    }
 
    private void simulateNetworkDelay() {
        try { Thread.sleep(800 + RANDOM.nextInt(700)); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
 