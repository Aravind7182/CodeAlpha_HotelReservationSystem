package hotel.ui;
 
import hotel.model.*;
import hotel.payment.*;
import hotel.service.*;
import hotel.storage.FileStorageManager;
import hotel.util.ConsoleUtlis;
 
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
 
import static hotel.util.ConsoleUtlis.*;
 
public class HotelConsoleUI {
 
    private final RoomService roomService;
    private final ReservationService reservationService;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
 
    public HotelConsoleUI() {
        FileStorageManager storage = new FileStorageManager();
        roomService        = new RoomService(storage);
        reservationService = new ReservationService(roomService, storage);
    }
    public void start() {
        printBanner();
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Enter choice: ");
            System.out.println();
            switch (choice) {
                case 1  -> searchRooms();
                case 2  -> makeReservation();
                case 3  -> viewReservation();
                case 4  -> cancelReservation();
                case 5  -> processPayment();
                case 6  -> checkInOut();
                case 7  -> viewAllReservations();
                case 8  -> roomAvailabilitySummary();
                case 9  -> exportData();
                case 0  -> running = false;
                default -> warn("Invalid choice. Please try again.");
            }
        }
        success("Thank you for using Grand Azure Hotel System. Goodbye!");
    }
 
    // ─── Banner & Menus ───────────────────────────────────────────────────────
 
    private void printBanner() {
        System.out.println(CYAN + BOLD);
        System.out.println("  ╔════════════════════════════════════════════════════════╗");
        System.out.println("  ║                                                        ║");
        System.out.println("  ║       🏨  GRAND AZURE HOTEL  🏨                        ║");
        System.out.println("  ║          Reservation Management System                 ║");
        System.out.println("  ║                                                        ║");
        System.out.println("  ╚════════════════════════════════════════════════════════╝" + RESET);
        System.out.println();
    }
    private void printMainMenu() {
        divider();
        System.out.println(BOLD + "  MAIN MENU" + RESET);
        divider();
        System.out.println("  1. 🔍  Search Rooms");
        System.out.println("  2. 📝  Make a Reservation");
        System.out.println("  3. 📋  View Booking Details");
        System.out.println("  4. ❌  Cancel Reservation");
        System.out.println("  5. 💳  Process Payment");
        System.out.println("  6. 🔑  Check-In / Check-Out");
        System.out.println("  7. 📊  View All Reservations");
        System.out.println("  8. 📈  Room Availability Summary");
        System.out.println("  9. 💾  Export Reservations to File");
        System.out.println("  0. 🚪  Exit");
        divider();
    }
 
    // ─── Feature 1: Search Rooms ──────────────────────────────────────────────
 
    private void searchRooms() {
        printHeader("ROOM SEARCH");
        System.out.println("  Filter by Category:");
        System.out.println("  1. Standard  (₹1,500/night)");
        System.out.println("  2. Deluxe    (₹3,000/night)");
        System.out.println("  3. Suite     (₹6,000/night)");
        System.out.println("  4. All Categories");
 
        int catChoice = readInt("Select category (1-4): ");
        RoomCategory category = switch (catChoice) {
            case 1 -> RoomCategory.STANDARD;
            case 2 -> RoomCategory.DELUXE;
            case 3 -> RoomCategory.SUITE;
            default -> null;
        };
        String availStr = readLine("Show available rooms only? (y/n): ");
        boolean availOnly = availStr.equalsIgnoreCase("y");
 
        List<Room> results = roomService.searchRooms(category, availOnly);
        System.out.println();
        if (results.isEmpty()) {
            warn("No rooms found matching your criteria.");
        } else {
            printSectionHeader("Search Results (" + results.size() + " rooms)");
            System.out.println("  " + "-".repeat(70));
            System.out.printf("  %-8s %-7s %-10s %-12s %s%n",
                    "Room No.", "Floor", "Category", "Status", "Amenities");
            System.out.println("  " + "-".repeat(70));
            for (Room r : results) {
                String status = r.isAvailable()
                        ? GREEN + "AVAILABLE" + RESET
                        : RED   + "OCCUPIED " + RESET;
                System.out.printf("  %-8d %-7d %-10s %-20s %s%n",
                        r.getRoomNumber(), r.getFloor(),
                        r.getCategory().getDisplayName(), status, r.getAmenities());
            }
            System.out.println("  " + "-".repeat(70));
        }
        pause();
    }
 
    // ─── Feature 2: Make Reservation ─────────────────────────────────────────
 
    private void makeReservation() {
        printHeader("NEW RESERVATION");
        // Guest details
        printSectionHeader("Guest Information");
        String name    = readLine("Full Name        : ");
        String email   = readLine("Email Address    : ");
        String phone   = readLine("Phone Number     : ");
        String idProof = readLine("ID Proof (Aadhaar/Passport/PAN) : ");
 
        if (name.isBlank() || email.isBlank() || phone.isBlank()) {
            error("Name, email, and phone are required.");
            return;
        }
 
        String guestId = "G-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Guest guest = new Guest(guestId, name, email, phone, idProof);
 
        // Room selection
        printSectionHeader("Room Selection");
        List<Room> available = roomService.getAvailableRooms();
        if (available.isEmpty()) {
            warn("No rooms are currently available.");
            return;
        }
        System.out.println("  Available rooms:");
        available.forEach(r -> System.out.printf("  Room %-4d | Floor %d | %-8s | ₹%-7.2f/night | %s%n",
                r.getRoomNumber(), r.getFloor(),
                r.getCategory().getDisplayName(),
                r.getPricePerNight(), r.getAmenities()));
 
        int roomNo = readInt("Enter Room Number : ");
 
        // Dates
        printSectionHeader("Stay Dates  (format: dd-MM-yyyy)");
        LocalDate checkIn  = parseDate(readLine("Check-In  Date : "));
        LocalDate checkOut = parseDate(readLine("Check-Out Date : "));
        if (checkIn == null || checkOut == null) {
            error("Invalid date format. Use dd-MM-yyyy.");
            return;
        }

        try {
            Reservation reservation = reservationService.createReservation(
                    guest, roomNo, checkIn, checkOut);
            System.out.println();
            System.out.println(reservation.getSummary());
            success("Reservation created! Please proceed to payment (option 5).");
        } catch (Exception e) {
            error(e.getMessage());
        }
        pause();
    }
 
    // ─── Feature 3: View Booking Details ─────────────────────────────────────
 
    private void viewReservation() {
        printHeader("VIEW BOOKING DETAILS");
        System.out.println("  1. Search by Reservation ID");
        System.out.println("  2. Search by Guest Name");
        int choice = readInt("Select option: ");
 
        if (choice == 1) {
            String id = readLine("Reservation ID : ").toUpperCase();
            reservationService.getReservation(id).ifPresentOrElse(
                    r -> System.out.println(r.getSummary()),
                    ()  -> warn("Reservation not found: " + id)
            );
        } else {
            String name = readLine("Guest Name (partial) : ");
            List<Reservation> found = reservationService.getReservationsByGuest(name);
            if (found.isEmpty()) {
                warn("No reservations found for: " + name);
            } else {
                found.forEach(r -> System.out.println(r.getSummary()));
            }
        }
        pause();
    }
    
    // ─── Feature 4: Cancel Reservation ───────────────────────────────────────
    
    private void cancelReservation() {
        printHeader("CANCEL RESERVATION");
        String id = readLine("Reservation ID to cancel : ").toUpperCase();
        reservationService.getReservation(id).ifPresentOrElse(r -> {
            System.out.println(r.getSummary());
            String confirm = readLine("Confirm cancellation? (yes/no): ");
            if (confirm.equalsIgnoreCase("yes")) {
                try {
                    String msg = reservationService.cancelReservation(id);
                    success(msg);
                } catch (Exception e) {
                    error(e.getMessage());
                }
            } else {
                info("Cancellation aborted.");
            }
        }, () -> warn("Reservation not found: " + id));
        pause();
    }
    // ─── Feature 5: Process Payment ──────────────────────────────────────────
    
    private void processPayment() {
        printHeader("PAYMENT PROCESSING");
        String id = readLine("Reservation ID : ").toUpperCase();
 
        reservationService.getReservation(id).ifPresentOrElse(r -> {
            if (r.isPaid()) {
                warn("This reservation is already paid.");
                return;
            }
            System.out.printf("%n  Amount Due  : ₹%.2f%n", r.getTotalAmount());
            printSectionHeader("Select Payment Method");
            PaymentMethod[] methods = PaymentMethod.values();
            for (int i = 0; i < methods.length; i++)
                System.out.printf("  %d. %s%n", i + 1, methods[i].getDisplay());
 
            int methodChoice = readInt("Select method (1-" + methods.length + "): ") - 1;
            if (methodChoice < 0 || methodChoice >= methods.length) {
                error("Invalid selection.");
                return;
            }
            PaymentMethod method = methods[methodChoice];
            String detail = switch (method) {
                case UPI         -> readLine("Enter UPI ID (e.g. name@upi): ");
                case CREDIT_CARD -> readLine("Enter Card Number (last 4 shown on receipt): ");
                case DEBIT_CARD  -> readLine("Enter Debit Card Number: ");
                case NET_BANKING -> readLine("Enter Bank Name: ");
                case CASH        -> "CASH";
            };
 
            try {
                PaymentResult result = reservationService.processPayment(id, method, detail);
                System.out.println(result.getReceipt());
                if (result.isSuccess()) success("Payment recorded. You may now check in on arrival.");
                else error("Payment failed. Please try again.");
            } catch (Exception e) {
                error(e.getMessage());
            }
        }, () -> warn("Reservation not found: " + id));
        pause();
    }
    // ─── Feature 6: Check-In / Check-Out ─────────────────────────────────────
    
    private void checkInOut() {
        printHeader("CHECK-IN / CHECK-OUT");
        System.out.println("  1. Check-In");
        System.out.println("  2. Check-Out");
        int choice = readInt("Select option: ");
        String id = readLine("Reservation ID : ").toUpperCase();
        try {
            String msg = (choice == 1)
                    ? reservationService.checkIn(id)
                    : reservationService.checkOut(id);
            success(msg);
        } catch (Exception e) {
            error(e.getMessage());
        }
        pause();
    }
 // ─── Feature 7: All Reservations ─────────────────────────────────────────
    
    private void viewAllReservations() {
        printHeader("ALL RESERVATIONS");
        System.out.println("  1. All Reservations");
        System.out.println("  2. Active Only (Confirmed + Checked-In)");
        int filter = readInt("Select filter: ");
 
        List<Reservation> list = (filter == 2)
                ? reservationService.getActiveReservations()
                : reservationService.getAllReservations();
 
        if (list.isEmpty()) {
            warn("No reservations found.");
        } else {
            System.out.println();
            System.out.println("  " + "-".repeat(90));
            System.out.printf("  %-12s %-20s %-6s %-12s %-12s %-10s %-12s %-8s%n",
                    "ID", "Guest", "Room", "Check-In", "Check-Out", "Amount", "Status", "Paid");
            System.out.println("  " + "-".repeat(90));
            for (Reservation r : list) {
                System.out.printf("  %-12s %-20s %-6d %-12s %-12s ₹%-9.2f %-12s %-8s%n",
                        r.getReservationId(),
                        r.getGuest().getName().length() > 18
                                ? r.getGuest().getName().substring(0, 18) + ".."
                                : r.getGuest().getName(),
                        r.getRoom().getRoomNumber(),
                        r.getCheckIn(), r.getCheckOut(),
                        r.getTotalAmount(), r.getStatus(),
                        r.isPaid() ? GREEN + "YES" + RESET : RED + "NO" + RESET);
            }
            System.out.println("  " + "-".repeat(90));
            info("Total: " + list.size() + " reservation(s)");
        }
        pause();
    }
    // ─── Feature 8: Availability Summary ─────────────────────────────────────
    
    private void roomAvailabilitySummary() {
        printHeader("ROOM AVAILABILITY SUMMARY");
        var stats = roomService.getAvailabilityStats();
        int totalAvailable = stats.values().stream().mapToInt(Long::intValue).sum();
        int totalRooms     = roomService.getAllRooms().size();
 
        System.out.println();
        System.out.println("  " + "-".repeat(50));
        System.out.printf("  %-12s %-12s %-12s %s%n", "Category", "Available", "Occupied", "Total");
        System.out.println("  " + "-".repeat(50));
        for (RoomCategory cat : RoomCategory.values()) {
            long avail   = stats.getOrDefault(cat, 0L);
            long total   = roomService.getAllRooms().stream()
                    .filter(r -> r.getCategory() == cat).count();
            long occupied = total - avail;
            System.out.printf("  %-12s %-12d %-12d %d%n",
                    cat.getDisplayName(), avail, occupied, total);
        }
        System.out.println("  " + "-".repeat(50));
        System.out.printf("  %-12s %-12d %-12d %d%n",
                "TOTAL", totalAvailable, totalRooms - totalAvailable, totalRooms);
        System.out.println("  " + "-".repeat(50));
        pause();
    }
    // ─── Feature 9: Export ────────────────────────────────────────────────────
    
    private void exportData() {
        printHeader("EXPORT RESERVATIONS");
        if (reservationService.exportToText()) {
            success("Exported to data/reservations_export.txt");
        } else {
            error("Export failed. Check permissions.");
        }
        pause();
    }
 
    // ─── Helpers ──────────────────────────────────────────────────────────────
 
    private LocalDate parseDate(String input) {
        try {
            return LocalDate.parse(input, DATE_FMT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
 
 