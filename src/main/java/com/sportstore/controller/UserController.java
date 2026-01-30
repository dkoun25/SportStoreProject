package com.sportstore.controller;

import com.sportstore.model.User;
import com.sportstore.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * API Controller quản lý thông tin người dùng
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private AuthService authService;

    /**
     * Lấy thông tin user hiện tại
     * GET /api/user/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(HttpSession session) {
        String userEmail = (String) session.getAttribute("userEmail");
        
        if (userEmail == null) {
            return ResponseEntity.status(401).build();
        }
        
        User user = authService.getUserByEmail(userEmail);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Tạo response map để không làm thay đổi object gốc
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("email", user.getEmail());
        response.put("phone", user.getPhone());
        response.put("avatar", user.getAvatar());
        response.put("address", user.getAddress());
        response.put("createdAt", user.getCreatedAt());
        // Không trả về password
        
        return ResponseEntity.ok(response);
    }

    /**
     * Cập nhật thông tin cá nhân
     * PUT /api/user/profile
     * Body: { "firstName": "...", "lastName": "...", "phone": "...", "address": "..." }
     */
    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestBody Map<String, String> updates,
            HttpSession session) {
        String userEmail = (String) session.getAttribute("userEmail");
        
        if (userEmail == null) {
            return ResponseEntity.status(401).body(createResponse(false, "Chưa đăng nhập"));
        }
        
        User user = authService.getUserByEmail(userEmail);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Cập nhật thông tin cơ bản
        String firstName = updates.getOrDefault("firstName", user.getFirstName());
        String lastName = updates.getOrDefault("lastName", user.getLastName());
        String phone = updates.getOrDefault("phone", user.getPhone());
        
        // Gọi service để update và lưu file
        authService.updateUser(user.getId(), firstName, lastName, phone);
        
        // Cập nhật địa chỉ riêng nếu có
        if (updates.containsKey("address")) {
            authService.updateUserAddress(user.getId(), updates.get("address"));
        }
        
        // Lấy user đã cập nhật
        User updatedUser = authService.getUserByEmail(userEmail);
        
        Map<String, Object> response = createResponse(true, "Cập nhật thông tin thành công");
        updatedUser.setPassword(null);
        response.put("user", updatedUser);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Đổi mật khẩu
     * PUT /api/user/change-password
     * Body: { "currentPassword": "...", "newPassword": "..." }
     */
    @PutMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestBody Map<String, String> request,
            HttpSession session) {
        String userEmail = (String) session.getAttribute("userEmail");
        
        if (userEmail == null) {
            return ResponseEntity.status(401).body(createResponse(false, "Chưa đăng nhập"));
        }
        
        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");
        
        if (currentPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body(createResponse(false, "Thiếu thông tin"));
        }
        
        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(createResponse(false, "Mật khẩu mới phải có ít nhất 6 ký tự"));
        }
        
        // Lấy user từ service (có đầy đủ thông tin bao gồm password)
        User user = authService.getUserByEmail(userEmail);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Kiểm tra mật khẩu hiện tại (user từ authService có password đầy đủ)
        if (user.getPassword() == null || !user.getPassword().equals(currentPassword)) {
            return ResponseEntity.badRequest().body(createResponse(false, "Mật khẩu hiện tại không đúng"));
        }
        
        // Đổi mật khẩu và lưu vào file
        authService.changePassword(user.getId(), newPassword);
        
        return ResponseEntity.ok(createResponse(true, "Đổi mật khẩu thành công"));
    }

    private Map<String, Object> createResponse(boolean success, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        return response;
    }
}
