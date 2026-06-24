package hotel.model;

import java.io.Serializable;

public class Room implements Serializable {
    private static final long serialVersionUID = 1L;
 
    private final int roomNumber;
    private final RoomCategory category;
    private boolean isAvailable;
    private final int floor;
    private final String amenities;
 
    public Room(int roomNumber, RoomCategory category, int floor) {
        this.roomNumber = roomNumber;
        this.category = category;
        this.floor = floor;
        this.isAvailable = true;
        this.amenities = buildAmenities(category);
    }
 
    private String buildAmenities(RoomCategory category) {
        return switch (category) {
            case STANDARD -> "WiFi, TV, AC, Hot Water";
            case DELUXE   -> "WiFi, Smart TV, AC, Mini-Bar, City View, Room Service";
            case SUITE    -> "WiFi, Smart TV, AC, Mini-Bar, Jacuzzi, Butler, Lounge, Sea View";
        };
    }
    public int getRoomNumber()       { return roomNumber; }
    public RoomCategory getCategory(){ return category; }
    public boolean isAvailable()     { return isAvailable; }
    public void setAvailable(boolean available) { this.isAvailable = available; }
    public int getFloor()            { return floor; }
    public String getAmenities()     { return amenities; }
    public double getPricePerNight() { return category.getPricePerNight(); }
 
    @Override
    public String toString() {
        return String.format("Room %-4d | Floor %d | %-8s | %-10s | %s",
                roomNumber, floor, category.getDisplayName(),
                isAvailable ? "AVAILABLE" : "OCCUPIED", amenities);
    }
}

