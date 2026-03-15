/**
 * Fresh Farm Juba - Main JavaScript File
 * Handles all interactive functionality across the website
 */

// ==================== GLOBAL STATE MANAGEMENT ====================
let cart = JSON.parse(localStorage.getItem('cart')) || [];
let wishlist = JSON.parse(localStorage.getItem('wishlist')) || [];
let currentUser = JSON.parse(localStorage.getItem('currentUser')) || null;
let recentOrders = JSON.parse(localStorage.getItem('recentOrders')) || [];
let notifications = [];

// ==================== INITIALIZATION ====================
document.addEventListener('DOMContentLoaded', function() {
    console.log('Fresh Farm Juba - Initializing...');

    // Initialize all components
    initializeNavigation();
    initializeCart();
    initializeWishlist();
    initializeForms();
    initializeProductCards();
    initializeSearch();
    initializeUserMenu();
    initializeAnimations();
    initializeTooltips();
    initializeModals();
    initializeCheckout();
    initializeAdminPanel();
    loadUserPreferences();
    updateUIForUser();

    console.log('Fresh Farm Juba - Initialization complete');
});

// ==================== NAVIGATION ====================
function initializeNavigation() {
    // Mobile menu toggle
    const mobileMenuToggle = document.getElementById('mobileMenuToggle');
    const mobileMenu = document.getElementById('mobileMenu');

    if (mobileMenuToggle && mobileMenu) {
        mobileMenuToggle.addEventListener('click', function() {
            mobileMenu.classList.toggle('show');
            document.body.classList.toggle('menu-open');
        });
    }

    // Navbar scroll effect
    const navbar = document.getElementById('mainNavbar');
    if (navbar) {
        window.addEventListener('scroll', function() {
            if (window.scrollY > 50) {
                navbar.classList.add('navbar-scrolled', 'shadow');
            } else {
                navbar.classList.remove('navbar-scrolled', 'shadow');
            }
        });
    }

    // Active link highlighting
    const currentPath = window.location.pathname;
    document.querySelectorAll('.nav-link').forEach(link => {
        const href = link.getAttribute('href');
        if (href && currentPath.includes(href) && href !== '/') {
            link.classList.add('active');
        } else if (currentPath === '/' && href === '/') {
            link.classList.add('active');
        }
    });

    // Smooth scroll for anchor links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function(e) {
            const targetId = this.getAttribute('href');
            if (targetId !== '#') {
                e.preventDefault();
                const target = document.querySelector(targetId);
                if (target) {
                    target.scrollIntoView({ behavior: 'smooth', block: 'start' });
                }
            }
        });
    });
}

// ==================== CART FUNCTIONALITY ====================
function initializeCart() {
    updateCartCount();
    updateCartPreview();
    setupCartEventListeners();
}

function setupCartEventListeners() {
    // Cart icon click
    const cartIcon = document.querySelector('.cart-icon, [data-cart]');
    if (cartIcon) {
        cartIcon.addEventListener('click', function(e) {
            e.preventDefault();
            toggleCartDropdown();
        });
    }

    // Close cart on outside click
    document.addEventListener('click', function(e) {
        const cartDropdown = document.getElementById('cartDropdown');
        const cartIcon = document.querySelector('.cart-icon, [data-cart]');
        if (cartDropdown && cartIcon && !cartDropdown.contains(e.target) && !cartIcon.contains(e.target)) {
            cartDropdown.classList.remove('show');
        }
    });
}

function toggleCartDropdown() {
    const dropdown = document.getElementById('cartDropdown');
    if (dropdown) {
        dropdown.classList.toggle('show');
        if (dropdown.classList.contains('show')) {
            updateCartPreview();
        }
    }
}

function addToCart(productId, quantity = 1, options = {}) {
    // Find product details from the page or API
    const product = findProductById(productId);

    if (!product) {
        showNotification('Product not found', 'error');
        return;
    }

    // Check if product already in cart
    const existingItem = cart.find(item =>
        item.id === productId &&
        JSON.stringify(item.options) === JSON.stringify(options)
    );

    if (existingItem) {
        existingItem.quantity += quantity;
        showNotification(`Added another ${product.name} to cart`, 'success');
    } else {
        cart.push({
            id: productId,
            name: product.name,
            price: product.price,
            quantity: quantity,
            image: product.image || '/images/default-product.jpg',
            options: options,
            addedAt: new Date().toISOString()
        });
        showNotification(`${product.name} added to cart`, 'success');
    }

    // Animate cart icon
    animateCartIcon();

    // Save to localStorage
    saveCart();

    // Update UI
    updateCartCount();
    updateCartPreview();

    // Track event
    trackEvent('add_to_cart', {
        product_id: productId,
        product_name: product.name,
        quantity: quantity
    });
}

function findProductById(productId) {
    // Try to find product in the DOM
    const productCard = document.querySelector(`[data-product-id="${productId}"]`);
    if (productCard) {
        return {
            id: productId,
            name: productCard.dataset.productName || 'Product',
            price: parseFloat(productCard.dataset.productPrice) || 0,
            image: productCard.dataset.productImage || '/images/default-product.jpg'
        };
    }

    // Try to find in global products array if it exists
    if (window.products && window.products.length > 0) {
        return window.products.find(p => p.id === productId);
    }

    // Return default
    return {
        id: productId,
        name: `Product ${productId}`,
        price: 0,
        image: '/images/default-product.jpg'
    };
}

function removeFromCart(productId, options = {}) {
    const index = cart.findIndex(item =>
        item.id === productId &&
        JSON.stringify(item.options) === JSON.stringify(options)
    );

    if (index > -1) {
        const removed = cart[index];
        cart.splice(index, 1);
        saveCart();
        updateCartCount();
        updateCartPreview();
        showNotification(`${removed.name} removed from cart`, 'info');

        trackEvent('remove_from_cart', {
            product_id: productId,
            product_name: removed.name
        });
    }
}

