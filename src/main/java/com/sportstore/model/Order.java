package com.sportstore.model;

import java.util.List;

public class Order {
    private int id;
    private String sessionId;
    private String userEmail; // null nếu không đăng nhập
    private List<CartItem> items;
    private Double subtotal;
    private Double shipping;
    private Double discount; // Số tiền được giảm
    private Double total;
    private String promoCode; // Mã giảm giá đã sử dụng
    private long createdAt;
    private String status; // "pending", "completed", "cancelled"

    public Order() {
        this.createdAt = System.currentTimeMillis();
        this.status = "completed";
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public Double getShipping() {
        return shipping;
    }

    public void setShipping(Double shipping) {
        this.shipping = shipping;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
