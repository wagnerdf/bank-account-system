-- ============================
-- NOVO USUÁRIO DE TESTE
-- ============================
INSERT INTO user_accounts (
    id,
    full_name,
    email,
    password_hash,
    role,
    active,
    created_at
) VALUES (
    2,
    'wagnerdf',
    'wagnerdf@gmail.com',
    '123456',
    'ADMIN',
    true,
    NOW()
);

-- ============================
-- NOVA CONTA BANCÁRIA (ID 3)
-- ============================
INSERT INTO bank_accounts (
    id,
    user_id,
    account_number,
    balance,
    created_at
) VALUES (
    3,
    2,               -- vinculada ao usuário 2
    'ACC-0003',
    10000.00,          -- saldo inicial
    NOW()
);
