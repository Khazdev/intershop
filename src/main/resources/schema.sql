CREATE TABLE IF NOT EXISTS items
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    title       VARCHAR(255)   NOT NULL,
    description TEXT,
    img_path    VARCHAR(512),
    price       DECIMAL(19, 2) NOT NULL CHECK (price > 0),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cart
(
    id      BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS cart_item
(
    id       BIGINT PRIMARY KEY AUTO_INCREMENT,
    cart_id  BIGINT NOT NULL,
    item_id  BIGINT NOT NULL,
    quantity INT    NOT NULL DEFAULT 1 CHECK (quantity > 0),
    FOREIGN KEY (cart_id) REFERENCES cart (id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES items (id) ON DELETE CASCADE,
    CONSTRAINT uk_cart_item UNIQUE (cart_id, item_id)
);

CREATE TABLE IF NOT EXISTS purchase_order
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    total_sum  DECIMAL(19, 2) NOT NULL CHECK (total_sum > 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_item
(
    id       BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT         NOT NULL,
    item_id  BIGINT         NOT NULL,
    quantity INT            NOT NULL CHECK (quantity > 0),
    price    DECIMAL(19, 2) NOT NULL CHECK (price > 0),
    FOREIGN KEY (order_id) REFERENCES purchase_order (id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES items (id)
);
