package com.sportstore.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sportstore.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrderService {
    
    private final Map<Integer, Order> orders = new ConcurrentHashMap<>();
    private final AtomicInteger orderIdCounter = new AtomicInteger(1);
    
    // Lưu trữ mã giảm giá
    private final Map<String, PromoCode> promoCodes = new ConcurrentHashMap<>();
    
    // Lưu số lần sử dụng mã giảm giá: Map<sessionId, Map<promoCode, usageCount>>
    private final Map<String, Map<String, Integer>> promoUsage = new ConcurrentHashMap<>();
    
    @Autowired
    private CartService cartService;
    
    private final ObjectMapper objectMapper;
    private final Path ordersFilePath;
    
    public OrderService() {
        // Khởi tạo ObjectMapper
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        
        // Đường dẫn file orders.json
        this.ordersFilePath = Paths.get("src/main/resources/data/orders.json");
        
        // Khởi tạo mã giảm giá APEX15
        promoCodes.put("APEX15", new PromoCode("APEX15", 0.15, 3, "Giảm 15% cho sản phẩm chưa giảm giá"));
        
        // Load orders từ file
        loadOrdersFromFile();
    }
    
    /**
     * Đọc danh sách orders từ file JSON
     */
    private void loadOrdersFromFile() {
        try {
            InputStream inputStream = getClass().getResourceAsStream("/data/orders.json");
            
            List<Order> orderList;
            if (inputStream != null) {
                orderList = objectMapper.readValue(inputStream, new TypeReference<List<Order>>() {});
            } else if (Files.exists(ordersFilePath)) {
                orderList = objectMapper.readValue(ordersFilePath.toFile(), new TypeReference<List<Order>>() {});
            } else {
                System.out.println("File orders.json không tồn tại. Tạo danh sách rỗng.");
                orderList = new ArrayList<>();
            }
            
            // Load vào Map
            for (Order order : orderList) {
                orders.put(order.getId(), order);
                if (order.getId() >= orderIdCounter.get()) {
                    orderIdCounter.set(order.getId() + 1);
                }
            }
            
            System.out.println("✓ Đã load " + orders.size() + " orders từ file");
            
        } catch (IOException e) {
            System.err.println("Lỗi đọc file orders.json: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Lưu danh sách orders vào file JSON
     */
    private void saveOrdersToFile() {
        try {
            Files.createDirectories(ordersFilePath.getParent());
            
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(ordersFilePath.toFile(), orders.values());
            
            System.out.println("✓ Đã lưu " + orders.size() + " orders vào file");
            
        } catch (IOException e) {
            System.err.println("Lỗi ghi file orders.json: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Kiểm tra và áp dụng mã giảm giá
     */
    public PromoCodeResponse validatePromoCode(String sessionId, String code) {
        PromoCodeResponse response = new PromoCodeResponse();
        
        if (code == null || code.trim().isEmpty()) {
            response.setValid(false);
            response.setMessage("Vui lòng nhập mã giảm giá");
            return response;
        }
        
        code = code.trim().toUpperCase();
        PromoCode promo = promoCodes.get(code);
        
        if (promo == null) {
            response.setValid(false);
            response.setMessage("Mã giảm giá không tồn tại");
            return response;
        }
        
        // Kiểm tra số lần sử dụng
        int usedCount = getPromoUsageCount(sessionId, code);
        int remaining = promo.getMaxUsesPerUser() - usedCount;
        
        if (remaining <= 0) {
            response.setValid(false);
            response.setMessage("Bạn đã sử dụng hết lượt giảm giá này");
            return response;
        }
        
        // Tính toán số tiền giảm
        CartResponse cart = cartService.getCart(sessionId);
        double discountAmount = calculateDiscountAmount(cart, promo);
        
        if (discountAmount == 0) {
            response.setValid(false);
            response.setMessage("Giỏ hàng không có sản phẩm áp dụng được mã này");
            return response;
        }
        
        response.setValid(true);
        response.setMessage("Áp dụng mã thành công! Giảm " + (int)(promo.getDiscountPercent() * 100) + "%");
        response.setDiscountPercent(promo.getDiscountPercent());
        response.setRemainingUses(remaining);
        response.setDiscountAmount(discountAmount);
        
        return response;
    }
    
    /**
     * Tính toán số tiền được giảm
     * Chỉ áp dụng cho sản phẩm chưa có discountPercent
     */
    private double calculateDiscountAmount(CartResponse cart, PromoCode promo) {
        if (cart.getItems() == null) return 0;
        
        double totalDiscount = 0;
        for (CartItem item : cart.getItems()) {
            // Chỉ áp dụng cho sản phẩm chưa được shop giảm giá
            if (item.getDiscountPercent() == null || item.getDiscountPercent() == 0) {
                double itemTotal = item.getPrice() * item.getQuantity();
                totalDiscount += itemTotal * promo.getDiscountPercent();
            }
        }
        
        return totalDiscount;
    }
    
    /**
     * Lấy số lần đã sử dụng mã giảm giá
     */
    private int getPromoUsageCount(String sessionId, String code) {
        Map<String, Integer> userUsage = promoUsage.get(sessionId);
        if (userUsage == null) return 0;
        return userUsage.getOrDefault(code, 0);
    }
    
    /**
     * Tăng số lần sử dụng mã giảm giá
     */
    private void incrementPromoUsage(String sessionId, String code) {
        promoUsage.putIfAbsent(sessionId, new ConcurrentHashMap<>());
        Map<String, Integer> userUsage = promoUsage.get(sessionId);
        userUsage.put(code, userUsage.getOrDefault(code, 0) + 1);
    }
    
    /**
     * Tạo đơn hàng từ giỏ hàng
     */
    public Order createOrder(String sessionId, String userEmail, String promoCode) {
        CartResponse cart = cartService.getCart(sessionId);
        
        if (cart.isEmpty()) {
            return null;
        }
        
        Order order = new Order();
        order.setId(orderIdCounter.getAndIncrement());
        order.setSessionId(sessionId);
        order.setUserEmail(userEmail);
        order.setItems(new ArrayList<>(cart.getItems()));
        order.setSubtotal(cart.getSubtotal());
        order.setShipping(cart.getShipping());
        
        // Áp dụng mã giảm giá nếu có
        double discount = 0;
        if (promoCode != null && !promoCode.trim().isEmpty()) {
            PromoCodeResponse promoResponse = validatePromoCode(sessionId, promoCode);
            if (promoResponse.isValid()) {
                discount = promoResponse.getDiscountAmount();
                order.setPromoCode(promoCode.trim().toUpperCase());
                // Tăng số lần sử dụng
                incrementPromoUsage(sessionId, promoCode.trim().toUpperCase());
            }
        }
        
        order.setDiscount(discount);
        order.setTotal(cart.getTotal() - discount);
        
        // Lưu đơn hàng vào Map
        orders.put(order.getId(), order);
        
        // Lưu vào file
        saveOrdersToFile();
        
        // Xóa giỏ hàng sau khi đặt
        cartService.clearCart(sessionId);
        
        return order;
    }
    
    /**
     * Lấy lịch sử đơn hàng theo sessionId (phân trang)
     */
    public List<Order> getOrderHistory(String sessionId, int page, int pageSize) {
        return orders.values().stream()
                .filter(order -> order.getSessionId().equals(sessionId))
                .sorted((o1, o2) -> Long.compare(o2.getCreatedAt(), o1.getCreatedAt())) // Mới nhất trước
                .skip((long) page * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
    }
    
    /**
     * Đếm tổng số đơn hàng của session
     */
    public long countOrders(String sessionId) {
        return orders.values().stream()
                .filter(order -> order.getSessionId().equals(sessionId))
                .count();
    }
    
    /**
     * Lấy thông tin đơn hàng theo ID
     */
    public Order getOrderById(int orderId) {
        return orders.get(orderId);
    }
}
