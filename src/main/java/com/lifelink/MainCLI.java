package com.lifelink;

import com.lifelink.database.DBConnection;
import com.lifelink.model.*;
import com.lifelink.dao.*;
import com.lifelink.daoimpl.*;
import com.lifelink.service.*;
import com.lifelink.util.Constants;
import com.lifelink.util.Validator;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class MainCLI {
    private static final Scanner scanner = new Scanner(System.in);
    private static final AuthService authService = new AuthService();
    private static final EligibilityService eligibilityService = new EligibilityService();
    private static final AvailabilityService availabilityService = new AvailabilityService();
    private static final DashboardService dashboardService = new DashboardService();

    private static final UserDAO userDAO = new UserDAOImpl();
    private static final DonorDAO donorDAO = new DonorDAOImpl();
    private static final HospitalDAO hospitalDAO = new HospitalDAOImpl();
    private static final RequestDAO requestDAO = new RequestDAOImpl();
    private static final DonationHistoryDAO donationHistoryDAO = new DonationHistoryDAOImpl();

    public static void main(String[] args) {
        System.out.println("Testing Database Connection...");
        if (DBConnection.testConnection()) {
            System.out.println("Database Connection Succeeded.");
        } else {
            System.err.println("Database Connection Failed! Please check your credentials and ensure MySQL is running.");
        }

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readIntInput("Enter your choice: ");
            switch (choice) {
                case 1:
                    handleLogin();
                    break;
                case 2:
                    handleDonorRegistration();
                    break;
                case 3:
                    handleHospitalRegistration();
                    break;
                case 4:
                    System.out.println("\nThank you for using LifeLink. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please choose a number from 1 to 4.");
            }
        }
    }

    private static void printMainMenu() {
        System.out.println("\n=================================");
        System.out.println("    LIFELINK BLOOD MANAGEMENT    ");
        System.out.println("=================================");
        System.out.println("1. Log In");
        System.out.println("2. Register as a Donor");
        System.out.println("3. Register as a Hospital");
        System.out.println("4. Exit");
        System.out.println("=================================");
    }

    private static int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid numeric input. Please try again.");
            }
        }
    }

    private static double readDoubleInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine().trim();
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid numeric input. Please try again.");
            }
        }
    }

    private static void handleLogin() {
        System.out.println("\n--- Log In ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        User user = authService.login(username, password);
        if (user != null) {
            System.out.println("Welcome, " + user.getUsername() + "! Login successful.");
            switch (user.getRole()) {
                case Constants.ROLE_ADMIN:
                    runAdminDashboard(user);
                    break;
                case Constants.ROLE_DONOR:
                    runDonorDashboard(user);
                    break;
                case Constants.ROLE_HOSPITAL:
                    runHospitalDashboard(user);
                    break;
            }
        } else {
            System.out.println("Error: Invalid username or password.");
        }
    }

    private static void handleDonorRegistration() {
        System.out.println("\n--- Donor Registration ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password (min 6 characters): ");
        String password = scanner.nextLine().trim();
        if (!Validator.isValidPassword(password)) {
            System.out.println("Error: Password must be at least 6 characters long.");
            return;
        }

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        if (!Validator.isValidEmail(email)) {
            System.out.println("Error: Invalid email format.");
            return;
        }

        System.out.print("Full Name: ");
        String name = scanner.nextLine().trim();
        int age = readIntInput("Age: ");
        if (age < 0) {
            System.out.println("Error: Age cannot be negative.");
            return;
        }

        System.out.print("Blood Group (e.g. A+, O-, AB+): ");
        String bloodGroup = scanner.nextLine().trim().toUpperCase();
        if (!Constants.isValidBloodGroup(bloodGroup)) {
            System.out.println("Error: Invalid blood group. Must be one of A+, A-, B+, B-, AB+, AB-, O+, O-.");
            return;
        }

        double weight = readDoubleInput("Weight (kg): ");
        if (weight <= 0) {
            System.out.println("Error: Weight must be positive.");
            return;
        }

        System.out.print("City: ");
        String city = scanner.nextLine().trim();
        if (city.isEmpty()) {
            System.out.println("Error: City cannot be empty.");
            return;
        }

        User user = authService.registerDonor(username, password, email, name, age, weight, bloodGroup, city);
        if (user != null) {
            System.out.println("Donor registered successfully! You can now log in.");
        } else {
            System.out.println("Error: Registration failed.");
        }
    }

    private static void handleHospitalRegistration() {
        System.out.println("\n--- Hospital Registration ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password (min 6 characters): ");
        String password = scanner.nextLine().trim();
        if (!Validator.isValidPassword(password)) {
            System.out.println("Error: Password must be at least 6 characters long.");
            return;
        }

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        if (!Validator.isValidEmail(email)) {
            System.out.println("Error: Invalid email format.");
            return;
        }

        System.out.print("Hospital Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Address: ");
        String address = scanner.nextLine().trim();
        System.out.print("Contact Number: ");
        String contact = scanner.nextLine().trim();
        if (!Validator.isValidPhone(contact)) {
            System.out.println("Error: Invalid phone number format.");
            return;
        }

        User user = authService.registerHospital(username, password, email, name, address, contact);
        if (user != null) {
            System.out.println("Hospital registered successfully! You can now log in.");
        } else {
            System.out.println("Error: Registration failed.");
        }
    }

    // ==========================================
    // DONOR ACTIONS
    // ==========================================
    private static void runDonorDashboard(User user) {
        Donor donor = donorDAO.readByUserId(user.getId());
        if (donor == null) {
            System.out.println("Error: Donor profile not found.");
            return;
        }

        boolean active = true;
        while (active) {
            // Re-evaluate eligibility state
            eligibilityService.checkAndUpdateEligibility(donor);
            donor = donorDAO.readById(donor.getId()); // Refresh from db

            System.out.println("\n=================================");
            System.out.println("         DONOR DASHBOARD         ");
            System.out.println("=================================");
            System.out.println("Welcome, " + donor.getName() + " (" + donor.getBloodGroup() + ")");
            System.out.println("Eligibility status: " + (donor.isAvailable() ? "ELIGIBLE" : "INELIGIBLE"));
            System.out.println("---------------------------------");
            System.out.println("1. View Profile");
            System.out.println("2. Update Profile");
            System.out.println("3. Check Detailed Eligibility");
            System.out.println("4. Record a Donation");
            System.out.println("5. View Donation History");
            System.out.println("6. Check Blood Stock Levels");
            System.out.println("7. Log Out");
            System.out.println("=================================");

            int choice = readIntInput("Select an option: ");
            switch (choice) {
                case 1:
                    System.out.println("\n--- Profile Details ---");
                    System.out.println("Donor ID: " + donor.getId());
                    System.out.println("Name: " + donor.getName());
                    System.out.println("Age: " + donor.getAge());
                    System.out.println("Blood Group: " + donor.getBloodGroup());
                    System.out.println("Last Donation: " + (donor.getLastDonationDate() != null ? donor.getLastDonationDate() : "None"));
                    System.out.println("Email: " + user.getEmail());
                    break;
                case 2:
                    System.out.println("\n--- Update Profile ---");
                    System.out.print("New Name (leave blank to keep current): ");
                    String newName = scanner.nextLine().trim();
                    if (!newName.isEmpty()) {
                        donor.setName(newName);
                    }
                    System.out.print("New Age (enter -1 to keep current): ");
                    int newAge = readIntInput("");
                    if (newAge != -1) {
                        donor.setAge(newAge);
                    }
                    System.out.print("New Blood Group (leave blank to keep current): ");
                    String newBg = scanner.nextLine().trim().toUpperCase();
                    if (!newBg.isEmpty()) {
                        if (Constants.isValidBloodGroup(newBg)) {
                            donor.setBloodGroup(newBg);
                        } else {
                            System.out.println("Invalid blood group. Unchanged.");
                        }
                    }
                    if (donorDAO.update(donor)) {
                        System.out.println("Profile updated successfully!");
                    } else {
                        System.out.println("Failed to update profile.");
                    }
                    break;
                case 3:
                    System.out.println("\n--- Eligibility Details ---");
                    String reason = eligibilityService.getEligibilityReason(donor);
                    System.out.println("Status: " + (donor.isAvailable() ? "ELIGIBLE" : "INELIGIBLE"));
                    System.out.println("Details: " + reason);
                    break;
                case 4:
                    if (!donor.isAvailable()) {
                        System.out.println("\nError: You are not eligible to donate. Reason: " + eligibilityService.getEligibilityReason(donor));
                        break;
                    }
                    System.out.println("\n--- Record a Donation ---");
                    int units = readIntInput("Units Donated (default 1): ");
                    if (units <= 0) units = 1;
                    System.out.print("Donation Location: ");
                    String location = scanner.nextLine().trim();
                    if (location.isEmpty()) location = "General Blood Bank";

                    System.out.print("Donation Date (YYYY-MM-DD, leave blank for Today): ");
                    String dateInput = scanner.nextLine().trim();
                    LocalDate donationDate = LocalDate.now();
                    if (!dateInput.isEmpty()) {
                        try {
                            donationDate = LocalDate.parse(dateInput);
                            if (donationDate.isAfter(LocalDate.now())) {
                                System.out.println("Error: Donation date cannot be in the future.");
                                break;
                            }
                        } catch (DateTimeParseException e) {
                            System.out.println("Invalid date format. Using current date.");
                        }
                    }

                    DonationHistory donation = new DonationHistory(donor.getId(), donationDate, units, location);
                    if (donationHistoryDAO.create(donation)) {
                        donor.setLastDonationDate(donationDate);
                        donorDAO.update(donor);
                        System.out.println("Donation recorded successfully! Thank you for your donation.");
                    } else {
                        System.out.println("Failed to record donation.");
                    }
                    break;
                case 5:
                    System.out.println("\n--- Donation History ---");
                    final int donorId = donor.getId();
                    List<DonationHistory> history = donationHistoryDAO.readAll().stream()
                            .filter(h -> h.getDonorId() == donorId)
                            .toList();
                    if (history.isEmpty()) {
                        System.out.println("No donations found.");
                    } else {
                        System.out.printf("%-10s | %-12s | %-10s | %-20s\n", "Record ID", "Date", "Units", "Location");
                        System.out.println("------------------------------------------------------------");
                        for (DonationHistory h : history) {
                            System.out.printf("%-10d | %-12s | %-10d | %-20s\n", h.getId(), h.getDonationDate(), h.getUnitsDonated(), h.getLocation());
                        }
                    }
                    break;
                case 6:
                    printStockLevels();
                    break;
                case 7:
                    System.out.println("Logging out...");
                    active = false;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    // ==========================================
    // HOSPITAL ACTIONS
    // ==========================================
    private static void runHospitalDashboard(User user) {
        Hospital hospital = hospitalDAO.readAll().stream()
                .filter(h -> h.getEmail().equalsIgnoreCase(user.getEmail()))
                .findFirst()
                .orElse(null);
        if (hospital == null) {
            System.out.println("Error: Hospital profile not found.");
            return;
        }

        boolean active = true;
        while (active) {
            System.out.println("\n=================================");
            System.out.println("       HOSPITAL DASHBOARD        ");
            System.out.println("=================================");
            System.out.println("Welcome, " + hospital.getName());
            System.out.println("---------------------------------");
            System.out.println("1. View Profile");
            System.out.println("2. Update Profile");
            System.out.println("3. Submit a Blood Request");
            System.out.println("4. View Request History");
            System.out.println("5. Check Blood Stock Levels");
            System.out.println("6. Log Out");
            System.out.println("=================================");

            int choice = readIntInput("Select an option: ");
            switch (choice) {
                case 1:
                    System.out.println("\n--- Hospital Details ---");
                    System.out.println("Hospital ID: " + hospital.getId());
                    System.out.println("Name: " + hospital.getName());
                    System.out.println("Address: " + hospital.getAddress());
                    System.out.println("Contact No: " + hospital.getContactNo());
                    System.out.println("Email: " + user.getEmail());
                    break;
                case 2:
                    System.out.println("\n--- Update Hospital Profile ---");
                    System.out.print("New Name (leave blank to keep current): ");
                    String newName = scanner.nextLine().trim();
                    if (!newName.isEmpty()) {
                        hospital.setName(newName);
                    }
                    System.out.print("New Address (leave blank to keep current): ");
                    String newAddress = scanner.nextLine().trim();
                    if (!newAddress.isEmpty()) {
                        hospital.setAddress(newAddress);
                    }
                    System.out.print("New Contact No (leave blank to keep current): ");
                    String newContact = scanner.nextLine().trim();
                    if (!newContact.isEmpty()) {
                        if (Validator.isValidPhone(newContact)) {
                            hospital.setContactNo(newContact);
                        } else {
                            System.out.println("Invalid contact format. Unchanged.");
                        }
                    }
                    if (hospitalDAO.update(hospital)) {
                        System.out.println("Hospital details updated successfully!");
                    } else {
                        System.out.println("Failed to update hospital details.");
                    }
                    break;
                case 3:
                    System.out.println("\n--- Submit a Blood Request ---");
                    System.out.print("Blood Group Required (e.g. A+, O-, AB+): ");
                    String bloodGroup = scanner.nextLine().trim().toUpperCase();
                    if (!Constants.isValidBloodGroup(bloodGroup)) {
                        System.out.println("Error: Invalid blood group.");
                        break;
                    }
                    int units = readIntInput("Units Requested: ");
                    if (units <= 0) {
                        System.out.println("Error: Units requested must be greater than zero.");
                        break;
                    }

                    int currentStock = availabilityService.getBloodStock(bloodGroup);
                    System.out.println("Current Available Stock of " + bloodGroup + ": " + currentStock + " units.");
                    if (currentStock < units) {
                        System.out.println("Note: The request exceeds current stock levels. It can be logged but might remain pending.");
                    }

                    System.out.print("Priority (Critical, High, Normal): ");
                    String priority = scanner.nextLine().trim();
                    if (priority.isEmpty()) {
                        priority = "Normal";
                    }

                    BloodRequest request = new BloodRequest(null, hospital.getId(), bloodGroup, units, priority, LocalDate.now(), "PENDING");
                    if (requestDAO.create(request)) {
                        System.out.println("Blood request submitted successfully! Request ID: " + request.getId());
                    } else {
                        System.out.println("Failed to log request.");
                    }
                    break;
                case 4:
                    System.out.println("\n--- Blood Request History ---");
                    List<BloodRequest> requests = requestDAO.readAll().stream()
                            .filter(r -> r.getHospitalId() != null && r.getHospitalId() == hospital.getId())
                            .toList();
                    if (requests.isEmpty()) {
                        System.out.println("No blood requests logged.");
                    } else {
                        System.out.printf("%-12s | %-12s | %-12s | %-15s | %-10s\n", "Request ID", "Blood Group", "Units Req.", "Date Logged", "Status");
                        System.out.println("-------------------------------------------------------------------------");
                        for (BloodRequest r : requests) {
                            System.out.printf("%-12d | %-12s | %-12d | %-15s | %-10s\n", r.getId(), r.getBloodGroup(), r.getUnitsRequested(), r.getRequestDate(), r.getStatus());
                        }
                    }
                    break;
                case 5:
                    printStockLevels();
                    break;
                case 6:
                    System.out.println("Logging out...");
                    active = false;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    // ==========================================
    // ADMIN ACTIONS
    // ==========================================
    private static void runAdminDashboard(User user) {
        boolean active = true;
        while (active) {
            System.out.println("\n=================================");
            System.out.println("         ADMIN DASHBOARD         ");
            System.out.println("=================================");
            System.out.println("1. System Summary Metrics");
            System.out.println("2. View All Registered Donors");
            System.out.println("3. View All Registered Hospitals");
            System.out.println("4. View All Blood Requests");
            System.out.println("5. Manage Pending Requests (Approve/Reject)");
            System.out.println("6. View System Donation Logs");
            System.out.println("7. Log Out");
            System.out.println("=================================");

            int choice = readIntInput("Select an option: ");
            switch (choice) {
                case 1:
                    System.out.println("\n--- System Metrics ---");
                    Map<String, Object> metrics = dashboardService.getAdminMetrics();
                    System.out.println("Total Donors Registered: " + metrics.get("totalDonors"));
                    System.out.println("Total Hospitals Registered: " + metrics.get("totalHospitals"));
                    System.out.println("Total Blood Request Submissions: " + metrics.get("totalRequests"));
                    System.out.println(" - Pending Requests: " + metrics.get("pendingRequests"));
                    System.out.println(" - Approved/Completed Requests: " + metrics.get("approvedRequests"));
                    System.out.println(" - Rejected Requests: " + metrics.get("rejectedRequests"));
                    System.out.println("Total Donation Log Entries: " + metrics.get("totalDonationRecords"));
                    System.out.println("Total Blood Units Donated (all-time): " + metrics.get("totalUnitsDonated"));
                    break;
                case 2:
                    System.out.println("\n--- Registered Donors ---");
                    List<Donor> donors = donorDAO.readAll();
                    if (donors.isEmpty()) {
                        System.out.println("No donors found.");
                    } else {
                        System.out.printf("%-10s | %-20s | %-5s | %-12s | %-12s | %-10s\n", "ID", "Name", "Age", "Blood Group", "Last Donated", "Status");
                        System.out.println("----------------------------------------------------------------------------------");
                        for (Donor d : donors) {
                            System.out.printf("%-10d | %-20s | %-5d | %-12s | %-12s | %-10s\n",
                                    d.getId(), d.getName(), d.getAge(), d.getBloodGroup(),
                                    (d.getLastDonationDate() != null ? d.getLastDonationDate() : "None"), (d.isAvailable() ? "ELIGIBLE" : "INELIGIBLE"));
                        }
                    }
                    break;
                case 3:
                    System.out.println("\n--- Registered Hospitals ---");
                    List<Hospital> hospitals = hospitalDAO.readAll();
                    if (hospitals.isEmpty()) {
                        System.out.println("No hospitals found.");
                    } else {
                        System.out.printf("%-10s | %-25s | %-25s | %-15s\n", "ID", "Name", "Address", "Contact");
                        System.out.println("----------------------------------------------------------------------------------");
                        for (Hospital h : hospitals) {
                            System.out.printf("%-10d | %-25s | %-25s | %-15s\n", h.getId(), h.getName(), h.getAddress(), h.getContactNo());
                        }
                    }
                    break;
                case 4:
                    System.out.println("\n--- All Blood Requests ---");
                    List<BloodRequest> requests = requestDAO.readAll();
                    if (requests.isEmpty()) {
                        System.out.println("No blood requests found.");
                    } else {
                        System.out.printf("%-10s | %-12s | %-12s | %-10s | %-15s | %-12s\n", "Req. ID", "Hospital ID", "Blood Group", "Units Req.", "Request Date", "Status");
                        System.out.println("----------------------------------------------------------------------------------");
                        for (BloodRequest r : requests) {
                            System.out.printf("%-10d | %-12d | %-12s | %-10d | %-15s | %-12s\n",
                                    r.getId(), r.getHospitalId(), r.getBloodGroup(), r.getUnitsRequested(), r.getRequestDate(), r.getStatus());
                        }
                    }
                    break;
                case 5:
                    System.out.println("\n--- Manage Blood Requests ---");
                    List<BloodRequest> reqs = requestDAO.readAll();
                    boolean hasPending = false;
                    for (BloodRequest r : reqs) {
                        if (r.getStatus().equalsIgnoreCase("PENDING")) {
                            System.out.printf("Pending Request - ID: %d | Hospital ID: %d | Blood: %s | Units: %d\n",
                                    r.getId(), r.getHospitalId(), r.getBloodGroup(), r.getUnitsRequested());
                            hasPending = true;
                        }
                    }
                    if (!hasPending) {
                        System.out.println("No pending blood requests available.");
                        break;
                    }

                    int reqId = readIntInput("\nEnter Request ID to manage (or -1 to cancel): ");
                    if (reqId == -1) break;

                    BloodRequest targetReq = requestDAO.readById(reqId);
                    if (targetReq == null || !targetReq.getStatus().equalsIgnoreCase("PENDING")) {
                        System.out.println("Invalid Request ID or request is not pending.");
                        break;
                    }

                    System.out.println("1. Approve Request");
                    System.out.println("2. Reject Request");
                    int action = readIntInput("Select action: ");
                    if (action == 1) {
                        // Check if stock is sufficient before completing or approving
                        int stock = availabilityService.getBloodStock(targetReq.getBloodGroup());
                        if (stock < targetReq.getUnitsRequested()) {
                            System.out.println("Warning: Insufficient stock. Currently available: " + stock + " units.");
                            System.out.print("Approve anyway? (Y/N): ");
                            String confirm = scanner.nextLine().trim().toUpperCase();
                            if (!confirm.equals("Y")) {
                                System.out.println("Approval cancelled.");
                                break;
                            }
                        }
                        targetReq.setStatus("APPROVED");
                        if (requestDAO.update(targetReq)) {
                            System.out.println("Request Approved successfully.");
                        } else {
                            System.out.println("Failed to update request status.");
                        }
                    } else if (action == 2) {
                        targetReq.setStatus("REJECTED");
                        if (requestDAO.update(targetReq)) {
                            System.out.println("Request Rejected successfully.");
                        } else {
                            System.out.println("Failed to update request status.");
                        }
                    } else {
                        System.out.println("Invalid action.");
                    }
                    break;
                case 6:
                    System.out.println("\n--- System-wide Donation Logs ---");
                    List<DonationHistory> donations = donationHistoryDAO.readAll();
                    if (donations.isEmpty()) {
                        System.out.println("No donation log entries found.");
                    } else {
                        System.out.printf("%-10s | %-10s | %-15s | %-10s | %-20s\n", "Log ID", "Donor ID", "Donation Date", "Units", "Location");
                        System.out.println("----------------------------------------------------------------------------------");
                        for (DonationHistory d : donations) {
                            System.out.printf("%-10d | %-10d | %-15s | %-10d | %-20s\n",
                                    d.getId(), d.getDonorId(), d.getDonationDate(), d.getUnitsDonated(), d.getLocation());
                        }
                    }
                    break;
                case 7:
                    System.out.println("Logging out...");
                    active = false;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private static void printStockLevels() {
        System.out.println("\n--- Blood Stock Levels ---");
        Map<String, Integer> stock = availabilityService.getAllBloodStock();
        System.out.printf("%-15s | %-10s\n", "Blood Group", "Units Available");
        System.out.println("---------------------------------");
        for (String bg : Constants.BLOOD_GROUPS) {
            System.out.printf("%-15s | %-10d\n", bg, stock.get(bg));
        }
    }
}
