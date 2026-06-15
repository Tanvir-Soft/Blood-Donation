package com.lifelink.service;

import com.lifelink.dao.UserDAO;
import com.lifelink.dao.DonorDAO;
import com.lifelink.dao.HospitalDAO;
import com.lifelink.daoimpl.UserDAOImpl;
import com.lifelink.daoimpl.DonorDAOImpl;
import com.lifelink.daoimpl.HospitalDAOImpl;
import com.lifelink.model.User;
import com.lifelink.model.Donor;
import com.lifelink.model.Hospital;

import java.security.MessageDigest;

/**
 * Handles user registration (Donor, Hospital, Seeker) and login authentication.
 * Passwords are hashed with SHA-256 before storage.
 */
public class AuthService {
    private final UserDAO userDAO;
    private final DonorDAO donorDAO;
    private final HospitalDAO hospitalDAO;

    public AuthService() {
        this.userDAO = new UserDAOImpl();
        this.donorDAO = new DonorDAOImpl();
        this.hospitalDAO = new HospitalDAOImpl();
    }

    public AuthService(UserDAO userDAO, DonorDAO donorDAO, HospitalDAO hospitalDAO) {
        this.userDAO = userDAO;
        this.donorDAO = donorDAO;
        this.hospitalDAO = hospitalDAO;
    }

    /**
     * Registers a new donor user account and a linked donor profile.
     */
    public User registerDonor(String username, String password, String email,
                              String name, int age, double weight, String bloodGroup, String city) {
        if (userDAO.readByUsername(username) != null) {
            System.err.println("Username already exists.");
            return null;
        }
        String hashedPassword = hashPassword(password);
        User user = new User(username, hashedPassword, email, "DONOR");
        if (userDAO.create(user)) {
            boolean isEligibleAge = (age >= 18 && age <= 60);
            Donor donor = new Donor(user.getId(), name, age, weight, bloodGroup, city, null, isEligibleAge);
            if (donorDAO.create(donor)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Registers a new hospital user account and a linked hospital profile.
     */
    public User registerHospital(String username, String password, String email,
                                 String hospitalName, String address, String contactNo) {
        if (userDAO.readByUsername(username) != null) {
            System.err.println("Username already exists.");
            return null;
        }
        String hashedPassword = hashPassword(password);
        User user = new User(username, hashedPassword, email, "HOSPITAL");
        if (userDAO.create(user)) {
            Hospital hospital = new Hospital(hospitalName, address, contactNo, email);
            if (hospitalDAO.create(hospital)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Registers a new seeker (blood requester) user account.
     */
    public User registerSeeker(String username, String password, String email) {
        if (userDAO.readByUsername(username) != null) {
            System.err.println("Username already exists.");
            return null;
        }
        String hashedPassword = hashPassword(password);
        User user = new User(username, hashedPassword, email, "SEEKER");
        if (userDAO.create(user)) {
            return user;
        }
        return null;
    }

    /**
     * Authenticates a user by username and password.
     */
    public User login(String username, String password) {
        User user = userDAO.readByUsername(username);
        if (user != null) {
            String hashedInput = hashPassword(password);
            if (user.getPassword().equals(hashedInput)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Hashes a password string using SHA-256 and returns the hex representation.
     */
    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}
