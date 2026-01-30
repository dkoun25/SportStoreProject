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
            @RequestParam(required = false, defaultValue = "M") String size,
            HttpSession session) {
        String sessionId = session.getId();
        return cartService.updateQuantity(sessionId, productId, size, quantity);
    }

    /**
     * Xóa item khỏi giỏ
     * DELETE /api/cart/remove/{productId}
     * Query param: size (mặc định "M")
     */
    @DeleteMapping("/remove/{productId}")
    public CartResponse removeFromCart(
            @PathVariable int productId,
            @RequestParam(required = false, defaultValue = "M") String size,
            HttpSession session) {
        String sessionId = session.getId();
        return cartService.removeFromCart(sessionId, productId, size);
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
        
        // Giả lập xử lý thanh toán
        if (!cart.isEmpty()) {
            // TODO: Lưu đơn hàng vào database
            // TODO: Gọi API thanh toán (VNPay, Paypal, etc.)
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
