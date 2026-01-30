// URL API
const API_URL = "/api/products";
const CART_API_URL = "/api/cart";
const AUTH_API_URL = "/api/auth";
const ITEMS_PER_PAGE = 9; // Hiển thị 9 sản phẩm (Lưới 3x3)
let allProductsData = [];
let currentPage = 1;

// Link ảnh dự phòng (Hiện ra khi ảnh gốc bị lỗi/die link)
const BACKUP_IMAGE = "https://placehold.co/600x800?text=Apex+Sports+Item&font=roboto";

const formatMoney = (amount) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
};

// Hàm tạo thẻ IMG an toàn (Tự động thay thế khi lỗi)
function getImageTag(src, alt, style="") {
    return `<img src="${src}" alt="${alt}" style="${style}" onerror="this.onerror=null; this.src='${BACKUP_IMAGE}';">`;
}

// Update navbar khi user login/logout
function updateUserNav() {
    const authButtons = document.querySelector('.auth-buttons');
    if (!authButtons) return;

    // Kiểm tra session từ backend trước, đồng bộ với localStorage
    fetch(`${AUTH_API_URL}/me`, { credentials: 'include' })
        .then(res => res.json())
        .then(data => {
            if (data.success && data.user) {
                // Session hợp lệ - cập nhật localStorage và hiển thị avatar
                const user = data.user;
                localStorage.setItem('user', JSON.stringify(user));
                renderUserNavbar(user, authButtons);
            } else {
                // Session không hợp lệ - xóa localStorage và hiển thị nút login
                localStorage.removeItem('user');
                renderGuestNavbar(authButtons);
            }
        })
        .catch(err => {
            console.log('Không thể kiểm tra session:', err);
            // Nếu có lỗi, fallback dùng localStorage
            const user = JSON.parse(localStorage.getItem('user'));
            if (user && user.avatar) {
                renderUserNavbar(user, authButtons);
            } else {
                renderGuestNavbar(authButtons);
            }
        });
}

// Render navbar cho user đã đăng nhập
function renderUserNavbar(user, authButtons) {
    authButtons.innerHTML = `
        <div style="display: flex; align-items: center; gap: 15px;">
            <span style="color: #ddd; font-size: 0.9rem;">${user.firstName || user.email}</span>
            <a href="user.html" title="Quản lý tài khoản">
                <img src="${user.avatar}" 
                     alt="Avatar" 
                     title="${user.firstName} ${user.lastName}"
                     style="width: 40px; height: 40px; border-radius: 50%; border: 2px solid var(--accent-green); cursor: pointer; object-fit: cover; transition: transform 0.3s;"
                     onmouseover="this.style.transform='scale(1.1)'"
                     onmouseout="this.style.transform='scale(1)'"
                     id="userAvatar"
                >
            </a>
            <button onclick="logout()" style="background: #ff4444; color: white; border: none; padding: 6px 12px; border-radius: 4px; cursor: pointer; font-size: 0.85rem;">Đăng xuất</button>
        </div>
    `;
}

// Render navbar cho khách (chưa đăng nhập)
function renderGuestNavbar(authButtons) {
    authButtons.innerHTML = `
        <a href="login.html">Đăng nhập</a> | <a href="register.html">Đăng ký</a>
    `;
}

// Logout function
function logout() {
    fetch(`${AUTH_API_URL}/logout`, { method: 'POST', credentials: 'include' })
        .then(res => res.json())
        .then(() => {
            localStorage.removeItem('user');
            window.location.href = 'index.html';
        })
        .catch(err => console.error('Lỗi logout:', err));
}

document.addEventListener("DOMContentLoaded", () => {
    handleSearchBox();
    updateCartCount();
    
    // Đồng bộ session trước khi update navbar
    // (updateUserNav đã tự động fetch /api/auth/me)
    updateUserNav();

    const path = window.location.pathname;

    if (path.includes("product-detail.html")) {
        loadProductDetail();
    } else if (path.includes("index.html") || path === "/") {
        loadHomeProducts();
    } else {
        loadCategoryPage(path);
    }
});

