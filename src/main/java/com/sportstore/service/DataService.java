package com.sportstore.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportstore.model.Product;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataService {
    private List<Product> products = new ArrayList<>();

    // Chạy hàm này ngay khi ứng dụng khởi động
    @PostConstruct
    public void loadData() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            // Đọc file từ thư mục resources
            File file = new ClassPathResource("data/products.json").getFile();
            // Map dữ liệu JSON vào List<Product>
            products = mapper.readValue(file, new TypeReference<List<Product>>(){});
            System.out.println("Đã load thành công " + products.size() + " sản phẩm.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Không tìm thấy dữ liệu.");
        }
    }

    public List<Product> getAllProducts() {
        return products;
    }
}