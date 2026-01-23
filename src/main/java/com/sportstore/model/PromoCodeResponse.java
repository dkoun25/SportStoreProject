package com.sportstore.model;

public class PromoCodeResponse {
    private boolean valid;
    private String message;
    private double discountPercent;
    private int remainingUses;
    private double discountAmount; // Số tiền được giảm

    public PromoCodeResponse() {}

    public PromoCodeResponse(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    // Getters & Setters
    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(double discountPercent) {
        this.discountPercent = discountPercent;
    }

    public int getRemainingUses() {
        return remainingUses;
    }

    public void setRemainingUses(int remainingUses) {
        this.remainingUses = remainingUses;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }
}