function loadCategoryPage(path) {
    fetch(API_URL)
        .then(res => res.json())
        .then(products => {
            // --- LOGIC LỌC SẢN PHẨM CHUẨN ---
            
            // 1. Kiểm tra Nữ trước
            if (path.includes("women.html")) {
                allProductsData = products.filter(p => p.category === "women");
                const titleEl = document.querySelector(".section-title");
                if (titleEl) titleEl.innerText = "Thời trang Nữ";
            } 
            // 2. Kiểm tra Nam
            else if (path.includes("men.html")) {
                allProductsData = products.filter(p => p.category === "men");
                const titleEl = document.querySelector(".section-title");
                if (titleEl) titleEl.innerText = "Thời trang Nam";
            }
            // 3. Phụ kiện
            else if (path.includes("accessories.html")) {
                allProductsData = products.filter(p => p.category === "accessories");
                const titleEl = document.querySelector(".section-title");
                if (titleEl) titleEl.innerText = "Phụ kiện & Dụng cụ";
            }
            // 4. Tìm kiếm
            else if (path.includes("search.html")) {
                const q = new URLSearchParams(window.location.search).get('q');
                if(q) {
                    const k = q.toLowerCase();
                    allProductsData = products.filter(p => 
                        p.name.toLowerCase().includes(k) || 
                        p.category.toLowerCase().includes(k)
                    );
                    const titleEl = document.querySelector(".section-title");
                    if (titleEl) titleEl.innerText = `Kết quả: "${q}"`;
                }
            }

            // Reset về trang 1 và hiển thị
            currentPage = 1;
            renderPaginatedProducts();
            setupPaginationControls();
        });
}

function renderPaginatedProducts() {
    const container = document.querySelector(".product-grid-search");
    const countEl = document.getElementById("result-count");
    if(!container) return;

    // Ép kiểu hiển thị 3 cột
    container.style.gridTemplateColumns = "repeat(3, 1fr)"; 

    const start = (currentPage - 1) * ITEMS_PER_PAGE;
    const end = start + ITEMS_PER_PAGE;
    const productsToShow = allProductsData.slice(start, end);

    if(countEl) countEl.innerText = `Hiển thị ${productsToShow.length} trên tổng ${allProductsData.length} sản phẩm`;

    container.innerHTML = "";
    if (productsToShow.length === 0) {
        container.innerHTML = `<h3 style="grid-column:1/-1;text-align:center; margin-top: 50px; color: #777;">Không tìm thấy sản phẩm phù hợp.</h3>`;
        return;
    }

    productsToShow.forEach(p => {
        container.innerHTML += `
            <div class="product-card" style="box-shadow: 0 2px 10px rgba(0,0,0,0.05);">
                <a href="product-detail.html?id=${p.id}" style="display:block;height:260px;overflow:hidden; position: relative;">
                    ${getImageTag(p.image, p.name, "width:100%;height:100%;object-fit:cover; transition: transform 0.5s ease;")}
                </a>
                <div class="product-info" style="padding: 15px;">
                    <p class="category" style="font-size:0.75rem;color:#888;text-transform:uppercase;margin-bottom:5px;">${p.category}</p>
                    <h3 style="font-size:1.05rem;margin:5px 0;height:45px;overflow:hidden; line-height: 1.4;">${p.name}</h3>
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-top: 10px;">
                        <p class="price" style="font-weight:bold;color:#111; font-size: 1.1rem;">${formatMoney(p.price)}</p>
                        <a href="product-detail.html?id=${p.id}" style="font-size:1.2rem; color: var(--accent-green);">&#10132;</a>
                    </div>
                </div>
            </div>
        `;
    });
    
    // Hiệu ứng hover phóng to ảnh
    const images = container.querySelectorAll("img");
    images.forEach(img => {
        img.parentElement.addEventListener("mouseenter", () => img.style.transform = "scale(1.1)");
        img.parentElement.addEventListener("mouseleave", () => img.style.transform = "scale(1)");
    });

    updatePaginationButtons();
}

