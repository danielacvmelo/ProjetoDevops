CREATE TABLE products (
    id UUID PRIMARY KEY,
    code VARCHAR(80) NOT NULL,
    code_key VARCHAR(80) UNIQUE,
    name VARCHAR(160) NOT NULL,
    category VARCHAR(100) NOT NULL,
    unit VARCHAR(30) NOT NULL,
    purchase_price NUMERIC(14, 2) NOT NULL CHECK (purchase_price >= 0),
    sale_price NUMERIC(14, 2) NOT NULL CHECK (sale_price >= 0),
    stock NUMERIC(15, 3) NOT NULL CHECK (stock >= 0),
    minimum_stock NUMERIC(15, 3) NOT NULL CHECK (minimum_stock >= 0),
    status VARCHAR(10) NOT NULL CHECK (status IN ('Ativo', 'Inativo')),
    search_key VARCHAR(400) NOT NULL
);

CREATE TABLE suppliers (
    id UUID PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    name_key VARCHAR(160) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(40) NOT NULL
);
