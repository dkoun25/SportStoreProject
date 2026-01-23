package com.sportstore.controller;

import com.sportstore.service.RatingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {
    
    @Autowired
    private RatingService ratingService;
    
    /**
     * Submit or update a rating for a product
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitRating(
            @RequestBody Map<String, Object> payload,
            HttpSession session
    ) {
        String userEmail = (String) session.getAttribute("userEmail");
        
        if (userEmail == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Vui lòng đăng nhập để đánh giá"));
        }
        
        String productId = (String) payload.get("productId");
        Integer rating = (Integer) payload.get("rating");
        
        if (productId == null || rating == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Thiếu thông tin sản phẩm hoặc điểm đánh giá"));
        }
        
        boolean success = ratingService.submitRating(productId, userEmail, rating);
        
        if (!success) {
            return ResponseEntity.badRequest().body(Map.of("error", "Điểm đánh giá không hợp lệ (1-5)"));
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đánh giá thành công");
        response.put("averageRating", ratingService.getAverageRating(productId));
        response.put("ratingCount", ratingService.getRatingCount(productId));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get ratings for a product
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getProductRatings(
            @PathVariable String productId,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();
        response.put("averageRating", ratingService.getAverageRating(productId));
        response.put("ratingCount", ratingService.getRatingCount(productId));
        
        String userEmail = (String) session.getAttribute("userEmail");
        if (userEmail != null) {
            response.put("userRating", ratingService.getUserRating(productId, userEmail));
        } else {
            response.put("userRating", 0);
        }
        
        return ResponseEntity.ok(response);
    }
}
