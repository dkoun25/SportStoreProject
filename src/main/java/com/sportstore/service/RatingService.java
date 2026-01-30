package com.sportstore.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RatingService {
    
    private final Map<String, Map<String, Integer>> ratings = new ConcurrentHashMap<>();
    // Structure: { "productId": { "userEmail": rating } }
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public RatingService() {
        loadRatingsFromFile();
    }
    
    private void loadRatingsFromFile() {
        try {
            File file = new File("src/main/resources/data/ratings.json");
            
            if (file.exists()) {
                // Load from file system
                Map<String, Map<String, Integer>> loaded = objectMapper.readValue(
                    file, 
                    new TypeReference<Map<String, Map<String, Integer>>>() {}
                );
                ratings.putAll(loaded);
                System.out.println("Loaded " + ratings.size() + " product ratings from file");
            } else {
                // Try loading from classpath
                InputStream is = getClass().getResourceAsStream("/data/ratings.json");
                if (is != null) {
                    Map<String, Map<String, Integer>> loaded = objectMapper.readValue(
                        is, 
                        new TypeReference<Map<String, Map<String, Integer>>>() {}
                    );
                    ratings.putAll(loaded);
                    System.out.println("Loaded " + ratings.size() + " product ratings from classpath");
                } else {
                    System.out.println("No ratings file found, starting with empty ratings");
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading ratings: " + e.getMessage());
        }
    }
    
    private void saveRatingsToFile() {
        try {
            File file = new File("src/main/resources/data/ratings.json");
            file.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, ratings);
            System.out.println("Saved ratings to file");
        } catch (Exception e) {
            System.err.println("Error saving ratings: " + e.getMessage());
        }
    }
    
    /**
     * Submit or update a user's rating for a product
     * @param productId Product ID
     * @param userEmail User's email
     * @param rating Rating value (1-5)
     * @return true if successful
     */
    public boolean submitRating(String productId, String userEmail, int rating) {
        if (rating < 1 || rating > 5) {
            return false;
        }
        
        ratings.computeIfAbsent(productId, k -> new ConcurrentHashMap<>());
        ratings.get(productId).put(userEmail, rating);
        saveRatingsToFile();
        return true;
    }
    
    /**
     * Get all ratings for a product
     * @param productId Product ID
     * @return Map of userEmail -> rating
     */
    public Map<String, Integer> getProductRatings(String productId) {
        return ratings.getOrDefault(productId, new HashMap<>());
    }
    
    /**
     * Get average rating for a product
     * @param productId Product ID
     * @return Average rating (0 if no ratings)
     */
    public double getAverageRating(String productId) {
        Map<String, Integer> productRatings = ratings.get(productId);
        
        if (productRatings == null || productRatings.isEmpty()) {
            return 0.0;
        }
        
        double sum = productRatings.values().stream().mapToInt(Integer::intValue).sum();
        return sum / productRatings.size();
    }
    
    /**
     * Get rating count for a product
     * @param productId Product ID
     * @return Number of ratings
     */
    public int getRatingCount(String productId) {
        Map<String, Integer> productRatings = ratings.get(productId);
        return productRatings == null ? 0 : productRatings.size();
    }
    
    /**
     * Get user's rating for a product
     * @param productId Product ID
     * @param userEmail User's email
     * @return Rating value (0 if not rated)
     */
    public int getUserRating(String productId, String userEmail) {
        Map<String, Integer> productRatings = ratings.get(productId);
        
        if (productRatings == null) {
            return 0;
        }
        
        return productRatings.getOrDefault(userEmail, 0);
    }
}
