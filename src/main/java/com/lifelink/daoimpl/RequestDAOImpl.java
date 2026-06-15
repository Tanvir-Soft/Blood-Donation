package com.lifelink.daoimpl;

import com.lifelink.dao.RequestDAO;
import com.lifelink.database.DBConnection;
import com.lifelink.model.BloodRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RequestDAOImpl implements RequestDAO {

    @Override
    public boolean create(BloodRequest request) {
        String sql = "INSERT INTO blood_requests (seeker_id, hospital_id, blood_group, units_requested, priority, request_date, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // seeker_id can be null
            if (request.getSeekerId() != null) {
                pstmt.setInt(1, request.getSeekerId());
            } else {
                pstmt.setNull(1, java.sql.Types.INTEGER);
            }

            // hospital_id can be null
            if (request.getHospitalId() != null) {
                pstmt.setInt(2, request.getHospitalId());
            } else {
                pstmt.setNull(2, java.sql.Types.INTEGER);
            }

            pstmt.setString(3, request.getBloodGroup());
            pstmt.setInt(4, request.getUnitsRequested());
            pstmt.setString(5, request.getPriority());
            pstmt.setDate(6, Date.valueOf(request.getRequestDate()));
            pstmt.setString(7, request.getStatus());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        request.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error inserting blood request: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public BloodRequest readById(int id) {
        String sql = "SELECT * FROM blood_requests WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBloodRequest(rs);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading blood request by id: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean update(BloodRequest request) {
        String sql = "UPDATE blood_requests SET seeker_id = ?, hospital_id = ?, blood_group = ?, units_requested = ?, priority = ?, request_date = ?, status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (request.getSeekerId() != null) {
                pstmt.setInt(1, request.getSeekerId());
            } else {
                pstmt.setNull(1, java.sql.Types.INTEGER);
            }

            if (request.getHospitalId() != null) {
                pstmt.setInt(2, request.getHospitalId());
            } else {
                pstmt.setNull(2, java.sql.Types.INTEGER);
            }

            pstmt.setString(3, request.getBloodGroup());
            pstmt.setInt(4, request.getUnitsRequested());
            pstmt.setString(5, request.getPriority());
            pstmt.setDate(6, Date.valueOf(request.getRequestDate()));
            pstmt.setString(7, request.getStatus());
            pstmt.setInt(8, request.getId());

            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error updating blood request: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM blood_requests WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error deleting blood request: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<BloodRequest> readAll() {
        List<BloodRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM blood_requests";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToBloodRequest(rs));
            }
        } catch (Exception e) {
            System.err.println("Error reading all blood requests: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Helper method to map a ResultSet row to a BloodRequest object.
     * Avoids code duplication across read methods.
     */
    private BloodRequest mapResultSetToBloodRequest(ResultSet rs) throws Exception {
        int seekerId = rs.getInt("seeker_id");
        Integer seekerIdObj = rs.wasNull() ? null : seekerId;

        int hospitalId = rs.getInt("hospital_id");
        Integer hospitalIdObj = rs.wasNull() ? null : hospitalId;

        Date sqlDate = rs.getDate("request_date");
        LocalDate requestDate = (sqlDate != null) ? sqlDate.toLocalDate() : null;

        return new BloodRequest(
                rs.getInt("id"),
                seekerIdObj,
                hospitalIdObj,
                rs.getString("blood_group"),
                rs.getInt("units_requested"),
                rs.getString("priority"),
                requestDate,
                rs.getString("status")
        );
    }
}
