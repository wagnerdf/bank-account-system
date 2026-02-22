-- 1️⃣ Remover constraint antiga
ALTER TABLE bank_accounts
DROP CONSTRAINT IF EXISTS chk_account_type;

-- 2️⃣ Atualizar dados antigos
UPDATE bank_accounts
SET account_type = 'CHECKING'
WHERE account_type = 'USER';

-- 3️⃣ Criar nova constraint alinhada ao enum
ALTER TABLE bank_accounts
ADD CONSTRAINT chk_account_type
CHECK (account_type IN ('CHECKING', 'SAVINGS', 'PLATFORM_FEE'));