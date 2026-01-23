package com.sportstore.model;

import java.util.List;

/**
 * Model đại diện cho response của giỏ hàng
 * Chứa danh sách items + các thông tin tính toán tiền
 */
public class CartResponse {
    private List<CartItem> items;
    private Double subtotal;      // Tạm tính (tổng giá tiền sản phẩm)
    private Double shipping;      // Phí vận chuyển
    private Double tax;           // Thuế (10%)
    private Double total;         // Tổng cộng
    private Integer itemCount;    // Tổng số lượng sản phẩm

    // Constructor không tham số
    public CartResponse() {}

    // Constructor đầy đủ
    public CartResponse(List<CartItem> items) {
        this.items = items;
        calculateTotals();
    }

    /**
     * Tính toán tất cả các giá trị (subtotal, shipping, tax, total)
     */
    private void calculateTotals() {
        // 1. Tính tạm tính (tổng giá tiền của tất cả items)
        this.subtotal = items.stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();

        // 2. Tính phí vận chuyển: Miễn phí nếu >= 200k, ngược lại 30k
        this.shipping = this.subtotal >= 200000 ? 0.0 : 30000.0;

        // 3. Tính thuế: 10% của subtotal
        this.tax = Math.floor(this.subtotal * 0.1);

        // 4. Tính tổng cộng
        this.total = this.subtotal + this.shipping + this.tax;

        // 5. Tính tổng số lượng items
        this.itemCount = items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    // Getters và Setters
    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) {
        this.items = items;
        calculateTotals();
    }

    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }

    public Double getShipping() { return shipping; }
    public void setShipping(Double shipping) { this.shipping = shipping; }

    public Double getTax() { return tax; }
    public void setTax(Double tax) { this.tax = tax; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }

    public Integer getItemCount() { return itemCount; }
    public void setItemCount(Integer itemCount) { this.itemCount = itemCount; }

    /**
     * Để kiểm tra cart có trống không
     */
    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }
}