function updatePaginationButtons() {
    const prevBtn = document.getElementById("prevBtn");
    const nextBtn = document.getElementById("nextBtn");
    const pageInfo = document.getElementById("pageInfo");
    const totalPages = Math.ceil(allProductsData.length / ITEMS_PER_PAGE);

    if(pageInfo) pageInfo.innerText = `Trang ${currentPage} / ${totalPages > 0 ? totalPages : 1}`;

    if(prevBtn) {
        prevBtn.disabled = currentPage === 1;
        prevBtn.style.opacity = currentPage === 1 ? "0.5" : "1";
    }
    if(nextBtn) {
        nextBtn.disabled = currentPage >= totalPages || totalPages === 0;
        nextBtn.style.opacity = (currentPage >= totalPages || totalPages === 0) ? "0.5" : "1";
    }
}

function setupPaginationControls() {
    const prevBtn = document.getElementById("prevBtn");
    const nextBtn = document.getElementById("nextBtn");

    if(prevBtn) {
        const newPrev = prevBtn.cloneNode(true); // Xóa event cũ
        prevBtn.parentNode.replaceChild(newPrev, prevBtn);
        newPrev.addEventListener("click", () => {
            if (currentPage > 1) {
                currentPage--;
                renderPaginatedProducts();
                window.scrollTo({ top: 0, behavior: 'smooth' });
            }
        });
    }

    if(nextBtn) {
        const newNext = nextBtn.cloneNode(true); // Xóa event cũ
        nextBtn.parentNode.replaceChild(newNext, nextBtn);
        newNext.addEventListener("click", () => {
            const totalPages = Math.ceil(allProductsData.length / ITEMS_PER_PAGE);
            if (currentPage < totalPages) {
                currentPage++;
                renderPaginatedProducts();
                window.scrollTo({ top: 0, behavior: 'smooth' });
            }
        });
    }
}

function loadProductDetail() {
    const params = new URLSearchParams(window.location.search);
    const id = params.get('id');
    if (!id) return;
    
    fetch(`${API_URL}/${id}`)
        .then(res => res.json())
        .then(p => {
            document.querySelector(".product-title").innerText = p.name;
            document.querySelector(".product-price-large").innerText = formatMoney(p.price);
            
            // Xử lý ảnh chi tiết (Thêm Fallback)
            const imgEl = document.querySelector(".main-image-container img");
            imgEl.src = p.image;
            imgEl.onerror = function() { this.src = BACKUP_IMAGE; };
            
            document.querySelector(".tag").innerText = p.category;
            document.querySelector(".description").innerText = `Mô tả: ${p.name} - Sản phẩm chất lượng cao từ dòng ${p.category}.`;
        })
        .catch(err => {
            console.error("Không tìm thấy sản phẩm");
        });
}

function loadHomeProducts() {
    fetch(API_URL).then(res=>res.json()).then(products => {
        // Sắp xếp theo ID giảm dần (mới nhất trước)
        products.sort((a, b) => b.id - a.id);
        
        // Phân loại sản phẩm theo category (chữ thường)
        const menProducts = products.filter(p => p.category === "men");
        const womenProducts = products.filter(p => p.category === "women");
        const accessoriesProducts = products.filter(p => p.category === "accessories");
        
        // Lấy sản phẩm có giảm giá
        const discountedProducts = products.filter(p => p.discountPercent && p.discountPercent > 0);
        
        // Featured/Siêu Khuyến Mãi - 5 sản phẩm có giảm giá hoặc mới nhất
        const featured = discountedProducts.slice(0, 5).length >= 5 
            ? discountedProducts.slice(0, 5) 
            : [...discountedProducts, ...products.slice(0, 5 - discountedProducts.length)];
        renderProductSection('#featuredProducts', featured, 'product-card-home');
        
        // Giày Sneaker - 4 sản phẩm men
        renderProductSection('#sneakerProducts', menProducts.slice(0, 4), 'product-card-home');
        
        // Túi Mũ - 4 sản phẩm accessories
        renderProductSection('#bagHatProducts', accessoriesProducts.slice(0, 4), 'product-card-home');
        
        // Sản phẩm nữ - 4 sản phẩm women
        renderProductSection('#airJordanProducts', womenProducts.slice(0, 4), 'product-card-home');
        
        // Nước Hoa - 4 sản phẩm accessories tiếp theo
        renderProductSection('#perfumeProducts', accessoriesProducts.slice(4, 8), 'product-card-home');
        
        // Mũ - 4 sản phẩm accessories tiếp theo
        renderProductSection('#hatProducts', accessoriesProducts.slice(8, 12), 'product-card-home');
    });
}

