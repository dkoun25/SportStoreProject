package com.sportstore.controller;

import com.sportstore.model.Product;
import com.sportstore.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private DataService dataService;

    // API 1: Lấy toàn bộ danh sách (Dùng cho trang chủ, trang category)
    // URL: http://localhost:8080/api/products
    @GetMapping
    public List<Product> getAll() {
        return dataService.getAllProducts();
    }

    // API 2: Lấy chi tiết 1 sản phẩm (Dùng cho trang Detail)
    // URL: http://localhost:8080/api/products/1
    @GetMapping("/{id}")
    public Product getOne(@PathVariable int id) {
        return dataService.getProductById(id);
    }

    // API 3: Tìm kiếm sản phẩm theo keyword
    // URL: http://localhost:8080/api/products/search?q=nike
    @GetMapping("/search")
    public List<Product> search(@RequestParam String q) {
        return dataService.searchProducts(q);
    }

    // API 4: Filter sản phẩm nâng cao (category group, size, color, price, sort, keyword)
    // Ví dụ: /api/products/filter?group=men&size=M&color=Đen&minPrice=500000&maxPrice=2000000&sort=price_asc&q=running
    @GetMapping("/filter")
    public List<Product> filter(
            @RequestParam(required = false) String group,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String q) {
        return dataService.filterProducts(group, size, color, minPrice, maxPrice, sort, q);
    }
}