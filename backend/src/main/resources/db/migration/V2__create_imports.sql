CREATE TABLE imports (
    id UUID PRIMARY KEY,
    supplier_id UUID NOT NULL REFERENCES suppliers(id),
    file_name VARCHAR(255) NOT NULL,
    hash VARCHAR(64) NOT NULL,
    file_data BYTEA NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) NOT NULL,
    duplicate_of UUID REFERENCES imports(id),
    item_count INTEGER NOT NULL,
    error VARCHAR(1000)
);
CREATE INDEX imports_supplier_hash ON imports(supplier_id, hash);
CREATE TABLE import_items (
    id UUID PRIMARY KEY,
    import_id UUID NOT NULL REFERENCES imports(id),
    line_number INTEGER NOT NULL,
    raw_line VARCHAR(2000) NOT NULL,
    reference VARCHAR(80) NOT NULL,
    description VARCHAR(300) NOT NULL,
    unit VARCHAR(30) NOT NULL,
    packaging VARCHAR(100) NOT NULL,
    quantity NUMERIC(15, 3),
    price NUMERIC(14, 2),
    product_id UUID REFERENCES products(id),
    confirmed BOOLEAN NOT NULL,
    classification VARCHAR(20) NOT NULL,
    error VARCHAR(1000)
);
CREATE INDEX import_items_batch ON import_items(import_id);
CREATE TABLE supplier_links (
    id UUID PRIMARY KEY,
    supplier_id UUID NOT NULL REFERENCES suppliers(id),
    item_key VARCHAR(64) NOT NULL,
    product_id UUID NOT NULL REFERENCES products(id),
    UNIQUE (supplier_id, item_key)
);
CREATE TABLE price_history (
    id UUID PRIMARY KEY,
    import_id UUID NOT NULL REFERENCES imports(id),
    item_id UUID NOT NULL UNIQUE REFERENCES import_items(id),
    supplier_id UUID NOT NULL REFERENCES suppliers(id),
    product_id UUID NOT NULL REFERENCES products(id),
    item_key VARCHAR(64) NOT NULL,
    price NUMERIC(14, 2) NOT NULL CHECK (price > 0),
    previous_price NUMERIC(14, 2),
    difference NUMERIC(14, 2),
    percentage NUMERIC(24, 2),
    classification VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX price_history_item ON price_history(supplier_id, item_key, created_at);
