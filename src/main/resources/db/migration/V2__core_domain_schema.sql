CREATE TABLE roles (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE customers (
    id UUID PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE customer_roles (
    customer_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (customer_id, role_id),
    CONSTRAINT fk_cr_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    CONSTRAINT fk_cr_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    account_number VARCHAR(255) NOT NULL UNIQUE,
    account_type VARCHAR(50) NOT NULL,
    balance DECIMAL(19, 4) NOT NULL,
    status VARCHAR(50) NOT NULL,
    customer_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_account_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    CONSTRAINT chk_account_balance CHECK (balance >= 0)
);

CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    transaction_reference VARCHAR(255) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    description VARCHAR(500),
    source_account_id UUID NOT NULL,
    destination_account_id UUID,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT fk_txn_source_account FOREIGN KEY (source_account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    CONSTRAINT fk_txn_dest_account FOREIGN KEY (destination_account_id) REFERENCES accounts(id) ON DELETE SET NULL
);
