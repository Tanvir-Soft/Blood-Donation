package com.lifelink.service;

import com.lifelink.dao.DonationHistoryDAO;
import com.lifelink.dao.DonorDAO;
import com.lifelink.dao.HospitalDAO;
import com.lifelink.dao.RequestDAO;
import com.lifelink.daoimpl.DonationHistoryDAOImpl;
import com.lifelink.daoimpl.DonorDAOImpl;
import com.lifelink.daoimpl.HospitalDAOImpl;
import com.lifelink.daoimpl.RequestDAOImpl;
import com.lifelink.model.BloodRequest;
import com.lifelink.model.DonationHistory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardService {
    private final DonorDAO donorDAO;
    private final HospitalDAO hospitalDAO;
    private final RequestDAO requestDAO;
    private final DonationHistoryDAO donationHistoryDAO;

    public DashboardService() {
        this.donorDAO = new DonorDAOImpl();
        this.hospitalDAO = new HospitalDAOImpl();
        this.requestDAO = new RequestDAOImpl();
        this.donationHistoryDAO = new DonationHistoryDAOImpl();
    }

    public DashboardService(DonorDAO donorDAO, HospitalDAO hospitalDAO, RequestDAO requestDAO, DonationHistoryDAO donationHistoryDAO) {
        this.donorDAO = donorDAO;
        this.hospitalDAO = hospitalDAO;
        this.requestDAO = requestDAO;
        this.donationHistoryDAO = donationHistoryDAO;
    }

    public Map<String, Object> getAdminMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        int totalUsers = 0;
        try (java.sql.Connection conn = com.lifelink.database.DBConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next()) {
                totalUsers = rs.getInt(1);
            }
        } catch (Exception e) {
            System.err.println("Error counting users: " + e.getMessage());
        }

        List<com.lifelink.model.Donor> donors = donorDAO.readAll();
        int totalDonors = donors.size();
        int availableDonors = 0;
        for (com.lifelink.model.Donor d : donors) {
            if (d.isAvailable()) {
                availableDonors++;
            }
        }
        
        int totalHospitals = hospitalDAO.readAll().size();
        
        List<BloodRequest> requests = requestDAO.readAll();
        int totalRequests = requests.size();
        int pendingRequests = 0;
        int approvedRequests = 0;
        int rejectedRequests = 0;
        
        for (BloodRequest req : requests) {
            if (req.getStatus().equalsIgnoreCase("PENDING")) {
                pendingRequests++;
            } else if (req.getStatus().equalsIgnoreCase("APPROVED") || req.getStatus().equalsIgnoreCase("COMPLETED")) {
                approvedRequests++;
            } else if (req.getStatus().equalsIgnoreCase("REJECTED")) {
                rejectedRequests++;
            }
        }
        
        List<DonationHistory> donations = donationHistoryDAO.readAll();
        int totalDonationRecords = donations.size();
        int totalUnitsDonated = 0;
        for (DonationHistory dh : donations) {
            totalUnitsDonated += dh.getUnitsDonated();
        }
        
        metrics.put("totalUsers", totalUsers);
        metrics.put("totalDonors", totalDonors);
        metrics.put("availableDonors", availableDonors);
        metrics.put("totalHospitals", totalHospitals);
        metrics.put("totalRequests", totalRequests);
        metrics.put("pendingRequests", pendingRequests);
        metrics.put("approvedRequests", approvedRequests);
        metrics.put("rejectedRequests", rejectedRequests);
        metrics.put("totalDonationRecords", totalDonationRecords);
        metrics.put("totalUnitsDonated", totalUnitsDonated);
        
        return metrics;
    }
}
