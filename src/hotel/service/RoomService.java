package src.hotel.service;
 
import src.hotel.model.Room;
import src.hotel.model.RoomCategory;
import src.hotel.storage.FileStorageManager;
 
import java.util.*;
import java.util.stream.Collectors;
 
public class RoomService {
 
    private final Map<Integer, Room> rooms;
    private final FileStorageManager storage;
 
    public RoomService(FileStorageManager storage) {
        this.storage = storage;
        Map<Integer, Room> loaded = storage.loadRooms();
        if (loaded.isEmpty()) {
            rooms = initializeRooms();
            storage.saveRooms(rooms);
        } else {
            rooms = loaded;
        }
    }
    /** Seed 30 rooms across 3 categories and 5 floors. */
    private Map<Integer, Room> initializeRooms() {
        Map<Integer, Room> map = new LinkedHashMap<>();
        // Floor 1-2: Standard  (101-110, 201-210)
        for (int floor = 1; floor <= 2; floor++)
            for (int num = 1; num <= 5; num++) {
                int roomNo = floor * 100 + num;
                map.put(roomNo, new Room(roomNo, RoomCategory.STANDARD, floor));
            }
        // Floor 3-4: Deluxe    (301-305, 401-405)
        for (int floor = 3; floor <= 4; floor++)
            for (int num = 1; num <= 5; num++) {
                int roomNo = floor * 100 + num;
                map.put(roomNo, new Room(roomNo, RoomCategory.DELUXE, floor));
            }
        // Floor 5: Suite       (501-505)
        for (int num = 1; num <= 5; num++) {
            int roomNo = 500 + num;
            map.put(roomNo, new Room(roomNo, RoomCategory.SUITE, 5));
        }
        return map;
    }
 
    public Optional<Room> findRoom(int roomNumber) {
        return Optional.ofNullable(rooms.get(roomNumber));
    }
 
    public List<Room> getAllRooms() {
        return new ArrayList<>(rooms.values());
    }
    public List<Room> getAvailableRooms() {
        return rooms.values().stream()
                .filter(Room::isAvailable)
                .collect(Collectors.toList());
    }
 
    public List<Room> searchRooms(RoomCategory category, boolean availableOnly) {
        return rooms.values().stream()
                .filter(r -> (category == null || r.getCategory() == category))
                .filter(r -> !availableOnly || r.isAvailable())
                .sorted(Comparator.comparingInt(Room::getRoomNumber))
                .collect(Collectors.toList());
    }
 
    public void markOccupied(int roomNumber) {
        rooms.computeIfPresent(roomNumber, (k, r) -> { r.setAvailable(false); return r; });
        storage.saveRooms(rooms);
    }
 
    public void markAvailable(int roomNumber) {
        rooms.computeIfPresent(roomNumber, (k, r) -> { r.setAvailable(true); return r; });
        storage.saveRooms(rooms);
    }
    public Map<RoomCategory, Long> getAvailabilityStats() {
        Map<RoomCategory, Long> stats = new LinkedHashMap<>();
        for (RoomCategory cat : RoomCategory.values()) {
            long count = rooms.values().stream()
                    .filter(r -> r.getCategory() == cat && r.isAvailable())
                    .count();
            stats.put(cat, count);
        }
        return stats;
    }
 
    public void saveRooms() {
        storage.saveRooms(rooms);
    }
}
 