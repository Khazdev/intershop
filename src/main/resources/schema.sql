CREATE TABLE IF NOT EXISTS items
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    title       VARCHAR(255)   NOT NULL,
    description TEXT,
    img_path    VARCHAR(512)   NOT NULL,
    price       DECIMAL(10, 2) NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);