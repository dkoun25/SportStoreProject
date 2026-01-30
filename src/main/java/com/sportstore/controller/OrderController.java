package com.sportstore.controller;

import com.sportstore.model.Order;
import com.sportstore.model.PromoCodeResponse;
import com.sportstore.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API Controller quản lý đơn hàng và mã giảm giá
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * Kiểm tra mã giảm giá
     * POST /api/orders/validate-promo
     * Body: { "code": "APEX15" }
     */
    @PostMapping("/validate-promo")
    public ResponseEntity<PromoCodeResponse> validatePromoCode(
            @RequestBody Map<String, String> request,
            HttpSession session) {
        String code = request.get("code");
        String sessionId = session.getId();
        
        PromoCodeResponse response = orderService.validatePromoCode(sessionId, code);
        return ResponseEntity.ok(response);
    }

    /**
     * Tạo đơn hàng (checkout)
     * POST /api/orders/checkout
     * Body: { "promoCode": "APEX15" } (optional)
     */
    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(
            @RequestBody(required = false) Map<String, String> request,
            HttpSession session) {
        String sessionId = session.getId();
        String promoCode = (request != null) ? request.get("promoCode") : null;
        
        // Lấy email user nếu đã đăng nhập
        String userEmail = (String) session.getAttribute("userEmail");
        
        Order order = orderService.createOrder(sessionId, userEmail, promoCode);
        
        if (order == null) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok(order);
    }

    /**
     * Lấy lịch sử đơn hàng (phân trang)
     * GET /api/orders/history?page=0&size=5
     */
    @GetMapping("/history")
public ResponseEntity<Map<String, Object>> getOrderHistory(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size,
        HttpSession session) {
    
    String userEmail = (String) session.getAttribute("userEmail");
    String sessionId = session.getId();
    
    System.out.println("=== GET ORDER HISTORY ===");
    System.out.println("Session ID: " + sessionId);
    System.out.println("User Email from session: " + userEmail);
    
    List<Order> orders;
    long totalOrders;
    
    // Luôn ưu tiên email, nếu không có email thì dùng sessionId
    if (userEmail != null && !userEmail.isEmpty()) {
        System.out.println("Fetching by EMAIL: " + userEmail);
        orders = orderService.getOrderHistoryByEmail(userEmail, page, size);
        totalOrders = orderService.countOrdersByEmail(userEmail);
    } else {
        System.out.println("Fetching by SESSION ID: " + sessionId);
        orders = orderService.getOrderHistory(sessionId, page, size);
        totalOrders = orderService.countOrders(sessionId);
    }
    
    System.out.println("Found orders: " + orders.size() + ", Total: " + totalOrders);
    
    int totalPages = (int) Math.ceil((double) totalOrders / size);
    
    Map<String, Object> response = new HashMap<>();
    response.put("orders", orders);
    response.put("currentPage", page);
    response.put("totalPages", totalPages);
    response.put("totalOrders", totalOrders);
    response.put("pageSize", size);
    
    return ResponseEntity.ok(response);
}

    /**
     * Lấy thông tin đơn hàng theo ID
     * GET /api/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable int orderId) {
        Order order = orderService.getOrderById(orderId);
        
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(order);
    }
}