function updateCartQuantity(productId, newQuantity, options = {}) {
    if (newQuantity < 1) {
        removeFromCart(productId, options);
        return;
    }

    const item = cart.find(item =>
        item.id === productId &&
        JSON.stringify(item.options) === JSON.stringify(options)
    );

    if (item) {
        const oldQuantity = item.quantity;
        item.quantity = newQuantity;
        saveCart();
        updateCartCount();
        updateCartPreview();

        // Update UI if on cart page
        updateCartPageItemQuantity(productId, newQuantity, options);

        trackEvent('update_cart', {
            product_id: productId,
            old_quantity: oldQuantity,
            new_quantity: newQuantity
        });
    }
}

function updateCartPageItemQuantity(productId, newQuantity, options) {
    const itemElement = document.querySelector(`.cart-item[data-product-id="${productId}"]`);
    if (itemElement) {
        const quantityInput = itemElement.querySelector('.item-quantity');
        const itemTotal = itemElement.querySelector('.item-total');
        const price = parseFloat(itemElement.dataset.productPrice);

        if (quantityInput) {
            quantityInput.value = newQuantity;
        }

        if (itemTotal && price) {
            itemTotal.textContent = `$${(price * newQuantity).toFixed(2)}`;
        }

        updateCartSummary();
    }
}

function updateCartSummary() {
    const subtotalElement = document.getElementById('cartSubtotal');
    const taxElement = document.getElementById('cartTax');
    const totalElement = document.getElementById('cartTotal');
    const deliveryElement = document.getElementById('deliveryFee');

    const subtotal = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    const delivery = subtotal > 50 ? 0 : 5;
    const tax = subtotal * 0.05;
    const total = subtotal + delivery + tax;

    if (subtotalElement) subtotalElement.textContent = `$${subtotal.toFixed(2)}`;
    if (taxElement) taxElement.textContent = `$${tax.toFixed(2)}`;
    if (totalElement) totalElement.textContent = `$${total.toFixed(2)}`;
    if (deliveryElement) {
        deliveryElement.textContent = delivery === 0 ? 'FREE' : `$${delivery.toFixed(2)}`;
    }

    // Update free shipping message
    const freeShippingMsg = document.querySelector('.free-shipping-msg');
    if (freeShippingMsg) {
        if (subtotal >= 50) {
            freeShippingMsg.innerHTML = '<i class="bi bi-check-circle-fill text-success"></i> You qualify for FREE shipping!';
        } else {
            const remaining = (50 - subtotal).toFixed(2);
            freeShippingMsg.innerHTML = `<i class="bi bi-truck"></i> Add $${remaining} more for FREE shipping`;
        }
    }
}

function clearCart() {
    if (cart.length === 0) return;

    if (confirm('Are you sure you want to clear your cart?')) {
        cart = [];
        saveCart();
        updateCartCount();
        updateCartPreview();

        // Reload page if on cart page
        if (window.location.pathname.includes('/cart')) {
            location.reload();
        }

        showNotification('Cart cleared', 'info');
        trackEvent('clear_cart', {});
    }
}

function getCartTotal() {
    return cart.reduce((total, item) => total + (item.price * item.quantity), 0);
}

function getCartCount() {
    return cart.reduce((count, item) => count + item.quantity, 0);
}

function saveCart() {
    localStorage.setItem('cart', JSON.stringify(cart));
}

function updateCartCount() {
    const count = getCartCount();
    const badges = document.querySelectorAll('.cart-count, #cartCount, .cart-badge');

    badges.forEach(badge => {
        badge.textContent = count;
        badge.style.display = count > 0 ? 'inline-flex' : 'none';
    });

    // Update mobile cart count
    const mobileBadge = document.querySelector('.mobile-cart-count');
    if (mobileBadge) {
        mobileBadge.textContent = count;
    }
}

function updateCartPreview() {
    const previewContainer = document.getElementById('cartPreview');
    if (!previewContainer) return;

    if (cart.length === 0) {
        previewContainer.innerHTML = `
            <div class="text-center py-4">
                <i class="bi bi-cart-x display-4 text-muted"></i>
                <p class="text-muted mt-2">Your cart is empty</p>
                <a href="/products" class="btn btn-sm btn-success rounded-pill">Start Shopping</a>
            </div>
        `;
        return;
    }

    let html = '<div class="cart-items-list" style="max-height: 300px; overflow-y: auto;">';
    cart.slice(0, 3).forEach(item => {
        html += `
            <div class="cart-preview-item d-flex align-items-center p-2 border-bottom">
                <img src="${item.image}" 
                     alt="${item.name}" 
                     style="width: 50px; height: 50px; object-fit: cover; border-radius: 8px;"
                     onerror="this.src='/images/default-product.jpg'">
                <div class="ms-3 flex-grow-1">
                    <h6 class="mb-0 fw-bold small">${item.name}</h6>
                    <small class="text-muted">Qty: ${item.quantity} x $${item.price.toFixed(2)}</small>
                </div>
                <span class="fw-bold text-success">$${(item.price * item.quantity).toFixed(2)}</span>
            </div>
        `;
    });

    if (cart.length > 3) {
        html += `<div class="text-center mt-2"><small class="text-muted">And ${cart.length - 3} more items...</small></div>`;
    }

    const subtotal = getCartTotal();
    html += `
        <div class="d-flex justify-content-between mt-3 p-2 bg-light rounded">
            <span class="fw-bold">Subtotal:</span>
            <span class="fw-bold text-success">$${subtotal.toFixed(2)}</span>
        </div>
        <div class="d-grid gap-2 mt-3">
            <a href="/cart" class="btn btn-outline-success btn-sm rounded-pill">View Cart</a>
            <a href="/checkout" class="btn btn-success btn-sm rounded-pill">Checkout</a>
        </div>
    `;

    previewContainer.innerHTML = html;
}

