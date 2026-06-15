package com.lifelink.service;

import com.lifelink.dao.DonorDAO;
import com.lifelink.daoimpl.DonorDAOImpl;
import com.lifelink.model.Donor;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Checks whether a donor is eligible to donate blood.
 * Rules: Age 18–60, Weight >= 50kg, Last Donation > 90 days ago.
 */
public class EligibilityService {
    private final DonorDAO donorDAO;

    public EligibilityService() {
        this.donorDAO = new DonorDAOImpl();
    }

    public EligibilityService(DonorDAO donorDAO) {
        this.donorDAO = donorDAO;
    }

    public boolean isEligible(Donor donor) {
        if (donor.getAge() < 18 || donor.getAge() > 60) {
            return false;
        }
        if (donor.getWeight() < 50) {
            return false;
        }
        if (donor.getLastDonationDate() != null) {
            long daysSinceLastDonation = ChronoUnit.DAYS.between(donor.getLastDonationDate(), LocalDate.now());
            if (daysSinceLastDonation < 90) {
                return false;
            }
        }
        return true;
    }

    public String getEligibilityReason(Donor donor) {
        if (donor.getAge() < 18 || donor.getAge() > 60) {
            return "Age must be between 18 and 60 years (current: " + donor.getAge() + ").";
        }
        if (donor.getWeight() < 50) {
            return "Weight must be at least 50 kg (current: " + donor.getWeight() + " kg).";
        }
        if (donor.getLastDonationDate() != null) {
            long daysSinceLastDonation = ChronoUnit.DAYS.between(donor.getLastDonationDate(), LocalDate.now());
            if (daysSinceLastDonation < 90) {
                long daysRemaining = 90 - daysSinceLastDonation;
                return "Last donation was " + daysSinceLastDonation + " days ago. Must wait " + daysRemaining + " more days.";
            }
        }
        return "Eligible";
    }

    /**
     * Checks eligibility and updates the donor's availability flag in the database.
     */
    public boolean checkAndUpdateEligibility(Donor donor) {
        boolean eligible = isEligible(donor);
        if (donor.isAvailable() != eligible) {
            donor.setAvailable(eligible);
            donorDAO.update(donor);
        }
        return eligible;
    }
}
