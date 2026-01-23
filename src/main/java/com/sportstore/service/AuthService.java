package com.sportstore.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sportstore.model.User;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AuthService {
    
    private Map<String, User> users = new HashMap<>();
    private AtomicInteger userIdCounter = new AtomicInteger(1);
    private final ObjectMapper objectMapper;
    private final Path usersFilePath;

    public AuthService() {
        // Khởi tạo ObjectMapper với JavaTimeModule để xử lý LocalDateTime
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        
        // Đường dẫn file users.json
        this.usersFilePath = Paths.get("src/main/resources/data/users.json");
        
        // Load users từ file
        loadUsersFromFile();
    }
    
    /**
     * Đọc danh sách users từ file JSON
     */
    private void loadUsersFromFile() {
        try {
            // Thử đọc từ classpath trước (khi chạy từ JAR)
            InputStream inputStream = getClass().getResourceAsStream("/data/users.json");
            
            List<User> userList;
            if (inputStream != null) {
                userList = objectMapper.readValue(inputStream, new TypeReference<List<User>>() {});
            } else if (Files.exists(usersFilePath)) {
                // Nếu không tìm thấy trong classpath, đọc từ file system
                userList = objectMapper.readValue(usersFilePath.toFile(), new TypeReference<List<User>>() {});
            } else {
                System.out.println("File users.json không tồn tại. Tạo danh sách rỗng.");
                userList = new ArrayList<>();
            }
            
            // Load vào Map
            for (User user : userList) {
                users.put(user.getEmail(), user);
                if (user.getId() >= userIdCounter.get()) {
                    userIdCounter.set(user.getId() + 1);
                }
            }
            
            System.out.println("✓ Đã load " + users.size() + " users từ file");
            
        } catch (IOException e) {
            System.err.println("Lỗi đọc file users.json: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Lưu danh sách users vào file JSON
     */
    private void saveUsersToFile() {
        try {
            // Tạo thư mục nếu chưa tồn tại
            Files.createDirectories(usersFilePath.getParent());
            
            // Ghi file
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(usersFilePath.toFile(), users.values());
            
            System.out.println("✓ Đã lưu " + users.size() + " users vào file");
            
        } catch (IOException e) {
            System.err.println("Lỗi ghi file users.json: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Đăng ký
    public User register(String firstName, String lastName, String email, String phone, String password) {
        // Kiểm tra email đã tồn tại
        if (users.containsKey(email)) {
            return null; // Email đã được đăng ký
        }

        // Tạo user mới
        User newUser = new User(firstName, lastName, email, phone, password);
        newUser.setId(userIdCounter.getAndIncrement());
        users.put(email, newUser);

        // Lưu vào file
        saveUsersToFile();

        return newUser;
    }

    // Đăng nhập
    public User login(String email, String password) {
        User user = users.get(email);

        if (user == null) {
            return null; // Email không tồn tại
        }

        // Kiểm tra password
        if (!user.getPassword().equals(password)) {
            return null; // Password sai
        }

        return user;
    }

    // Lấy user theo email
    public User getUserByEmail(String email) {
        return users.get(email);
    }

    // Lấy user theo ID
    public User getUserById(int id) {
        return users.values().stream()
                .filter(u -> u.getId() == id)
                .findFirst()
                .orElse(null);
    }

    // Lấy toàn bộ users (admin)
    public Collection<User> getAllUsers() {
        return users.values();
    }

    // Cập nhật thông tin user
    public User updateUser(int id, String firstName, String lastName, String phone) {
        User user = getUserById(id);
        if (user != null) {
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPhone(phone);
            // Cập nhật avatar
            user.setAvatar("https://ui-avatars.com/api/?name=" + firstName + "+" + lastName + "&background=00ff66&color=000");
            
            // Lưu vào file
            saveUsersToFile();
        }
        return user;
    }
    
    // Cập nhật địa chỉ user
    public User updateUserAddress(int id, String address) {
        User user = getUserById(id);
        if (user != null) {
            user.setAddress(address);
            saveUsersToFile();
        }
        return user;
    }
    
    // Đổi mật khẩu
    public boolean changePassword(int id, String newPassword) {
        User user = getUserById(id);
        if (user != null) {
            user.setPassword(newPassword);
            saveUsersToFile();
            return true;
        }
        return false;
    }
}
