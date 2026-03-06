-- Create tables
CREATE TABLE locations (
    city_id SERIAL PRIMARY KEY,
    city_name VARCHAR(100) NOT NULL
);

CREATE TABLE sellers (
    seller_id SERIAL PRIMARY KEY,
    seller_name VARCHAR(100) NOT NULL,
    city_id INT REFERENCES locations(city_id)
);

-- Insert mock data
INSERT INTO locations (city_name) VALUES 
('New York'), ('Los Angeles'), ('Chicago'), ('Houston'), ('Phoenix');

INSERT INTO sellers (seller_name, city_id) VALUES 
('John Doe', 1), ('Jane Smith', 2), ('Bob Johnson', 1), 
('Alice Williams', 3), ('Charlie Brown', 4);