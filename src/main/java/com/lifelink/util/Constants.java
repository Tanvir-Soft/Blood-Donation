package com.lifelink.util;

public class Constants {
    public static final String[] BLOOD_GROUPS = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
    
    public static final String ROLE_DONOR = "DONOR";
    public static final String ROLE_HOSPITAL = "HOSPITAL";
    public static final String ROLE_ADMIN = "ADMIN";

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_COMPLETED = "COMPLETED";

    public static boolean isValidBloodGroup(String bg) {
        if (bg == null) return false;
        for (String validBg : BLOOD_GROUPS) {
            if (validBg.equalsIgnoreCase(bg.trim())) {
                return true;
            }
        }
        return false;
    }
}
