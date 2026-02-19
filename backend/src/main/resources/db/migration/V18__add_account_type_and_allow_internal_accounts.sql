-- 1️⃣ Permitir user_id NULL (para contas internas)
ALTER TABLE bank_accounts
ALTER COLUMN user_id DROP NOT NULL;

-- 2️⃣ Adicionar coluna account_type
ALTER TABLE bank_accounts
ADD COLUMN account_type VARCHAR(30) NOT NULL DEFAULT 'USER';

-- 3️⃣ Garantir valores válidos
ALTER TABLE bank_accounts
ADD CONSTRAINT chk_account_type
CHECK (account_type IN (
    'USER',
    'GAME_POOL',
    'PLATFORM_FEE',
    'TAX',
    'TREASURY'
));
