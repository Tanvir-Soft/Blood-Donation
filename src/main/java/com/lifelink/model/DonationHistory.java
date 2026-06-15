package com.lifelink.model;

import java.time.LocalDate;

public class DonationHistory {
    private int id;
    private int donorId;
    private LocalDate donationDate;
    private int unitsDonated;
    private String location;

    public DonationHistory() {}

    public DonationHistory(int donorId, LocalDate donationDate, int unitsDonated, String location) {
        this.donorId = donorId;
        this.donationDate = donationDate;
        this.unitsDonated = unitsDonated;
        this.location = location;
    }

    public DonationHistory(int id, int donorId, LocalDate donationDate, int unitsDonated, String location) {
        this.id = id;
        this.donorId = donorId;
        this.donationDate = donationDate;
        this.unitsDonated = unitsDonated;
        this.location = location;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDonorId() {
        return donorId;
    }

    public void setDonorId(int donorId) {
        this.donorId = donorId;
    }

    public LocalDate getDonationDate() {
        return donationDate;
    }

    public void setDonationDate(LocalDate donationDate) {
        this.donationDate = donationDate;
    }

    public int getUnitsDonated() {
        return unitsDonated;
    }

    public void setUnitsDonated(int unitsDonated) {
        this.unitsDonated = unitsDonated;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "DonationHistory{" +
                "id=" + id +
                ", donorId=" + donorId +
                ", donationDate=" + donationDate +
                ", unitsDonated=" + unitsDonated +
                ", location='" + location + '\'' +
                '}';
    }
}
