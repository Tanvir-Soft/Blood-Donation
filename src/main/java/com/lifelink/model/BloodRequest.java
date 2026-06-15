package com.lifelink.model;

import java.time.LocalDate;

public class BloodRequest {
    private int id;
    private Integer seekerId; // Links to users (role SEEKER)
    private Integer hospitalId; // Links to hospitals
    private String bloodGroup;
    private int unitsRequested;
    private String priority; // Critical, High, Normal
    private LocalDate requestDate;
    private String status; // PENDING, APPROVED, REJECTED, COMPLETED

    public BloodRequest() {}

    public BloodRequest(Integer seekerId, Integer hospitalId, String bloodGroup, int unitsRequested, String priority, LocalDate requestDate, String status) {
        this.seekerId = seekerId;
        this.hospitalId = hospitalId;
        this.bloodGroup = bloodGroup;
        this.unitsRequested = unitsRequested;
        this.priority = priority;
        this.requestDate = requestDate;
        this.status = status;
    }

    public BloodRequest(int id, Integer seekerId, Integer hospitalId, String bloodGroup, int unitsRequested, String priority, LocalDate requestDate, String status) {
        this.id = id;
        this.seekerId = seekerId;
        this.hospitalId = hospitalId;
        this.bloodGroup = bloodGroup;
        this.unitsRequested = unitsRequested;
        this.priority = priority;
        this.requestDate = requestDate;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getSeekerId() {
        return seekerId;
    }

    public void setSeekerId(Integer seekerId) {
        this.seekerId = seekerId;
    }

    public Integer getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(Integer hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public int getUnitsRequested() {
        return unitsRequested;
    }

    public void setUnitsRequested(int unitsRequested) {
        this.unitsRequested = unitsRequested;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "BloodRequest{" +
                "id=" + id +
                ", seekerId=" + seekerId +
                ", hospitalId=" + hospitalId +
                ", bloodGroup='" + bloodGroup + '\'' +
                ", unitsRequested=" + unitsRequested +
                ", priority='" + priority + '\'' +
                ", requestDate=" + requestDate +
                ", status='" + status + '\'' +
                '}';
    }
}
