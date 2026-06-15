package com.lifelink.daoimpl;

import com.lifelink.dao.DonorDAO;
import com.lifelink.database.DBConnection;
import com.lifelink.model.Donor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DonorDAOImpl implements DonorDAO {

    @Override
    public boolean create(Donor donor) {
        String sql = "INSERT INTO donors (user_id, name, age, weight, blood_group, city, last_donation_date, is_available) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, donor.getUserId());
            pstmt.setString(2, donor.getName());
            pstmt.setInt(3, donor.getAge());
            pstmt.setDouble(4, donor.getWeight());
            pstmt.setString(5, donor.getBloodGroup());
            pstmt.setString(6, donor.getCity());
            
            if (donor.getLastDonationDate() != null) {
                pstmt.setDate(7, Date.valueOf(donor.getLastDonationDate()));
            } else {
                pstmt.setNull(7, java.sql.Types.DATE);
            }
            pstmt.setBoolean(8, donor.isAvailable());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        donor.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error inserting donor: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Donor readById(int id) {
        String sql = "SELECT * FROM donors WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Date sqlDate = rs.getDate("last_donation_date");
                    LocalDate lastDonation = (sqlDate != null) ? sqlDate.toLocalDate() : null;
                    return new Donor(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("name"),
                            rs.getInt("age"),
                            rs.getDouble("weight"),
                            rs.getString("blood_group"),
                            rs.getString("city"),
                            lastDonation,
                            rs.getBoolean("is_available")
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading donor by id: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Donor readByUserId(int userId) {
        String sql = "SELECT * FROM donors WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Date sqlDate = rs.getDate("last_donation_date");
                    LocalDate lastDonation = (sqlDate != null) ? sqlDate.toLocalDate() : null;
                    return new Donor(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("name"),
                            rs.getInt("age"),
                            rs.getDouble("weight"),
                            rs.getString("blood_group"),
                            rs.getString("city"),
                            lastDonation,
                            rs.getBoolean("is_available")
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading donor by user id: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean update(Donor donor) {
        String sql = "UPDATE donors SET name = ?, age = ?, weight = ?, blood_group = ?, city = ?, last_donation_date = ?, is_available = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, donor.getName());
            pstmt.setInt(2, donor.getAge());
            pstmt.setDouble(3, donor.getWeight());
            pstmt.setString(4, donor.getBloodGroup());
            pstmt.setString(5, donor.getCity());
            if (donor.getLastDonationDate() != null) {
                pstmt.setDate(6, Date.valueOf(donor.getLastDonationDate()));
            } else {
                pstmt.setNull(6, java.sql.Types.DATE);
            }
            pstmt.setBoolean(7, donor.isAvailable());
            pstmt.setInt(8, donor.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error updating donor: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM donors WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error deleting donor: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Donor> readAll() {
        List<Donor> list = new ArrayList<>();
        String sql = "SELECT * FROM donors";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Date sqlDate = rs.getDate("last_donation_date");
                LocalDate lastDonation = (sqlDate != null) ? sqlDate.toLocalDate() : null;
                list.add(new Donor(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getDouble("weight"),
                        rs.getString("blood_group"),
                        rs.getString("city"),
                        lastDonation,
                        rs.getBoolean("is_available")
                ));
            }
        } catch (Exception e) {
            System.err.println("Error reading all donors: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
}
