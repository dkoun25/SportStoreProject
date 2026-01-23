package com.sportstore.service;

import com.sportstore.model.CartItem;
import com.sportstore.model.CartResponse;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service quản lý giỏ hàng
 * Lưu trữ giỏ hàng trong session/memory (có thể mở rộng để lưu vào DB hoặc Redis)
 */
@Service
public class CartService {

    /**
     * Lưu trữ giỏ hàng theo session ID (key: sessionId, value: danh sách CartItem)
     * Trong thực tế, nên dùng Redis hoặc Database
     */
    private Map<String, List<CartItem>> cartStorage = new HashMap<>();

    /**
     * Lấy giỏ hàng của một session
     */
    public CartResponse getCart(String sessionId) {
        List<CartItem> items = cartStorage.getOrDefault(sessionId, new ArrayList<>());
        return new CartResponse(items);
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     * Nếu sản phẩm đã tồn tại (cùng id + size + color), tăng số lượng
     */
    public CartResponse addToCart(String sessionId, CartItem item) {
        List<CartItem> cart = cartStorage.getOrDefault(sessionId, new ArrayList<>());

        // Tìm xem có item cùng id, size và color không
        Optional<CartItem> existingItem = cart.stream()
                .filter(i -> i.getId() == item.getId() && 
                           Objects.equals(i.getSize(), item.getSize()) &&
                           Objects.equals(i.getColor(), item.getColor()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Nếu tồn tại, cộng thêm số lượng
            existingItem.get().setQuantity(existingItem.get().getQuantity() + item.getQuantity());
        } else {
            // Nếu không, thêm item mới
            item.setAddedDate(System.currentTimeMillis());
            cart.add(item);
        }

        cartStorage.put(sessionId, cart);
        return new CartResponse(cart);
    }

    /**
     * Cập nhật số lượng item trong giỏ
     */
    public CartResponse updateQuantity(String sessionId, int productId, String size, int newQuantity) {
        List<CartItem> cart = cartStorage.getOrDefault(sessionId, new ArrayList<>());

        for (CartItem item : cart) {
            if (item.getId() == productId && 
               (item.getSize() != null ? item.getSize().equals(size) : size == null)) {
                if (newQuantity > 0) {
                    item.setQuantity(newQuantity);
                } else {
                    // Nếu số lượng <= 0, xóa item
                    cart.remove(item);
                }
                break;
            }
        }

        if (cart.isEmpty()) {
            cartStorage.remove(sessionId);
        } else {
            cartStorage.put(sessionId, cart);
        }

        return new CartResponse(cart);
    }

    /**
     * Xóa 1 item khỏi giỏ
     */
    public CartResponse removeFromCart(String sessionId, int productId, String size) {
        List<CartItem> cart = cartStorage.getOrDefault(sessionId, new ArrayList<>());

        cart.removeIf(item -> item.getId() == productId && 
                             (item.getSize() != null ? item.getSize().equals(size) : size == null));

        if (cart.isEmpty()) {
            cartStorage.remove(sessionId);
        } else {
            cartStorage.put(sessionId, cart);
        }

        return new CartResponse(cart);
    }

    /**
     * Xóa toàn bộ giỏ hàng
     */
    public void clearCart(String sessionId) {
        cartStorage.remove(sessionId);
    }

    /**
     * Lấy số lượng items trong giỏ
     */
    public int getCartItemCount(String sessionId) {
        return cartStorage.getOrDefault(sessionId, new ArrayList<>()).size();
    }

    /**
     * Lấy tổng số lượng sản phẩm (có tính quantity)
     */
    public int getCartTotalQuantity(String sessionId) {
        List<CartItem> cart = cartStorage.getOrDefault(sessionId, new ArrayList<>());
        return cart.stream().mapToInt(CartItem::getQuantity).sum();
    }

    /**
     * Checkout - xóa giỏ hàng sau khi hoàn tất đơn hàng
     */
    public void checkout(String sessionId) {
        cartStorage.remove(sessionId);
    }
}
