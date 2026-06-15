package com.lifelink.daoimpl;

import com.lifelink.dao.DonationHistoryDAO;
import com.lifelink.database.DBConnection;
import com.lifelink.model.DonationHistory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DonationHistoryDAOImpl implements DonationHistoryDAO {

    @Override
    public boolean create(DonationHistory history) {
        String sql = "INSERT INTO donation_history (donor_id, donation_date, units_donated, location) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, history.getDonorId());
            pstmt.setDate(2, Date.valueOf(history.getDonationDate()));
            pstmt.setInt(3, history.getUnitsDonated());
            pstmt.setString(4, history.getLocation());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        history.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error inserting donation history: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public DonationHistory readById(int id) {
        String sql = "SELECT * FROM donation_history WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDonationHistory(rs);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading donation history by id: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean update(DonationHistory history) {
        String sql = "UPDATE donation_history SET donor_id = ?, donation_date = ?, units_donated = ?, location = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, history.getDonorId());
            pstmt.setDate(2, Date.valueOf(history.getDonationDate()));
            pstmt.setInt(3, history.getUnitsDonated());
            pstmt.setString(4, history.getLocation());
            pstmt.setInt(5, history.getId());

            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error updating donation history: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM donation_history WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error deleting donation history: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<DonationHistory> readAll() {
        List<DonationHistory> list = new ArrayList<>();
        String sql = "SELECT * FROM donation_history";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToDonationHistory(rs));
            }
        } catch (Exception e) {
            System.err.println("Error reading all donation history: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Helper method to map a ResultSet row to a DonationHistory object.
     */
    private DonationHistory mapResultSetToDonationHistory(ResultSet rs) throws Exception {
        Date sqlDate = rs.getDate("donation_date");
        LocalDate donationDate = (sqlDate != null) ? sqlDate.toLocalDate() : null;
        return new DonationHistory(
                rs.getInt("id"),
                rs.getInt("donor_id"),
                donationDate,
                rs.getInt("units_donated"),
                rs.getString("location")
        );
    }
}
