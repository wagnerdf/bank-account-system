CREATE TABLE bank_accounts (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(50) NOT NULL UNIQUE,
    balance NUMERIC(19,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    user_id BIGINT NOT NULL REFERENCES user_accounts(id),
    created_at TIMESTAMP DEFAULT now()
);


DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'bank_accounts') THEN
        UPDATE bank_accounts SET status = 'ACTIVE' WHERE status = 'ATIVA';
        UPDATE bank_accounts SET status = 'BLOCKED' WHERE status = 'BLOQUEADA';
    END IF;
END$$;
