package com.sportstore.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportstore.model.Product;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;

@Service
public class DataService {

    private List<Product> products = new ArrayList<>();

    // Hàm này chạy ngay khi khởi động App để nạp dữ liệu
    public DataService() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            // Đọc file từ thư mục resources/data/products.json
            InputStream inputStream = getClass().getResourceAsStream("/data/products.json");
            
            if (inputStream == null) {
                System.out.println("ERROR: Khong tim thay file products.json!");
            } else {
                // Chuyển đổi JSON thành List<Product>
                products = mapper.readValue(inputStream, new TypeReference<List<Product>>(){});
                System.out.println("DA LOAD THANH CONG " + products.size() + " SAN PHAM");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Lấy toàn bộ sản phẩm
    public List<Product> getAllProducts() {
        return products;
    }

    // Lấy sản phẩm theo ID (Dùng cho trang Chi tiết)
    public Product getProductById(int id) {
        return products.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
    }

    // Tìm kiếm sản phẩm theo keyword
    // Tìm trong tên sản phẩm hoặc category
    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String searchTerm = keyword.toLowerCase().trim();

        return products.stream()
                .filter(p -> p.getName().toLowerCase().contains(searchTerm) ||
                           p.getCategory().toLowerCase().contains(searchTerm))
                .toList();
    }

    // Lọc sản phẩm nâng cao
    public List<Product> filterProducts(String group, String size, String color, Double minPrice, Double maxPrice, String sort, String keyword) {
        String groupLower = group != null ? group.toLowerCase() : null;
        String sizeLower = size != null ? size.toLowerCase() : null;
        String colorLower = color != null ? color.toLowerCase() : null;
        String keywordLower = keyword != null ? keyword.toLowerCase() : null;

        List<Product> filtered = products.stream()
                .filter(p -> {
                    // Group/category filter (men, women, accessories, shoes)
                    if (groupLower != null && !groupLower.isEmpty()) {
                        String cat = p.getCategory() != null ? p.getCategory().toLowerCase() : "";
                        boolean matchGroup = false;
                        if (groupLower.equals("men")) {
                            matchGroup = cat.startsWith("men");
                        } else if (groupLower.equals("women")) {
                            matchGroup = cat.startsWith("women");
                        } else if (groupLower.equals("accessories")) {
                            matchGroup = cat.startsWith("accessories");
                        } else if (groupLower.equals("shoes")) {
                            matchGroup = cat.contains("shoes");
                        }
                        if (!matchGroup) return false;
                    }

                    // Size filter
                    if (sizeLower != null && !sizeLower.isEmpty()) {
                        if (p.getSizes() == null || p.getSizes().stream().noneMatch(s -> s != null && s.toLowerCase().equals(sizeLower))) {
                            return false;
                        }
                    }

                    // Color filter
                    if (colorLower != null && !colorLower.isEmpty()) {
                        if (p.getColors() == null || p.getColors().stream().noneMatch(c -> c != null && c.toLowerCase().contains(colorLower))) {
                            return false;
                        }
                    }

                    // Price range
                    if (minPrice != null && p.getPrice() != null && p.getPrice() < minPrice) return false;
                    if (maxPrice != null && p.getPrice() != null && p.getPrice() > maxPrice) return false;

                    // Keyword search in name/category/description
                    if (keywordLower != null && !keywordLower.isEmpty()) {
                        String name = p.getName() != null ? p.getName().toLowerCase() : "";
                        String cat = p.getCategory() != null ? p.getCategory().toLowerCase() : "";
                        String desc = p.getDescription() != null ? p.getDescription().toLowerCase() : "";
                        if (!(name.contains(keywordLower) || cat.contains(keywordLower) || desc.contains(keywordLower))) {
                            return false;
                        }
                    }

                    return true;
                })
                .toList();

        // Sorting
        if (sort != null) {
            switch (sort) {
                case "price_asc" -> filtered = filtered.stream()
                        .sorted((a, b) -> Double.compare(a.getPrice(), b.getPrice()))
                        .toList();
                case "price_desc" -> filtered = filtered.stream()
                        .sorted((a, b) -> Double.compare(b.getPrice(), a.getPrice()))
                        .toList();
                case "name_asc" -> filtered = filtered.stream()
                        .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                        .toList();
                case "name_desc" -> filtered = filtered.stream()
                        .sorted((a, b) -> b.getName().compareToIgnoreCase(a.getName()))
                        .toList();
                default -> {
                }
            }
        }

        return filtered;
    }

    // === ADMIN CRUD METHODS ===

    // Thêm sản phẩm mới
    public Product addProduct(Product product) {
        // Tạo ID mới (lấy max ID hiện tại + 1)
        int maxId = products.stream()
                .mapToInt(Product::getId)
                .max()
                .orElse(0);
        product.setId(maxId + 1);
        products.add(product);
        saveProductsToFile();
        return product;
    }

    // Cập nhật sản phẩm
    public Product updateProduct(int id, Product updatedProduct) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId() == id) {
                updatedProduct.setId(id); // Giữ nguyên ID
                products.set(i, updatedProduct);
                saveProductsToFile();
                return updatedProduct;
            }
        }
        return null;
    }

    // Xóa sản phẩm
    public boolean deleteProduct(int id) {
        boolean removed = products.removeIf(p -> p.getId() == id);
        if (removed) {
            saveProductsToFile();
        }
        return removed;
    }

    // Lưu products vào file JSON
    private void saveProductsToFile() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            java.io.File file = new java.io.File("src/main/resources/data/products.json");
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, products);
            System.out.println("DA LUU " + products.size() + " SAN PHAM VAO FILE");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}