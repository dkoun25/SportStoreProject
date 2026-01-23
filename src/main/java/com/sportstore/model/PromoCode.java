package com.sportstore.model;

public class PromoCode {
    private String code;
    private double discountPercent; // 0.15 = 15%
    private int maxUsesPerUser;
    private String description;

    public PromoCode() {}

    public PromoCode(String code, double discountPercent, int maxUsesPerUser, String description) {
        this.code = code;
        this.discountPercent = discountPercent;
        this.maxUsesPerUser = maxUsesPerUser;
        this.description = description;
    }

    // Getters & Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(double discountPercent) {
        this.discountPercent = discountPercent;
    }

    public int getMaxUsesPerUser() {
        return maxUsesPerUser;
    }

    public void setMaxUsesPerUser(int maxUsesPerUser) {
        this.maxUsesPerUser = maxUsesPerUser;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
