package com.lifelink.daoimpl;

import com.lifelink.dao.HospitalDAO;
import com.lifelink.database.DBConnection;
import com.lifelink.model.Hospital;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class HospitalDAOImpl implements HospitalDAO {

    @Override
    public boolean create(Hospital hospital) {
        String sql = "INSERT INTO hospitals (name, address, contact_no, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, hospital.getName());
            pstmt.setString(2, hospital.getAddress());
            pstmt.setString(3, hospital.getContactNo());
            pstmt.setString(4, hospital.getEmail());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        hospital.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error inserting hospital: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Hospital readById(int id) {
        String sql = "SELECT * FROM hospitals WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Hospital(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("address"),
                            rs.getString("contact_no"),
                            rs.getString("email")
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading hospital by id: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean update(Hospital hospital) {
        String sql = "UPDATE hospitals SET name = ?, address = ?, contact_no = ?, email = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, hospital.getName());
            pstmt.setString(2, hospital.getAddress());
            pstmt.setString(3, hospital.getContactNo());
            pstmt.setString(4, hospital.getEmail());
            pstmt.setInt(5, hospital.getId());

            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error updating hospital: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM hospitals WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error deleting hospital: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Hospital> readAll() {
        List<Hospital> list = new ArrayList<>();
        String sql = "SELECT * FROM hospitals";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Hospital(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("contact_no"),
                        rs.getString("email")
                ));
            }
        } catch (Exception e) {
            System.err.println("Error reading all hospitals: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
}
