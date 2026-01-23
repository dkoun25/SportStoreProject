# 🛒 API Documentation - Apex Sports Backend

## Tổng Quan
Backend được xây dựng bằng **Spring Boot 3.1.0** với Java 17.

Đặc điểm chính:
- ✅ **Không cần đăng nhập** - Sử dụng Session ID để phân biệt người dùng
- ✅ **Tính tiền tự động** - Backend tính toán subtotal, shipping, tax, total
- ✅ **Quản lý giỏ hàng** - Thêm, sửa, xóa sản phẩm
- ✅ **RESTful API** - Dễ dàng integrate với frontend

---

## 📦 API Endpoints

### 1. Lấy Danh Sách Sản Phẩm
```
GET /api/products
```
**Mô tả**: Lấy toàn bộ danh sách sản phẩm

**Response**:
```json
[
  {
    "id": 1,
    "name": "Running Shoes Pro",
    "category": "Men's Shoes",
    "price": 1500000,
    "image": "https://..."
  }
]
```

---

### 2. Lấy Chi Tiết 1 Sản Phẩm
```
GET /api/products/{id}
```
**Ví dụ**: `GET /api/products/1`

**Response**:
```json
{
  "id": 1,
  "name": "Running Shoes Pro",
  "category": "Men's Shoes",
  "price": 1500000,
  "image": "https://..."
}
```

---

### 3. Lấy Giỏ Hàng Hiện Tại
```
GET /api/cart
```
**Mô tả**: Lấy giỏ hàng của user (dùng Session ID)

**Response**:
```json
{
  "items": [
    {
      "id": 1,
      "name": "Running Shoes Pro",
      "category": "Men's Shoes",
      "price": 1500000,
      "image": "https://...",
      "quantity": 2,
      "size": "M"
    }
  ],
  "subtotal": 3000000,
  "shipping": 0,
  "tax": 300000,
  "total": 3300000,
  "itemCount": 1
}
```

---

### 4. Thêm Sản Phẩm vào Giỏ
```
POST /api/cart/add
Content-Type: application/json
```

**Body**:
```json
{
  "id": 1,
  "name": "Running Shoes Pro",
  "category": "Men's Shoes",
  "price": 1500000,
  "image": "https://...",
  "quantity": 2,
  "size": "M"
}
```

**Response**: Trả về CartResponse với thông tin mới

**Lưu ý**:
- Nếu sản phẩm đã tồn tại (cùng `id` + `size`), hệ thống sẽ cộng thêm `quantity`
- Mặc định `size` là "M" nếu không truyền

---

### 5. Cập Nhật Số Lượng
```
PUT /api/cart/update/{productId}?quantity={newQuantity}&size={size}
```

**Ví dụ**: `PUT /api/cart/update/1?quantity=5&size=L`

**Response**: Trả về CartResponse cập nhật

---

### 6. Xóa Sản Phẩm khỏi Giỏ
```
DELETE /api/cart/remove/{productId}?size={size}
```

**Ví dụ**: `DELETE /api/cart/remove/1?size=M`

**Response**: Trả về CartResponse sau khi xóa

---

### 7. Xóa Toàn Bộ Giỏ
```
DELETE /api/cart/clear
```

**Response**: `200 OK` (no content)

---

### 8. Lấy Tổng Số Lượng Sản Phẩm
```
GET /api/cart/count
```

**Response**:
```
5
```
(trả về số nguyên - tổng quantity của tất cả items)

---

### 9. Kiểm Tra Trạng Thái Giỏ
```
GET /api/cart/status
```

**Response**: Trả về CartResponse (tương tự `GET /api/cart`)

---

### 10. Thanh Toán (Checkout)
```
POST /api/cart/checkout
```

**Response**: Trả về CartResponse của đơn hàng vừa thanh toán, sau đó xóa giỏ

**Lưu ý**:
- Backend giả lập xử lý thanh toán
- Cần implement: Lưu Order vào Database, gọi Payment Gateway (VNPay, Paypal)
- Giỏ hàng sẽ bị xóa sau checkout

---

## 💡 Logic Tính Tiền

```
Subtotal = Σ (price × quantity) của tất cả items

Shipping:
  - Nếu Subtotal ≥ 500,000 VND → Miễn phí (0 VND)
  - Nếu Subtotal < 500,000 VND → 30,000 VND

Tax = Subtotal × 10% (làm tròn xuống)

Total = Subtotal + Shipping + Tax
```

**Ví dụ**:
```
Item 1: Giày × 1 = 1,500,000
Item 2: Áo × 2 = 400,000

Subtotal: 1,900,000
Shipping: 0 (vì >= 500k)
Tax: 190,000
Total: 2,090,000
```

---

## 📝 Session Management

**Session ID** được tự động tạo bởi Spring Boot
- Mỗi người dùng có một Session ID riêng
- Session ID được lưu trong Cookie `JSESSIONID`
- Giỏ hàng được lưu trong **Memory** (Map trong CartService)

**Lưu ý**: 
- Session hiện tại được lưu trong RAM, khi server restart sẽ mất dữ liệu
- Để lưu trữ vĩnh viễn, cần:
  - Lưu vào Database (JPA/Hibernate)
  - Sử dụng Redis (cho session caching)
  - Yêu cầu người dùng đăng nhập

---

## 🔧 Chạy Backend

```bash
# 1. Compile Maven
mvn clean install

# 2. Run Spring Boot
mvn spring-boot:run

# 3. Hoặc chạy file JAR
java -jar target/sportstore-1.0.0.jar
```

Server sẽ chạy tại: **http://localhost:8080**

---

## 📱 Frontend Integration

### Thêm Sản Phẩm vào Giỏ

```javascript
const cartItem = {
    id: product.id,
    name: product.name,
    category: product.category,
    price: product.price,
    image: product.image,
    quantity: 1,
    size: 'M'
};

fetch('/api/cart/add', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(cartItem)
})
.then(res => res.json())
.then(cartResponse => {
    console.log('Giỏ hàng:', cartResponse);
    console.log('Tổng tiền:', cartResponse.total);
});
```

### Lấy Giỏ Hàng

```javascript
fetch('/api/cart')
    .then(res => res.json())
    .then(cartResponse => {
        console.log('Items:', cartResponse.items);
        console.log('Subtotal:', cartResponse.subtotal);
        console.log('Shipping:', cartResponse.shipping);
        console.log('Tax:', cartResponse.tax);
        console.log('Total:', cartResponse.total);
    });
```

---

## ⚠️ Error Handling

### Possible Errors

**400 Bad Request** - Dữ liệu không hợp lệ
```json
{
  "error": "Product ID is invalid"
}
```

**404 Not Found** - Sản phẩm không tìm thấy
```json
{
  "error": "Product not found"
}
```

**500 Internal Server Error** - Lỗi server

---

## 🚀 Future Enhancements

- [ ] Database integration (JPA/Hibernate)
- [ ] User authentication & authorization
- [ ] Order history
- [ ] Promo code validation
- [ ] Payment gateway integration (VNPay, Paypal)
- [ ] Redis for session caching
- [ ] Cart persistence
- [ ] Admin dashboard

---

## 📞 Support

Nếu có vấn đề, kiểm tra:
1. Server có running ở http://localhost:8080 không?
2. Browser console có lỗi gì không?
3. Network tab - API responses
4. Server logs để tìm lỗi backend
