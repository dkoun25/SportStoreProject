document.addEventListener("DOMContentLoaded", () => {
    const productContainer = document.getElementById("product-list");

    // Hàm định dạng tiền tệ Việt Nam
    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
    };

    // Gọi API từ Backend Java
    fetch('/api/products')
        .then(response => {
            if (!response.ok) throw new Error("Lỗi kết nối server");
            return response.json();
        })
        .then(products => {
            productContainer.innerHTML = ""; // Xóa text 'Đang tải...'

            products.forEach(product => {
                const card = document.createElement("div");
                card.className = "product-card";

                card.innerHTML = `
                    <img src="${product.image}" alt="${product.name}">
                    <h3>${product.name}</h3>
                    <p class="category">${product.category}</p>
                    <p class="price">${formatCurrency(product.price)}</p>
                    <button class="btn-buy" onclick="addToCart(${product.id})">Thêm vào giỏ</button>
                `;
                productContainer.appendChild(card);
            });
        })
        .catch(error => {
            console.error('Error:', error);
            productContainer.innerHTML = "<p style='color:red'>Không thể tải sản phẩm.</p>";
        });
});

function addToCart(id) {
    alert("Đã thêm sản phẩm ID: " + id + " vào giỏ hàng (Giả lập)!");
}