function animateCartIcon() {
    const cartIcon = document.querySelector('.cart-icon, .bi-cart3, [data-cart]');
    if (cartIcon) {
        cartIcon.classList.add('animate__animated', 'animate__rubberBand');
        setTimeout(() => {
            cartIcon.classList.remove('animate__animated', 'animate__rubberBand');
        }, 1000);
    }
}

// ==================== WISHLIST FUNCTIONALITY ====================
function initializeWishlist() {
    updateWishlistCount();

    // Add wishlist buttons to product cards
    document.querySelectorAll('.wishlist-btn').forEach(btn => {
        const productId = btn.dataset.productId;
        if (isInWishlist(productId)) {
            btn.classList.add('active');
            btn.innerHTML = '<i class="bi bi-heart-fill"></i>';
        }

        btn.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            const productId = this.dataset.productId;
            toggleWishlist(productId);
        });
    });
}

function toggleWishlist(productId) {
    const product = findProductById(productId);
    if (!product) return;

    const index = wishlist.findIndex(item => item.id == productId);

    if (index === -1) {
        wishlist.push({
            id: productId,
            name: product.name,
            price: product.price,
            image: product.image,
            addedAt: new Date().toISOString()
        });
        showNotification(`${product.name} added to wishlist`, 'success');
        updateWishlistButton(productId, true);
    } else {
        wishlist.splice(index, 1);
        showNotification(`${product.name} removed from wishlist`, 'info');
        updateWishlistButton(productId, false);
    }

    saveWishlist();
    updateWishlistCount();

    trackEvent('toggle_wishlist', {
        product_id: productId,
        action: index === -1 ? 'add' : 'remove'
    });
}

function isInWishlist(productId) {
    return wishlist.some(item => item.id == productId);
}

function updateWishlistButton(productId, isInList) {
    const btn = document.querySelector(`.wishlist-btn[data-product-id="${productId}"]`);
    if (btn) {
        if (isInList) {
            btn.classList.add('active');
            btn.innerHTML = '<i class="bi bi-heart-fill"></i>';
        } else {
            btn.classList.remove('active');
            btn.innerHTML = '<i class="bi bi-heart"></i>';
        }
    }
}

function saveWishlist() {
    localStorage.setItem('wishlist', JSON.stringify(wishlist));
}

function updateWishlistCount() {
    const count = wishlist.length;
    const badges = document.querySelectorAll('.wishlist-count');
    badges.forEach(badge => {
        badge.textContent = count;
        badge.style.display = count > 0 ? 'inline-flex' : 'none';
    });
}

// ==================== PRODUCT CARDS ====================
function initializeProductCards() {
    // Quick view buttons
    document.querySelectorAll('.quick-view-btn').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            const productId = this.dataset.productId;
            quickView(productId);
        });
    });

    // Add to cart buttons
    document.querySelectorAll('.add-to-cart-btn').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            const productId = this.dataset.productId;
            const quantity = parseInt(this.dataset.quantity || 1);
            addToCart(parseInt(productId), quantity);
        });
    });

    // Quantity selectors
    document.querySelectorAll('.quantity-selector').forEach(selector => {
        const decreaseBtn = selector.querySelector('.decrease-qty');
        const increaseBtn = selector.querySelector('.increase-qty');
        const input = selector.querySelector('.qty-input');

        if (decreaseBtn && input) {
            decreaseBtn.addEventListener('click', function() {
                let value = parseInt(input.value) - 1;
                if (value < 1) value = 1;
                input.value = value;

                // Trigger change event for price updates
                input.dispatchEvent(new Event('change'));
            });
        }

        if (increaseBtn && input) {
            increaseBtn.addEventListener('click', function() {
                let value = parseInt(input.value) + 1;
                const max = parseInt(input.max) || 99;
                if (value > max) value = max;
                input.value = value;

                // Trigger change event for price updates
                input.dispatchEvent(new Event('change'));
            });
        }

        if (input) {
            input.addEventListener('change', function() {
                let value = parseInt(this.value);
                const min = parseInt(this.min) || 1;
                const max = parseInt(this.max) || 99;

                if (isNaN(value) || value < min) value = min;
                if (value > max) value = max;

                this.value = value;

                // Update price if needed
                updateProductPrice(this);
            });
        }
    });
}

function updateProductPrice(input) {
    const productCard = input.closest('.product-card, .product-detail');
    if (productCard) {
        const basePrice = parseFloat(productCard.dataset.productPrice);
        const quantity = parseInt(input.value);
        const priceElement = productCard.querySelector('.product-price, .current-price');

        if (priceElement && basePrice) {
            priceElement.textContent = `$${(basePrice * quantity).toFixed(2)}`;
        }
    }
}

