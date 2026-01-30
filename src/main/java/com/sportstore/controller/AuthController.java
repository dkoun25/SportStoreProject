package com.sportstore.controller;

import com.sportstore.model.AuthResponse;
import com.sportstore.model.User;
import com.sportstore.service.AuthService;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // API 1: Đăng ký
    // URL: POST http://localhost:8080/api/auth/register
    @PostMapping("/register")
    public AuthResponse register(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String password,
            HttpSession session
    ) {
        // Kiểm tra dữ liệu
        if (firstName == null || firstName.isEmpty() ||
            lastName == null || lastName.isEmpty() ||
            email == null || email.isEmpty() ||
            password == null || password.isEmpty()) {
            return new AuthResponse(false, "Vui lòng điền đầy đủ thông tin");
        }

        // Kiểm tra email đã tồn tại
        if (authService.getUserByEmail(email) != null) {
            return new AuthResponse(false, "Email đã được đăng ký");
        }

        // Tạo user mới
        User newUser = authService.register(firstName, lastName, email, phone, password);

        if (newUser != null) {
            // Lưu user vào session
            session.setAttribute("userId", newUser.getId());
            session.setAttribute("userEmail", newUser.getEmail());
            session.setAttribute("userName", newUser.getFirstName() + " " + newUser.getLastName());
            session.setAttribute("userAvatar", newUser.getAvatar());

            return new AuthResponse(true, "Đăng ký thành công", newUser);
        } else {
            return new AuthResponse(false, "Đăng ký thất bại");
        }
    }
    

    // API 2: Đăng nhập
    // URL: POST http://localhost:8080/api/auth/login
    @PostMapping("/login")
    public AuthResponse login(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session
    ) {
        // Kiểm tra dữ liệu
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            return new AuthResponse(false, "Vui lòng điền đầy đủ thông tin");
        }

        // Kiểm tra đăng nhập
        User user = authService.login(email, password);

        if (user != null) {
            // Lưu user vào session
            session.setAttribute("userId", user.getId());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userName", user.getFirstName() + " " + user.getLastName());
            session.setAttribute("userAvatar", user.getAvatar());

            return new AuthResponse(true, "Đăng nhập thành công", user);
        } else {
            return new AuthResponse(false, "Email hoặc mật khẩu sai");
        }
    }

    // API 3: Lấy thông tin user hiện tại
    // URL: GET http://localhost:8080/api/auth/me
    @GetMapping("/me")
    public AuthResponse getCurrentUser(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            return new AuthResponse(false, "Chưa đăng nhập");
        }

        User user = authService.getUserById(userId);
        if (user != null) {
            return new AuthResponse(true, "Lấy thông tin thành công", user);
        } else {
            return new AuthResponse(false, "Không tìm thấy user");
        }
    }

    // API 4: Đăng xuất
    // URL: POST http://localhost:8080/api/auth/logout
    @PostMapping("/logout")
    public AuthResponse logout(HttpSession session) {
        session.invalidate();
        return new AuthResponse(true, "Đăng xuất thành công");
    }

    // API 5: Cập nhật thông tin user
    // URL: PUT http://localhost:8080/api/auth/update
    @PutMapping("/update")
    public AuthResponse updateUser(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String phone,
            HttpSession session
    ) {
        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            return new AuthResponse(false, "Chưa đăng nhập");
        }

        User updatedUser = authService.updateUser(userId, firstName, lastName, phone);

        if (updatedUser != null) {
            // Cập nhật session
            session.setAttribute("userName", updatedUser.getFirstName() + " " + updatedUser.getLastName());
            session.setAttribute("userAvatar", updatedUser.getAvatar());

            return new AuthResponse(true, "Cập nhật thành công", updatedUser);
        } else {
            return new AuthResponse(false, "Cập nhật thất bại");
        }
    }
    // API: Đồng bộ session (thêm vào AuthController.java)
    // URL: POST http://localhost:8080/api/auth/sync-session
    @PostMapping("/sync-session")
    public ResponseEntity<Map<String, Object>> syncSession(
            @RequestBody Map<String, String> request,
            HttpSession session) {
        
        String email = request.get("email");
        
        System.out.println("=== SYNC SESSION ===");
        System.out.println("Email from request: " + email);
        System.out.println("Current session ID: " + session.getId());
        
        if (email == null || email.isEmpty()) {
            System.out.println("Email is null or empty");
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Email is required");
            return ResponseEntity.badRequest().body(response);
        }
        
        User user = authService.getUserByEmail(email);
        
        if (user != null) {
            // Force set session attributes
            session.setAttribute("userId", user.getId());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userName", user.getFirstName() + " " + user.getLastName());
            session.setAttribute("userAvatar", user.getAvatar());
            
            System.out.println("✓ Session synced for user: " + user.getEmail());
            System.out.println("Session userId: " + session.getAttribute("userId"));
            System.out.println("Session userEmail: " + session.getAttribute("userEmail"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("synced", true);
            response.put("message", "Session synced successfully");
            return ResponseEntity.ok(response);
        } else {
            System.out.println("User not found for email: " + email);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "User not found");
            return ResponseEntity.ok(response);
        }
    }
}
