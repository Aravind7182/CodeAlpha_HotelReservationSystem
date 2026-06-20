package hotel.model;
 
public enum RoomCategory {
    STANDARD("Standard", 1500.00, "Basic amenities with comfortable bedding and TV"),
    DELUXE("Deluxe", 3000.00, "Spacious room with king bed, mini-bar, and city view"),
    SUITE("Suite", 6000.00, "Luxury suite with living area, jacuzzi, and butler service");
 
    private final String displayName;
    private final double pricePerNight;
    private final String description;
 
    RoomCategory(String displayName, double pricePerNight, String description) {
        this.displayName = displayName;
        this.pricePerNight = pricePerNight;
        this.description = description;
    }
 
    public String getDisplayName() { return displayName; }
    public double getPricePerNight() { return pricePerNight; }
    public String getDescription() { return description; }
 
    @Override
    public String toString() {
        return String.format("%s (₹%.2f/night) - %s", displayName, pricePerNight, description);
    }
}