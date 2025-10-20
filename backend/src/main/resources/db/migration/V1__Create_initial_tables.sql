-- 创建用户表
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建品类表
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    parent_id UUID REFERENCES categories(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建物品表
CREATE TABLE items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    category_id UUID REFERENCES categories(id),
    brand VARCHAR(100),
    size VARCHAR(50),
    color VARCHAR(50),
    purchase_price DECIMAL(18,2) NOT NULL,
    purchase_date DATE NOT NULL,
    location VARCHAR(200),
    images JSONB,
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'REMOVED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- 创建库存交易表
CREATE TABLE inventory_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    item_id UUID NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL CHECK (type IN ('IN', 'OUT', 'ADJUST')),
    quantity INTEGER DEFAULT 1,
    unit_price DECIMAL(18,2) NOT NULL,
    total_amount DECIMAL(18,2) NOT NULL,
    transaction_date DATE NOT NULL,
    reason VARCHAR(50) CHECK (reason IN ('PURCHASE', 'SELL', 'DISPOSE', 'GIFT', 'LOST', 'ADJUST')),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建账户表
CREATE TABLE accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('CASH', 'BANK', 'PLATFORM', 'OTHER')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建记账分录表
CREATE TABLE ledger_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    transaction_date DATE NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    direction VARCHAR(10) NOT NULL CHECK (direction IN ('DEBIT', 'CREDIT')),
    account_id UUID NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    item_id UUID REFERENCES items(id) ON DELETE SET NULL,
    category_code VARCHAR(50),
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_items_user_id ON items(user_id);
CREATE INDEX idx_items_category_id ON items(category_id);
CREATE INDEX idx_items_status ON items(status);
CREATE INDEX idx_items_purchase_date ON items(purchase_date);
CREATE INDEX idx_items_deleted_at ON items(deleted_at);

CREATE INDEX idx_transactions_user_id ON inventory_transactions(user_id);
CREATE INDEX idx_transactions_item_id ON inventory_transactions(item_id);
CREATE INDEX idx_transactions_date ON inventory_transactions(transaction_date);
CREATE INDEX idx_transactions_type ON inventory_transactions(type);

CREATE INDEX idx_ledger_user_id ON ledger_entries(user_id);
CREATE INDEX idx_ledger_date ON ledger_entries(transaction_date);
CREATE INDEX idx_ledger_account_id ON ledger_entries(account_id);
CREATE INDEX idx_ledger_item_id ON ledger_entries(item_id);

CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_categories_parent_id ON categories(parent_id);

-- 插入默认品类数据
INSERT INTO categories (name, parent_id) VALUES 
('服装', NULL),
('鞋子', NULL),
('配饰', NULL),
('其他', NULL);

INSERT INTO categories (name, parent_id) VALUES 
('上衣', (SELECT id FROM categories WHERE name = '服装' LIMIT 1)),
('裤子', (SELECT id FROM categories WHERE name = '服装' LIMIT 1)),
('裙子', (SELECT id FROM categories WHERE name = '服装' LIMIT 1)),
('运动鞋', (SELECT id FROM categories WHERE name = '鞋子' LIMIT 1)),
('休闲鞋', (SELECT id FROM categories WHERE name = '鞋子' LIMIT 1)),
('包包', (SELECT id FROM categories WHERE name = '配饰' LIMIT 1)),
('手表', (SELECT id FROM categories WHERE name = '配饰' LIMIT 1));