function quickView(productId) {
    const product = findProductById(productId);
    if (!product) {
        showNotification('Product not found', 'error');
        return;
    }

    // Create modal if it doesn't exist
    let modal = document.getElementById('quickViewModal');
    if (!modal) {
        modal = document.createElement('div');
        modal.id = 'quickViewModal';
        modal.className = 'modal fade';
        modal.setAttribute('tabindex', '-1');
        document.body.appendChild(modal);
    }

    modal.innerHTML = `
        <div class="modal-dialog modal-lg modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header bg-success text-white">
                    <h5 class="modal-title">${product.name}</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <div class="row">
                        <div class="col-md-6">
                            <img src="${product.image}" class="img-fluid rounded-4" alt="${product.name}" onerror="this.src='/images/default-product.jpg'">
                        </div>
                        <div class="col-md-6">
                            <h4 class="fw-bold">${product.name}</h4>
                            <div class="product-rating mb-3">
                                ${generateRatingStars(product.rating || 4.5)}
                                <span class="text-muted">(${product.reviews || 0} reviews)</span>
                            </div>
                            <p class="text-muted">${product.description || 'Premium quality farm fresh product.'}</p>
                            <div class="mb-3">
                                <span class="h4 text-success fw-bold">$${product.price.toFixed(2)}</span>
                                ${product.oldPrice ? `<span class="text-muted text-decoration-line-through ms-2">$${product.oldPrice.toFixed(2)}</span>` : ''}
                            </div>
                            <div class="mb-3">
                                <label class="form-label fw-semibold">Quantity:</label>
                                <div class="quantity-selector d-inline-flex ms-3">
                                    <button class="btn btn-sm btn-outline-success decrease-qty" type="button">-</button>
                                    <input type="number" class="form-control form-control-sm qty-input text-center mx-1" value="1" min="1" max="99" style="width: 60px;">
                                    <button class="btn btn-sm btn-outline-success increase-qty" type="button">+</button>
                                </div>
                            </div>
                            <div class="d-grid gap-2">
                                <button class="btn btn-success" onclick="addToCart(${productId}, document.querySelector('#quickViewModal .qty-input').value)">
                                    <i class="bi bi-cart-plus me-2"></i>Add to Cart
                                </button>
                                <button class="btn btn-outline-success" onclick="window.location.href='/product/' + ${productId}">
                                    <i class="bi bi-eye me-2"></i>View Details
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;

    // Initialize Bootstrap modal
    const bsModal = new bootstrap.Modal(modal);
    bsModal.show();

    // Initialize quantity selector in modal
    setTimeout(() => {
        initializeQuantitySelector(modal);
    }, 100);
}

function generateRatingStars(rating) {
    const fullStars = Math.floor(rating);
    const halfStar = rating % 1 >= 0.5;
    let stars = '';

    for (let i = 0; i < fullStars; i++) {
        stars += '<i class="bi bi-star-fill text-warning"></i>';
    }
    if (halfStar) {
        stars += '<i class="bi bi-star-half text-warning"></i>';
    }
    const emptyStars = 5 - Math.ceil(rating);
    for (let i = 0; i < emptyStars; i++) {
        stars += '<i class="bi bi-star text-warning"></i>';
    }

    return stars;
}

function initializeQuantitySelector(container) {
    const decreaseBtn = container.querySelector('.decrease-qty');
    const increaseBtn = container.querySelector('.increase-qty');
    const input = container.querySelector('.qty-input');

    if (decreaseBtn && input) {
        decreaseBtn.addEventListener('click', () => {
            let value = parseInt(input.value) - 1;
            if (value < 1) value = 1;
            input.value = value;
        });
    }

    if (increaseBtn && input) {
        increaseBtn.addEventListener('click', () => {
            let value = parseInt(input.value) + 1;
            const max = parseInt(input.max) || 99;
            if (value > max) value = max;
            input.value = value;
        });
    }

    if (input) {
        input.addEventListener('change', function() {
            let value = parseInt(this.value);
            const min = parseInt(this.min) || 1;
            const max = parseInt(this.max) || 99;

            if (isNaN(value) || value < min) value = min;
            if (value > max) value = max;

            this.value = value;
        });
    }
}

// ==================== SEARCH FUNCTIONALITY ====================
function initializeSearch() {
    const searchInput = document.getElementById('searchInput');
    const searchBtn = document.getElementById('searchBtn');
    const searchClear = document.getElementById('searchClear');

    if (searchInput) {
        searchInput.addEventListener('input', debounce(function(e) {
            const query = e.target.value.trim();
            if (query.length >= 3) {
                performSearch(query);
            } else if (query.length === 0) {
                resetSearch();
            }

            // Show/hide clear button
            if (searchClear) {
                searchClear.style.display = query.length > 0 ? 'block' : 'none';
            }
        }, 500));

        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                const query = this.value.trim();
                if (query) {
                    performSearch(query);
                }
            }
        });
    }

    if (searchBtn) {
        searchBtn.addEventListener('click', function() {
            const query = searchInput ? searchInput.value.trim() : '';
            if (query) {
                performSearch(query);
            }
        });
    }

    if (searchClear) {
        searchClear.addEventListener('click', function() {
            if (searchInput) {
                searchInput.value = '';
                searchInput.focus();
                this.style.display = 'none';
                resetSearch();
            }
        });
    }
}

function performSearch(query) {
    showNotification(`Searching for "${query}"...`, 'info');

    // If on products page, filter products
    const productCards = document.querySelectorAll('.product-card');
    if (productCards.length > 0) {
        let visibleCount = 0;
        productCards.forEach(card => {
            const title = card.querySelector('.product-title, h5, .card-title')?.textContent.toLowerCase() || '';
            const desc = card.querySelector('.product-description, p')?.textContent.toLowerCase() || '';

            if (title.includes(query.toLowerCase()) || desc.includes(query.toLowerCase())) {
                card.style.display = 'block';
                visibleCount++;
            } else {
                card.style.display = 'none';
            }
        });

        // Show no results message
        const noResults = document.getElementById('noResults');
        if (noResults) {
            noResults.style.display = visibleCount === 0 ? 'block' : 'none';
        }

        showNotification(`Found ${visibleCount} products`, 'success');
    } else {
        // Redirect to search page
        window.location.href = `/products?search=${encodeURIComponent(query)}`;
    }

    trackEvent('search', { query: query });
}

function resetSearch() {
    const productCards = document.querySelectorAll('.product-card');
    productCards.forEach(card => {
        card.style.display = 'block';
    });

    const noResults = document.getElementById('noResults');
    if (noResults) {
        noResults.style.display = 'none';
    }
}

// ==================== FORM HANDLING ====================
function initializeForms() {
    // Login form
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }

    // Registration form
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', handleRegistration);
    }

    // Contact form
    const contactForm = document.getElementById('contactForm');
    if (contactForm) {
        contactForm.addEventListener('submit', handleContactForm);
    }

    // Checkout form
    const checkoutForm = document.getElementById('checkoutForm');
    if (checkoutForm) {
        checkoutForm.addEventListener('submit', handleCheckout);
    }

    // Newsletter forms
    document.querySelectorAll('.newsletter-form').forEach(form => {
        form.addEventListener('submit', handleNewsletter);
    });

    // Password strength indicator
    const passwordInput = document.getElementById('password');
    if (passwordInput) {
        passwordInput.addEventListener('input', checkPasswordStrength);
    }

    // Password confirmation
    const confirmPassword = document.getElementById('confirmPassword');
    if (confirmPassword) {
        confirmPassword.addEventListener('input', checkPasswordMatch);
    }
}

function handleLogin(e) {
    e.preventDefault();

    const email = document.getElementById('email')?.value;
    const password = document.getElementById('password')?.value;
    const remember = document.getElementById('remember')?.checked;

    if (!email || !password) {
        showNotification('Please fill in all fields', 'error');
        return;
    }

    // Show loading
    const submitBtn = e.target.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;
    submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Logging in...';
    submitBtn.disabled = true;

    // Simulate login (replace with actual API call)
    setTimeout(() => {
        // Demo credentials
        if (email === 'admin@freshfarm.com' && password === 'admin123') {
            currentUser = {
                email: email,
                name: 'Admin User',
                role: 'admin',
                loggedInAt: new Date().toISOString()
            };
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            if (remember) {
                localStorage.setItem('rememberedEmail', email);
            }
            showNotification('Login successful!', 'success');
            window.location.href = '/admin/dashboard';
        } else if (email === 'user@example.com' && password === 'user123') {
            currentUser = {
                email: email,
                name: 'John Doe',
                role: 'user',
                loggedInAt: new Date().toISOString()
            };
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            if (remember) {
                localStorage.setItem('rememberedEmail', email);
            }
            showNotification('Login successful!', 'success');
            window.location.href = '/';
        } else {
            submitBtn.innerHTML = originalText;
            submitBtn.disabled = false;
            showNotification('Invalid email or password', 'error');
        }

        updateUIForUser();
    }, 1500);
}

function handleRegistration(e) {
    e.preventDefault();

    const formData = new FormData(e.target);
    const data = Object.fromEntries(formData.entries());

    // Validate passwords match
    if (data.password !== data.confirmPassword) {
        showNotification('Passwords do not match', 'error');
        return;
    }

    // Validate terms
    if (!data.agreeTerms) {
        showNotification('You must agree to the terms and conditions', 'error');
        return;
    }

    // Show loading
    const submitBtn = e.target.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;
    submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Creating account...';
    submitBtn.disabled = true;

    // Simulate registration
    setTimeout(() => {
        showNotification('Registration successful! Please check your email to verify your account.', 'success');
        submitBtn.innerHTML = originalText;
        submitBtn.disabled = false;

        // Redirect to login after 2 seconds
        setTimeout(() => {
            window.location.href = '/login';
        }, 2000);
    }, 2000);
}

function handleContactForm(e) {
    e.preventDefault();

    const formData = new FormData(e.target);
    const data = Object.fromEntries(formData.entries());

    // Validate
    if (!data.name || !data.email || !data.message) {
        showNotification('Please fill in all required fields', 'error');
        return;
    }

    // Show loading
    const submitBtn = e.target.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;
    submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Sending...';
    submitBtn.disabled = true;

    // Simulate sending
    setTimeout(() => {
        showNotification('Thank you for your message! We\'ll get back to you soon.', 'success');
        e.target.reset();
        submitBtn.innerHTML = originalText;
        submitBtn.disabled = false;

        trackEvent('contact_form_submit', data);
    }, 1500);
}

function handleNewsletter(e) {
    e.preventDefault();

    const emailInput = e.target.querySelector('input[type="email"]');
    const email = emailInput?.value;

    if (!email || !isValidEmail(email)) {
        showNotification('Please enter a valid email address', 'error');
        return;
    }

    // Show loading
    const submitBtn = e.target.querySelector('button[type="submit"]');
    const originalText = submitBtn?.innerHTML;
    if (submitBtn) {
        submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>';
        submitBtn.disabled = true;
    }

    // Simulate subscription
    setTimeout(() => {
        showNotification('Thank you for subscribing to our newsletter!', 'success');
        emailInput.value = '';
        if (submitBtn) {
            submitBtn.innerHTML = originalText;
            submitBtn.disabled = false;
        }

        trackEvent('newsletter_signup', { email: email });
    }, 1000);
}

function checkPasswordStrength() {
    const password = document.getElementById('password')?.value;
    const strengthBar = document.getElementById('passwordStrength');
    const strengthText = document.getElementById('passwordStrengthText');

    if (!password || !strengthBar) return;

    let strength = 0;

    if (password.length >= 8) strength += 25;
    if (password.match(/[a-z]+/)) strength += 25;
    if (password.match(/[A-Z]+/)) strength += 25;
    if (password.match(/[0-9]+/)) strength += 25;
    if (password.match(/[$@#&!]+/)) strength += 25; // Extra for special chars

    strengthBar.style.width = Math.min(strength, 100) + '%';

    if (strength < 50) {
        strengthBar.className = 'progress-bar bg-danger';
        if (strengthText) strengthText.textContent = 'Weak';
    } else if (strength < 75) {
        strengthBar.className = 'progress-bar bg-warning';
        if (strengthText) strengthText.textContent = 'Medium';
    } else {
        strengthBar.className = 'progress-bar bg-success';
        if (strengthText) strengthText.textContent = 'Strong';
    }
}

function checkPasswordMatch() {
    const password = document.getElementById('password')?.value;
    const confirm = document.getElementById('confirmPassword')?.value;
    const matchError = document.getElementById('passwordMatchError');

    if (!confirm || !matchError) return;

    if (password !== confirm) {
        matchError.style.display = 'block';
        document.getElementById('confirmPassword')?.classList.add('is-invalid');
    } else {
        matchError.style.display = 'none';
        document.getElementById('confirmPassword')?.classList.remove('is-invalid');
    }
}

// ==================== CHECKOUT PROCESS ====================
function initializeCheckout() {
    // Delivery option selection
    document.querySelectorAll('input[name="deliveryOption"]').forEach(radio => {
        radio.addEventListener('change', function() {
            updateDeliveryFee(this.value);
        });
    });

    // Payment method selection
    document.querySelectorAll('input[name="paymentMethod"]').forEach(radio => {
        radio.addEventListener('change', function() {
            showPaymentMethodDetails(this.value);
        });
    });

    // Promo code application
    const applyPromoBtn = document.getElementById('applyPromo');
    if (applyPromoBtn) {
        applyPromoBtn.addEventListener('click', applyPromoCode);
    }

    // Initialize summary
    updateCartSummary();
}

function updateDeliveryFee(option) {
    const deliveryFeeElement = document.getElementById('deliveryFee');
    const subtotal = getCartTotal();

    let deliveryFee = 0;
    if (option === 'express') {
        deliveryFee = 15.00;
    } else {
        deliveryFee = subtotal >= 50 ? 0 : 5.00;
    }

    if (deliveryFeeElement) {
        deliveryFeeElement.textContent = deliveryFee === 0 ? 'FREE' : `$${deliveryFee.toFixed(2)}`;
    }

    updateCartSummary();
}

function showPaymentMethodDetails(method) {
    // Hide all payment details
    document.querySelectorAll('.payment-details').forEach(el => {
        el.style.display = 'none';
    });

    // Show selected payment details
    const detailsElement = document.getElementById(`${method}Details`);
    if (detailsElement) {
        detailsElement.style.display = 'block';
    }
}

function applyPromoCode() {
    const promoInput = document.getElementById('promoCode');
    const code = promoInput?.value.trim().toUpperCase();

    if (!code) {
        showNotification('Please enter a promo code', 'error');
        return;
    }

    // Demo promo codes
    const promos = {
        'FRESH10': 10,
        'WELCOME20': 20,
        'FARM15': 15,
        'FREE50': 50
    };

    if (promos[code]) {
        const discount = promos[code];
        const subtotal = getCartTotal();
        const discountAmount = subtotal * (discount / 100);

        const discountElement = document.getElementById('discountAmount');
        if (discountElement) {
            discountElement.textContent = `-$${discountAmount.toFixed(2)}`;
            discountElement.parentElement.style.display = 'flex';
        }

        showNotification(`Promo code applied! ${discount}% discount`, 'success');
        updateCartSummary();

        trackEvent('apply_promo', { code: code, discount: discount });
    } else {
        showNotification('Invalid promo code', 'error');
    }
}

function handleCheckout(e) {
    e.preventDefault();

    // Validate form
    if (!validateCheckoutForm()) {
        return;
    }

    // Show loading
    const submitBtn = e.target.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;
    submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Processing...';
    submitBtn.disabled = true;

    // Collect order data
    const formData = new FormData(e.target);
    const orderData = Object.fromEntries(formData.entries());
    orderData.items = cart;
    orderData.total = getCartTotal();
    orderData.orderNumber = generateOrderNumber();

    // Simulate order processing
    setTimeout(() => {
        // Clear cart
        cart = [];
        saveCart();

        // Save order to recent orders
        recentOrders.push({
            ...orderData,
            date: new Date().toISOString()
        });
        localStorage.setItem('recentOrders', JSON.stringify(recentOrders));

        showNotification('Order placed successfully!', 'success');

        // Redirect to success page
        window.location.href = `/order/success?order=${orderData.orderNumber}`;

        trackEvent('place_order', {
            order_number: orderData.orderNumber,
            total: orderData.total,
            items: cart.length
        });
    }, 2000);
}

function validateCheckoutForm() {
    const required = ['customer.name', 'customer.email', 'customer.phone', 'address'];
    let isValid = true;

    required.forEach(field => {
        const input = document.querySelector(`[name="${field}"]`);
        if (input && !input.value.trim()) {
            input.classList.add('is-invalid');
            isValid = false;
        } else if (input) {
            input.classList.remove('is-invalid');
        }
    });

    if (!isValid) {
        showNotification('Please fill in all required fields', 'error');
    }

    return isValid;
}

function generateOrderNumber() {
    const prefix = 'FFJ';
    const timestamp = Date.now().toString().slice(-8);
    const random = Math.floor(Math.random() * 1000).toString().padStart(3, '0');
    return `${prefix}-${timestamp}-${random}`;
}

// ==================== ADMIN PANEL ====================
function initializeAdminPanel() {
    if (!window.location.pathname.includes('/admin')) return;

    // Initialize charts if on dashboard
    if (document.getElementById('revenueChart')) {
        loadCharts();
    }

    // Product management
    initializeProductManagement();

    // Order management
    initializeOrderManagement();

    // User management
    initializeUserManagement();

    // Bulk actions
    initializeBulkActions();
}

function loadCharts() {
    // This would use Chart.js - make sure it's included
    if (typeof Chart === 'undefined') {
        console.warn('Chart.js not loaded');
        return;
    }

    // Revenue chart
    const revenueCtx = document.getElementById('revenueChart')?.getContext('2d');
    if (revenueCtx) {
        new Chart(revenueCtx, {
            type: 'line',
            data: {
                labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
                datasets: [{
                    label: 'Revenue ($)',
                    data: [1200, 1900, 1500, 2100, 2800, 2400, 3200],
                    borderColor: '#2B5E3B',
                    backgroundColor: 'rgba(43, 94, 59, 0.1)',
                    tension: 0.4,
                    fill: true
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false }
                }
            }
        });
    }

    // Category chart
    const categoryCtx = document.getElementById('categoryChart')?.getContext('2d');
    if (categoryCtx) {
        new Chart(categoryCtx, {
            type: 'doughnut',
            data: {
                labels: ['Poultry', 'Vegetables', 'Fruits'],
                datasets: [{
                    data: [45, 30, 25],
                    backgroundColor: ['#2B5E3B', '#FFC107', '#17A2B8'],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false }
                },
                cutout: '70%'
            }
        });
    }
}

function initializeProductManagement() {
    // Delete product buttons
    document.querySelectorAll('.delete-product-btn').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            const productId = this.dataset.productId;
            const productName = this.dataset.productName;

            if (confirm(`Are you sure you want to delete "${productName}"?`)) {
                deleteProduct(productId);
            }
        });
    });

    // Toggle featured
    document.querySelectorAll('.toggle-featured').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            const productId = this.dataset.productId;
            toggleFeatured(productId);
        });
    });
}

function deleteProduct(productId) {
    showNotification('Deleting product...', 'info');

    // Simulate API call
    setTimeout(() => {
        // Remove from DOM
        const productRow = document.querySelector(`tr[data-product-id="${productId}"]`);
        if (productRow) {
            productRow.remove();
        }

        showNotification('Product deleted successfully', 'success');
        trackEvent('admin_delete_product', { product_id: productId });
    }, 1000);
}

function toggleFeatured(productId) {
    const btn = document.querySelector(`.toggle-featured[data-product-id="${productId}"]`);
    const isFeatured = btn?.classList.contains('active');

    if (btn) {
        if (isFeatured) {
            btn.classList.remove('active');
            btn.innerHTML = '<i class="bi bi-star"></i> Set Featured';
        } else {
            btn.classList.add('active');
            btn.innerHTML = '<i class="bi bi-star-fill"></i> Featured';
        }
    }

    showNotification(`Product ${isFeatured ? 'removed from' : 'added to'} featured`, 'success');
}

function initializeOrderManagement() {
    // Update order status
    document.querySelectorAll('.order-status-select').forEach(select => {
        select.addEventListener('change', function() {
            const orderId = this.dataset.orderId;
            const newStatus = this.value;
            updateOrderStatus(orderId, newStatus);
        });
    });

    // View order details
    document.querySelectorAll('.view-order-btn').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            const orderId = this.dataset.orderId;
            viewOrderDetails(orderId);
        });
    });
}

function updateOrderStatus(orderId, newStatus) {
    showNotification(`Updating order #${orderId} status to ${newStatus}...`, 'info');

    // Simulate API call
    setTimeout(() => {
        showNotification(`Order #${orderId} status updated to ${newStatus}`, 'success');

        // Update badge
        const badge = document.querySelector(`.order-status-badge[data-order-id="${orderId}"]`);
        if (badge) {
            badge.className = `badge bg-${getStatusColor(newStatus)}`;
            badge.textContent = newStatus;
        }

        trackEvent('admin_update_order', { order_id: orderId, status: newStatus });
    }, 1000);
}

