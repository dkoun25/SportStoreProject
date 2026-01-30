package com.sportstore.controller;

import com.sportstore.model.CartItem;
import com.sportstore.model.CartResponse;
import com.sportstore.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

/**
 * API Controller quản lý giỏ hàng
 * Không cần đăng nhập, dùng Session ID để phân biệt người dùng
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * Lấy giỏ hàng hiện tại
     * GET /api/cart
     */
    @GetMapping
    public CartResponse getCart(HttpSession session) {
        String sessionId = session.getId();
        return cartService.getCart(sessionId);
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     * POST /api/cart/add
     * Body: { "id": 1, "name": "...", "price": 100000, "quantity": 2, "size": "M", "color": "Đen" }
     */
    @PostMapping("/add")
    public CartResponse addToCart(@RequestBody CartItem item, HttpSession session) {
        String sessionId = session.getId();
        
        // Validation
        if (item == null || item.getId() == 0 || item.getPrice() == null || item.getQuantity() == null) {
            return new CartResponse();
        }

        // Mặc định quantity nếu không có
        if (item.getQuantity() <= 0) {
            item.setQuantity(1);
        }

        return cartService.addToCart(sessionId, item);
    }

    /**
     * Cập nhật số lượng item
     * PUT /api/cart/update/{productId}
     * Body: { "quantity": 5, "size": "L" }
     */
    @PutMapping("/update/{productId}")
public CartResponse updateQuantity(
        @PathVariable int productId,
        @RequestParam int quantity,
        @RequestParam(required = false, defaultValue = "") String size,
        HttpSession session) {
    String sessionId = session.getId();
    System.out.println("=== UPDATE QUANTITY ===");
    System.out.println("Product ID: " + productId);
    System.out.println("Size: " + size);
    System.out.println("New quantity: " + quantity);
    System.out.println("Session ID: " + sessionId);
    
    CartResponse response = cartService.updateQuantity(sessionId, productId, size, quantity);
    System.out.println("Cart items after update: " + response.getItems().size());
    
    return response;
}

    /**
     * Xóa item khỏi giỏ
     * DELETE /api/cart/remove/{productId}
     * Query param: size (mặc định "M")
     */
    @DeleteMapping("/remove/{productId}")
public CartResponse removeFromCart(
        @PathVariable int productId,
        @RequestParam(required = false, defaultValue = "") String size,
        HttpSession session) {
    String sessionId = session.getId();
    System.out.println("=== REMOVE ITEM ===");
    System.out.println("Product ID: " + productId);
    System.out.println("Size: " + size);
    System.out.println("Session ID: " + sessionId);
    
    CartResponse response = cartService.removeFromCart(sessionId, productId, size);
    System.out.println("Cart items after remove: " + response.getItems().size());
    
    return response;
}

    /**
     * Xóa toàn bộ giỏ hàng
     * DELETE /api/cart/clear
     */
    @DeleteMapping("/clear")
    public void clearCart(HttpSession session) {
        String sessionId = session.getId();
        cartService.clearCart(sessionId);
    }

    /**
     * Lấy tổng số lượng sản phẩm trong giỏ
     * GET /api/cart/count
     */
    @GetMapping("/count")
    public int getCartCount(HttpSession session) {
        String sessionId = session.getId();
        return cartService.getCartTotalQuantity(sessionId);
    }

    /**
     * Checkout - hoàn tất đơn hàng
     * POST /api/cart/checkout
     */
    @PostMapping("/checkout")
public CartResponse checkout(HttpSession session) {
    String sessionId = session.getId();
    CartResponse cart = cartService.getCart(sessionId);
    
    if (!cart.isEmpty()) {
        cartService.checkout(sessionId);
    }
    
    return cart;
}

    /**
     * Kiểm tra trạng thái giỏ hàng
     * GET /api/cart/status
     */
    @GetMapping("/status")
    public CartResponse getCartStatus(HttpSession session) {
        String sessionId = session.getId();
        return cartService.getCart(sessionId);
    }
}
