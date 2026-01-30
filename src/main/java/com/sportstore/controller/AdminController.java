package com.sportstore.controller;

import com.sportstore.model.Product;
import com.sportstore.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private DataService dataService;

    // API: Thêm sản phẩm mới
    @PostMapping("/products/add")
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        try {
            Product saved = dataService.addProduct(product);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // API: Cập nhật sản phẩm
    @PutMapping("/products/update/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable int id, @RequestBody Product product) {
        try {
            System.out.println("Updating product ID: " + id);
            System.out.println("Product data: " + product.getName());
            Product updated = dataService.updateProduct(id, product);
            if (updated == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // API: Xóa sản phẩm
    @DeleteMapping("/products/delete/{id}")
    public ResponseEntity<Boolean> deleteProduct(@PathVariable int id) {
        try {
            boolean deleted = dataService.deleteProduct(id);
            return ResponseEntity.ok(deleted);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
