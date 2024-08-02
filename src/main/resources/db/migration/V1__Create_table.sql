-- Enable the required extension for UUID if not already enabled
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Create the 'accounts' schema
CREATE SCHEMA IF NOT EXISTS accounts;

-- Create the holder table
CREATE TABLE accounts.holder (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provided_id VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address VARCHAR(255) NOT NULL
);

CREATE INDEX idx_holder_id ON accounts.holder (id);

-- Create the account table
CREATE TABLE accounts.account (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    holder_id UUID NOT NULL,
    number VARCHAR(10) UNIQUE NOT NULL,
    balance NUMERIC(10, 2) NOT NULL CHECK (balance >= 0) DEFAULT 0,
    FOREIGN KEY (holder_id) REFERENCES accounts.holder(id)
);

CREATE INDEX idx_account_id ON accounts.account (id);
CREATE INDEX idx_account_number ON accounts.account (number);

-- Create the card table
CREATE TABLE accounts.card (
    number VARCHAR(50) PRIMARY KEY,
    cvv VARCHAR(50) NOT NULL,
    type VARCHAR(10) NOT NULL CHECK (type IN ('CREDIT', 'DEBIT')),
    account_id UUID NOT NULL,
    FOREIGN KEY (account_id) REFERENCES accounts.account(id)
);

CREATE INDEX idx_card_number ON accounts.card (number);

-- Create the transaction table
CREATE TABLE accounts.transaction (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    timestamp TIMESTAMP NOT NULL,
    type VARCHAR(10) NOT NULL CHECK (type IN ('WITHDRAW', 'TRANSFER', 'DEPOSIT')),
    amount NUMERIC(10, 2) NOT NULL,
    credit_card_fee_amount NUMERIC(10, 2),
    credit_card_fee NUMERIC(5, 2),
    total_amount NUMERIC(10, 2) NOT NULL,
    account_id UUID NOT NULL,
    account_balance NUMERIC(10, 2) NOT NULL CHECK (account_balance >= 0) DEFAULT 0,
    card_id VARCHAR(50),
    ref_transaction UUID NOT NULL,
    FOREIGN KEY (account_id) REFERENCES accounts.account(id),
    FOREIGN KEY (card_id) REFERENCES accounts.card(number)
);

CREATE INDEX idx_transaction_id ON accounts.transaction (id);
CREATE INDEX idx_transaction_timestamp ON accounts.transaction (timestamp);


-- Grant usage on the schema to the user
GRANT USAGE ON SCHEMA accounts TO "user";

-- Revoke update and delete privileges on the transactions table
REVOKE ALL ON TABLE accounts.transaction FROM PUBLIC;
GRANT INSERT ON TABLE accounts.transaction TO "user";
