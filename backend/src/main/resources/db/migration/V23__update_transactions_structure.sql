-- 1️⃣ Remover foreign key antiga (se existir)
ALTER TABLE transactions DROP CONSTRAINT IF EXISTS fk_transactions_account;

-- 2️⃣ Remover coluna antiga
ALTER TABLE transactions DROP COLUMN IF EXISTS account_id;

-- 3️⃣ Adicionar novas colunas
ALTER TABLE transactions
ADD COLUMN from_account_id BIGINT,
ADD COLUMN to_account_id BIGINT,
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

-- 4️⃣ Criar foreign keys
ALTER TABLE transactions
ADD CONSTRAINT fk_transactions_from_account
FOREIGN KEY (from_account_id)
REFERENCES bank_accounts(id);

ALTER TABLE transactions
ADD CONSTRAINT fk_transactions_to_account
FOREIGN KEY (to_account_id)
REFERENCES bank_accounts(id);

-- 5️⃣ Criar índices (performance em consulta financeira)
CREATE INDEX idx_transactions_from_account ON transactions(from_account_id);
CREATE INDEX idx_transactions_to_account ON transactions(to_account_id);