function renderProductSection(selectorId, productList, cardClass = 'product-card') {
    const container = document.querySelector(selectorId);
    if (!container) return;
    
    container.innerHTML = "";
    
    if (productList.length === 0) {
        container.innerHTML = `<p style="grid-column:1/-1;text-align:center;color:#777;">Không có sản phẩm</p>`;
        return;
    }
    
    productList.forEach(p => {
        const dp = p.discountPercent || 0;
        const originalPrice = p.price;
        const oldPrice = dp > 0 ? Math.round(originalPrice / (1 - dp/100)) : null;
        
        container.innerHTML += `
            <div class="${cardClass}">
                <a href="product-detail.html?id=${p.id}" style="display:block;position:relative;">
                    ${getImageTag(p.image, p.name, "width:100%;height:100%;object-fit:cover;")}
                    ${dp > 0 ? `<span class=\"discount-tag\">-${dp}%</span>` : ''}
                </a>
                <div class="product-card-home-info">
                    <h3>${p.name}</h3>
                    <div class="price">${formatMoney(originalPrice)}</div>
                    ${oldPrice ? `<div class=\"old-price\">${formatMoney(oldPrice)}</div>` : ''}
                </div>
            </div>`;
    });
}

function handleSearchBox() {
    const input = document.getElementById("searchInput");
    if(input) input.addEventListener("keypress", (e) => {
        if(e.key === "Enter" && input.value.trim()) window.location.href = `search.html?q=${encodeURIComponent(input.value.trim())}`;
    });
}

function updateCartCount() {
    // Lấy số lượng từ backend API
    fetch(CART_API_URL + '/count', { credentials: 'include' })
        .then(res => res.json())
        .then(count => {
            const b = document.querySelector(".cart-count");
            if(b) b.innerText = count;
        })
        .catch(err => {
            console.log('Không thể tải cart count:', err);
            const b = document.querySelector(".cart-count");
            if(b) b.innerText = '0';
        });
}

// Hàm thêm sản phẩm vào giỏ hàng (gọi API Backend)
function addToCart(product, quantity = 1, size = null, color = null) {
    const cartItem = {
        id: product.id,
        name: product.name,
        category: product.category,
        price: product.price,
        image: product.image,
        quantity: quantity,
        size: size,
        color: color
    };

    console.log('Gửi sản phẩm đến cart:', cartItem);

    fetch(CART_API_URL + '/add', {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(cartItem)
    })
    .then(res => {
        console.log('Response status:', res.status);
        return res.json();
    })
    .then(data => {
        console.log('Phản hồi từ server:', data);
        showAddToCartNotification(product.name, size, color);
        updateCartCount();
    })
    .catch(err => {
        console.error('Lỗi khi thêm vào giỏ:', err);
        alert('Có lỗi khi thêm sản phẩm vào giỏ. Vui lòng thử lại.');
    });
}

function showAddToCartNotification(productName, size, color) {
    const notif = document.createElement('div');
    notif.style.cssText = `
        position: fixed;
        top: 100px;
        right: 20px;
        background-color: var(--accent-green);
        color: black;
        padding: 15px 20px;
        border-radius: 6px;
        font-weight: 600;
        z-index: 9999;
        animation: slideIn 0.3s ease;
    `;
    
    let details = '';
    if (size) details += ` - Size: ${size}`;
    if (color) details += ` - Màu: ${color}`;
    
    notif.textContent = `✓ Đã thêm "${productName}${details}" vào giỏ hàng`;
    document.body.appendChild(notif);
    
    setTimeout(() => {
        notif.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => notif.remove(), 300);
    }, 2000);
}

// Add CSS animation for notification
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from { transform: translateX(400px); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
    @keyframes slideOut {
        from { transform: translateX(0); opacity: 1; }
        to { transform: translateX(400px); opacity: 0; }
    }
`;
document.head.appendChild(style);