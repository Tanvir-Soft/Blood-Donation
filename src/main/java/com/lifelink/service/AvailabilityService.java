package com.lifelink.service;

import com.lifelink.dao.DonationHistoryDAO;
import com.lifelink.dao.DonorDAO;
import com.lifelink.dao.RequestDAO;
import com.lifelink.daoimpl.DonationHistoryDAOImpl;
import com.lifelink.daoimpl.DonorDAOImpl;
import com.lifelink.daoimpl.RequestDAOImpl;
import com.lifelink.model.DonationHistory;
import com.lifelink.model.Donor;
import com.lifelink.model.BloodRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregates blood stock levels across all blood groups.
 * Stock = total donated units minus approved/completed request units.
 */
public class AvailabilityService {
    private final DonationHistoryDAO donationHistoryDAO;
    private final DonorDAO donorDAO;
    private final RequestDAO requestDAO;

    public AvailabilityService() {
        this.donationHistoryDAO = new DonationHistoryDAOImpl();
        this.donorDAO = new DonorDAOImpl();
        this.requestDAO = new RequestDAOImpl();
    }

    public AvailabilityService(DonationHistoryDAO donationHistoryDAO, DonorDAO donorDAO, RequestDAO requestDAO) {
        this.donationHistoryDAO = donationHistoryDAO;
        this.donorDAO = donorDAO;
        this.requestDAO = requestDAO;
    }

    public int getBloodStock(String bloodGroup) {
        int totalDonated = 0;
        List<DonationHistory> donations = donationHistoryDAO.readAll();
        for (DonationHistory donation : donations) {
            Donor donor = donorDAO.readById(donation.getDonorId());
            if (donor != null && donor.getBloodGroup().equalsIgnoreCase(bloodGroup)) {
                totalDonated += donation.getUnitsDonated();
            }
        }

        int totalConsumed = 0;
        List<BloodRequest> requests = requestDAO.readAll();
        for (BloodRequest request : requests) {
            if (request.getBloodGroup().equalsIgnoreCase(bloodGroup) &&
                    (request.getStatus().equalsIgnoreCase("APPROVED") || request.getStatus().equalsIgnoreCase("COMPLETED"))) {
                totalConsumed += request.getUnitsRequested();
            }
        }

        return Math.max(0, totalDonated - totalConsumed);
    }

    public Map<String, Integer> getAllBloodStock() {
        String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        Map<String, Integer> stockMap = new HashMap<>();
        for (String bg : bloodGroups) {
            stockMap.put(bg, getBloodStock(bg));
        }
        return stockMap;
    }
}