function getStatusColor(status) {
    const colors = {
        'PENDING': 'warning',
        'PROCESSING': 'info',
        'SHIPPED': 'primary',
        'DELIVERED': 'success',
        'CANCELLED': 'danger'
    };
    return colors[status] || 'secondary';
}

function viewOrderDetails(orderId) {
    // Implement order details modal
    showNotification(`Loading order #${orderId} details...`, 'info');
}

function initializeUserManagement() {
    // Toggle user status
    document.querySelectorAll('.toggle-user-status').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            const userId = this.dataset.userId;
            const username = this.dataset.username;

            if (confirm(`Toggle active status for user "${username}"?`)) {
                toggleUserStatus(userId);
            }
        });
    });
}

function toggleUserStatus(userId) {
    showNotification('Updating user status...', 'info');

    // Simulate API call
    setTimeout(() => {
        const badge = document.querySelector(`.user-status-badge[data-user-id="${userId}"]`);
        if (badge) {
            if (badge.textContent === 'Active') {
                badge.className = 'badge bg-danger user-status-badge';
                badge.textContent = 'Inactive';
            } else {
                badge.className = 'badge bg-success user-status-badge';
                badge.textContent = 'Active';
            }
        }

        showNotification('User status updated successfully', 'success');
        trackEvent('admin_toggle_user', { user_id: userId });
    }, 1000);
}

