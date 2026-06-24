package hotel.storage;
 
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

import hotel.model.Reservation;
import hotel.model.Room;
 
/**
 * Handles all file I/O using Java object serialisation.
 * Two files are maintained:
 *   data/rooms.dat         - room inventory
 *   data/reservations.dat  - all bookings
 */
public class FileStorageManager {
	 
    private static final Logger LOG = Logger.getLogger(FileStorageManager.class.getName());
    private static final String DATA_DIR   = "data/";
    private static final String ROOMS_FILE = DATA_DIR + "rooms.dat";
    private static final String RSVN_FILE  = DATA_DIR + "reservations.dat";
 
    public FileStorageManager() {
        try { Files.createDirectories(Paths.get(DATA_DIR)); }
        catch (IOException e) { LOG.warning("Could not create data directory: " + e.getMessage()); }
    }
 
    // ─── Rooms ────────────────────────────────────────────────────────────────
 
    @SuppressWarnings("unchecked")
    public Map<Integer, Room> loadRooms() {
        return loadObject(ROOMS_FILE, new HashMap<>());
    }
 
    public boolean saveRooms(Map<Integer, Room> rooms) {
        return saveObject(ROOMS_FILE, rooms);
    }
 
    // ─── Reservations ─────────────────────────────────────────────────────────
 
    @SuppressWarnings("unchecked")
    public Map<String, Reservation> loadReservations() {
        return loadObject(RSVN_FILE, new HashMap<>());
    }
 
    public boolean saveReservations(Map<String, Reservation> reservations) {
        return saveObject(RSVN_FILE, reservations);
    }
 // ─── Generic helpers ──────────────────────────────────────────────────────
    
    @SuppressWarnings("unchecked")
    private <T> T loadObject(String filePath, T defaultValue) {
        File f = new File(filePath);
        if (!f.exists()) return defaultValue;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LOG.warning("Failed to load " + filePath + ": " + e.getMessage());
            return defaultValue;
        }
    }
 
    private boolean saveObject(String filePath, Object obj) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(obj);
            return true;
        } catch (IOException e) {
            LOG.severe("Failed to save " + filePath + ": " + e.getMessage());
            return false;
        }
    }
    /** Exports all reservations to a human-readable text file for auditing. */
    public boolean exportReservationsToText(Map<String, Reservation> reservations) {
        String path = DATA_DIR + "reservations_export.txt";
        try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {
            pw.println("=".repeat(70));
            pw.println("  HOTEL RESERVATION EXPORT  —  Generated: " + new Date());
            pw.println("=".repeat(70));
            if (reservations.isEmpty()) {
                pw.println("  No reservations found.");
            } else {
                reservations.values().forEach(r -> {
                    pw.println(r.getSummary());
                    pw.println("-".repeat(70));
                });
            }
            pw.println("Total Reservations: " + reservations.size());
            return true;
        } catch (IOException e) {
            LOG.severe("Export failed: " + e.getMessage());
            return false;
        }
    }
}
 