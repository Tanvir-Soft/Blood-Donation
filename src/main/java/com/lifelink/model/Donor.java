package com.lifelink.model;

import java.time.LocalDate;

public class Donor {
    private int id;
    private int userId;
    private String name;
    private int age;
    private double weight;
    private String bloodGroup;
    private String city;
    private LocalDate lastDonationDate;
    private boolean isAvailable;

    public Donor() {}

    public Donor(int userId, String name, int age, double weight, String bloodGroup, String city, LocalDate lastDonationDate, boolean isAvailable) {
        this.userId = userId;
        this.name = name;
        this.age = age;
        this.weight = weight;
        this.bloodGroup = bloodGroup;
        this.city = city;
        this.lastDonationDate = lastDonationDate;
        this.isAvailable = isAvailable;
    }

    public Donor(int id, int userId, String name, int age, double weight, String bloodGroup, String city, LocalDate lastDonationDate, boolean isAvailable) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.age = age;
        this.weight = weight;
        this.bloodGroup = bloodGroup;
        this.city = city;
        this.lastDonationDate = lastDonationDate;
        this.isAvailable = isAvailable;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public LocalDate getLastDonationDate() {
        return lastDonationDate;
    }

    public void setLastDonationDate(LocalDate lastDonationDate) {
        this.lastDonationDate = lastDonationDate;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    @Override
    public String toString() {
        return "Donor{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", weight=" + weight +
                ", bloodGroup='" + bloodGroup + '\'' +
                ", city='" + city + '\'' +
                ", lastDonationDate=" + lastDonationDate +
                ", isAvailable=" + isAvailable +
                '}';
    }
}