function initializeBulkActions() {
    // Select all checkbox
    const selectAll = document.getElementById('selectAll');
    if (selectAll) {
        selectAll.addEventListener('change', function() {
            document.querySelectorAll('.item-checkbox').forEach(cb => {
                cb.checked = this.checked;
            });
        });
    }

    // Bulk delete button
    const bulkDelete = document.getElementById('bulkDelete');
    if (bulkDelete) {
        bulkDelete.addEventListener('click', function() {
            const selected = Array.from(document.querySelectorAll('.item-checkbox:checked'))
                .map(cb => cb.value);

            if (selected.length === 0) {
                showNotification('No items selected', 'error');
                return;
            }

            if (confirm(`Delete ${selected.length} selected items?`)) {
                performBulkDelete(selected);
            }
        });
    }

    // Bulk status update
    const bulkUpdate = document.getElementById('bulkUpdate');
    if (bulkUpdate) {
        bulkUpdate.addEventListener('click', function() {
            const selected = Array.from(document.querySelectorAll('.item-checkbox:checked'))
                .map(cb => cb.value);

            if (selected.length === 0) {
                showNotification('No items selected', 'error');
                return;
            }

            const status = document.getElementById('bulkStatus')?.value;
            if (status) {
                performBulkUpdate(selected, status);
            }
        });
    }
}

