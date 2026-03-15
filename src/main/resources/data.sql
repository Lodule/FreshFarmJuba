-- Clean up existing data to ensure a fresh start
DELETE FROM order_items;
DELETE FROM orders;
DELETE FROM products;
DELETE FROM users;

-- Insert Products
INSERT INTO products (name, description, price, category, image_url, stock_quantity, is_organic, fresh_daily, is_featured, unit) VALUES
('Fresh Layers', 'High-quality laying hens for your backyard or farm. Healthy and productive.', 15.00, 'POULTRY', '/images/layers.jpg', 50, true, true, true, 'bird'),
('Broiler Chickens', 'Ready-to-eat fresh broiler chickens, raised organically.', 12.00, 'POULTRY', '/images/broiler.webp', 100, true, true, true, 'bird'),
('Fresh Cabbages', 'Crisp and organic green cabbages harvested daily.', 2.50, 'VEGETABLES', '/images/cabbages.jpeg', 200, true, true, false, 'kg'),
('Sweet Carrots', 'Crunchy, vitamin-rich carrots grown in Juba soil.', 1.80, 'VEGETABLES', '/images/carrots.jpeg', 150, true, true, true, 'kg'),
('Large Pumpkins', 'Nutritious large pumpkins, perfect for soups and stews.', 5.00, 'VEGETABLES', '/images/pumpkin.jpeg', 40, true, false, false, 'pcs'),
('Juicy Watermelons', 'Sweet and refreshing watermelons for the hot Juba sun.', 4.50, 'FRUITS', '/images/watermelon.jpeg', 60, true, true, true, 'pcs'),
('Red Peppers', 'Fresh and vibrant red bell peppers.', 3.00, 'VEGETABLES', '/images/red peppers.jpeg', 80, true, true, false, 'kg'),
('Green Peppers', 'Crunchy green peppers for your favorite dishes.', 2.80, 'VEGETABLES', '/images/green peppers.jpeg', 90, true, true, false, 'kg'),
('Irish Potatoes', 'Premium quality Irish potatoes, great for frying or boiling.', 1.20, 'VEGETABLES', '/images/irish potatoes.jpeg', 300, true, false, false, 'kg'),
('Sweet Potatoes', 'Naturally sweet and healthy orange-fleshed sweet potatoes.', 1.50, 'VEGETABLES', '/images/sweet potatoes.jpeg', 250, true, false, false, 'kg'),
('Fresh Yams', 'Large, starchy yams from our latest harvest.', 3.50, 'VEGETABLES', '/images/yams.jpeg', 70, true, false, false, 'kg');

-- Insert Admin User
-- Password is "admin123" (BCrypt: $2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2)
INSERT INTO users (email, password, first_name, last_name, phone_number, roles, active, email_verified, account_non_expired, account_non_locked, credentials_non_expired, created_at, account_type, failed_attempts)
VALUES ('admin@freshfarmjuba.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'Admin', 'User', '+211981502973', 'ROLE_ADMIN', true, true, true, true, true, NOW(), 'admin', 0);

-- Insert Regular User
-- Password is "user123" (BCrypt: $2a$10$fV3MBSWzNfS2p4vB.Y9.Nu08VpG9YqJ.GzY6J6/J9.V7v9U6vK.iW)
INSERT INTO users (email, password, first_name, last_name, phone_number, roles, active, email_verified, account_non_expired, account_non_locked, credentials_non_expired, created_at, account_type, failed_attempts)
VALUES ('user@example.com', '$2a$10$fV3MBSWzNfS2p4vB.Y9.Nu08VpG9YqJ.GzY6J6/J9.V7v9U6vK.iW', 'John', 'Doe', '+211925674716', 'ROLE_USER', true, true, true, true, true, NOW(), 'customer', 0);
