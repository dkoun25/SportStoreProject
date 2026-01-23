package com.sportstore.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {
    private int id;
    private String name;
    private String category;
    private Double price;
    private String image;
    private String description;
    private List<String> sizes;
    private List<String> colors;
    private String material;
    private Map<String, Object> specs;
    private Integer discountPercent; // % giảm giá, null hoặc 0 nếu không giảm

    // Constructor không tham số (Bắt buộc để Jackson đọc JSON)
    public Product() {}

    // Constructor đầy đủ (legacy)
    public Product(int id, String name, String category, Double price, String image) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.image = image;
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

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getSizes() { return sizes; }
    public void setSizes(List<String> sizes) { this.sizes = sizes; }

    public List<String> getColors() { return colors; }
    public void setColors(List<String> colors) { this.colors = colors; }

    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }

    public Map<String, Object> getSpecs() { return specs; }
    public void setSpecs(Map<String, Object> specs) { this.specs = specs; }

    public Integer getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(Integer discountPercent) { this.discountPercent = discountPercent; }
}