function performBulkDelete(ids) {
    showNotification(`Deleting ${ids.length} items...`, 'info');

    setTimeout(() => {
        ids.forEach(id => {
            const row = document.querySelector(`tr[data-id="${id}"]`);
            if (row) row.remove();
        });

        showNotification(`${ids.length} items deleted successfully`, 'success');
        trackEvent('admin_bulk_delete', { count: ids.length });
    }, 1500);
}

function performBulkUpdate(ids, status) {
    showNotification(`Updating ${ids.length} items to ${status}...`, 'info');

    setTimeout(() => {
        ids.forEach(id => {
            const statusElement = document.querySelector(`.item-status[data-id="${id}"]`);
            if (statusElement) {
                statusElement.className = `badge bg-${getStatusColor(status)} item-status`;
                statusElement.textContent = status;
            }
        });

        showNotification(`${ids.length} items updated to ${status}`, 'success');
        trackEvent('admin_bulk_update', { count: ids.length, status: status });
    }, 1500);
}

// ==================== USER INTERFACE ====================
function initializeUserMenu() {
    const userMenuBtn = document.getElementById('userMenuBtn');
    const userMenu = document.getElementById('userMenu');

    if (userMenuBtn && userMenu) {
        userMenuBtn.addEventListener('click', function(e) {
            e.stopPropagation();
            userMenu.classList.toggle('show');
        });

        document.addEventListener('click', function(e) {
            if (!userMenu.contains(e.target) && !userMenuBtn.contains(e.target)) {
                userMenu.classList.remove('show');
            }
        });
    }
}

