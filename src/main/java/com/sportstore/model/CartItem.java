package com.sportstore.model;

/**
 * Model đại diện cho một item trong giỏ hàng
 * Chứa thông tin sản phẩm + số lượng + size + color
 */
public class CartItem {
    private int id;
    private String name;
    private String category;
    private Double price;
    private String image;
    private Integer quantity;
    private String size;
    private String color;
    private Long addedDate; // Timestamp khi thêm vào giỏ
    private Integer discountPercent; // Phần trăm giảm giá của shop (0-100)

    // Constructor không tham số
    public CartItem() {}

    // Constructor đầy đủ
    public CartItem(int id, String name, String category, Double price, String image, 
                    Integer quantity, String size, String color) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.image = image;
        this.quantity = quantity;
        this.size = size;
        this.color = color;
        this.addedDate = System.currentTimeMillis();
    }

    // Getters và Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Long getAddedDate() { return addedDate; }
    public void setAddedDate(Long addedDate) { this.addedDate = addedDate; }

    public Integer getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(Integer discountPercent) { this.discountPercent = discountPercent; }

    /**
     * Tính toán giá tiền cho item này (price * quantity)
     */
    public Double getSubtotal() {
        return (this.price != null && this.quantity != null) 
            ? this.price * this.quantity 
            : 0.0;
    }
}
