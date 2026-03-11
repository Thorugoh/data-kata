-- scripts/init-analytics.sql

CREATE TABLE IF NOT EXISTS top_sales_city (
    city_name VARCHAR(255) PRIMARY KEY,
    total_amount DECIMAL(15, 2),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS top_sales_seller (
    seller_id VARCHAR(50) PRIMARY KEY,
    total_amount DECIMAL(15, 2),
    avg_performance DECIMAL(5, 2),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);