function updateUIForUser() {
    const loginLinks = document.querySelectorAll('.login-link, .signin-link');
    const userMenus = document.querySelectorAll('.user-menu');
    const userNames = document.querySelectorAll('.user-name');
    const adminLinks = document.querySelectorAll('.admin-link');

    if (currentUser) {
        loginLinks.forEach(link => link.style.display = 'none');
        userMenus.forEach(menu => menu.style.display = 'block');
        userNames.forEach(name => name.textContent = currentUser.name || 'User');

        if (currentUser.role === 'admin') {
            adminLinks.forEach(link => link.style.display = 'block');
        }
    } else {
        loginLinks.forEach(link => link.style.display = 'block');
        userMenus.forEach(menu => menu.style.display = 'none');
        adminLinks.forEach(link => link.style.display = 'none');
    }
}

function logout() {
    if (confirm('Are you sure you want to logout?')) {
        currentUser = null;
        localStorage.removeItem('currentUser');
        updateUIForUser();
        showNotification('Logged out successfully', 'info');
        window.location.href = '/';
    }
}

// ==================== NOTIFICATIONS ====================
function showNotification(message, type = 'success', duration = 3000) {
    // Check if toast container exists
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container position-fixed bottom-0 end-0 p-3';
        container.style.zIndex = '9999';
        document.body.appendChild(container);
    }

    // Create toast
    const toastId = 'toast-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);
    const toast = document.createElement('div');
    toast.className = `toast align-items-center text-white bg-${type} border-0 show`;
    toast.id = toastId;
    toast.setAttribute('role', 'alert');
    toast.setAttribute('aria-live', 'assertive');
    toast.setAttribute('aria-atomic', 'true');

    const icon = {
        success: 'check-circle',
        error: 'exclamation-triangle',
        info: 'info-circle',
        warning: 'exclamation-circle'
    }[type] || 'info-circle';

    toast.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">
                <i class="bi bi-${icon} me-2"></i>
                ${message}
            </div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
    `;

    container.appendChild(toast);

    // Auto remove after duration
    setTimeout(() => {
        toast.remove();
        if (container.children.length === 0) {
            container.remove();
        }
    }, duration);
}

// ==================== ANIMATIONS ====================
function initializeAnimations() {
    // Add animation classes to elements
    document.querySelectorAll('.fade-in-on-scroll').forEach(el => {
        observer.observe(el);
    });

    // Counter animations
    document.querySelectorAll('.counter').forEach(counter => {
        animateCounter(counter);
    });
}

const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.classList.add('animate__animated', 'animate__fadeInUp');

            // Animate counters inside
            entry.target.querySelectorAll('.counter').forEach(counter => {
                animateCounter(counter);
            });
        }
    });
}, { threshold: 0.1 });

function animateCounter(element) {
    const target = parseInt(element.dataset.target) || 0;
    const duration = 2000;
    const stepTime = 20;
    const steps = duration / stepTime;
    const increment = target / steps;
    let current = 0;

    const timer = setInterval(() => {
        current += increment;
        if (current >= target) {
            element.textContent = target;
            clearInterval(timer);
        } else {
            element.textContent = Math.floor(current);
        }
    }, stepTime);
}

// ==================== TOOLTIPS ====================
function initializeTooltips() {
    // Initialize Bootstrap tooltips
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(tooltipTriggerEl => {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Initialize Bootstrap popovers
    const popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    popoverTriggerList.map(popoverTriggerEl => {
        return new bootstrap.Popover(popoverTriggerEl);
    });
}

// ==================== MODALS ====================
function initializeModals() {
    // Generic modal trigger
    document.querySelectorAll('[data-modal]').forEach(trigger => {
        trigger.addEventListener('click', function(e) {
            e.preventDefault();
            const modalId = this.dataset.modal;
            const modal = new bootstrap.Modal(document.getElementById(modalId));
            modal.show();
        });
    });
}

// ==================== UTILITY FUNCTIONS ====================
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

function isValidEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
}

function loadUserPreferences() {
    // Load theme preference
    const darkMode = localStorage.getItem('darkMode') === 'true';
    if (darkMode) {
        document.body.classList.add('dark-mode');
    }

    // Load saved email if exists
    const rememberedEmail = localStorage.getItem('rememberedEmail');
    const emailInput = document.querySelector('input[name="email"]');
    if (rememberedEmail && emailInput) {
        emailInput.value = rememberedEmail;
        const rememberCheck = document.getElementById('remember');
        if (rememberCheck) rememberCheck.checked = true;
    }
}

function trackEvent(eventName, eventData) {
    // Log to console in development
    if (window.location.hostname === 'localhost') {
        console.log('Track event:', eventName, eventData);
    }

    // Store in localStorage for analytics
    const events = JSON.parse(localStorage.getItem('userEvents')) || [];
    events.push({
        name: eventName,
        data: eventData,
        timestamp: new Date().toISOString()
    });
    localStorage.setItem('userEvents', JSON.stringify(events.slice(-50)));
}

// ==================== EXPOSE GLOBALLY ====================
window.addToCart = addToCart;
window.removeFromCart = removeFromCart;
window.updateCartQuantity = updateCartQuantity;
window.clearCart = clearCart;
window.toggleWishlist = toggleWishlist;
window.quickView = quickView;
window.logout = logout;
window.showNotification = showNotification;
window.applyPromoCode = applyPromoCode;

