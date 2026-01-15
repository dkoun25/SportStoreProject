package com.sportstore.controller;

import com.sportstore.model.Product;
import com.sportstore.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {

    @Autowired
    private DataService dataService;

    // Endpoint: http://localhost:8080/api/products
    @GetMapping("/products")
    public List<Product> getProducts() {
        return dataService.getAllProducts();
    }
}