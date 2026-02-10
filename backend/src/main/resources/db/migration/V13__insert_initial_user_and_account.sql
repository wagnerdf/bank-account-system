-- ============================
-- USUÁRIO INICIAL
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
    1,
    'Usuário Teste',
    'teste@local.com',
    '123456',
    'USER',
    true,
    NOW()
);

-- ============================
-- CONTA BANCÁRIA INICIAL
-- ============================
INSERT INTO bank_accounts (
    id,
    user_id,
    account_number,
    balance,
    created_at
) VALUES (
    1,
    1,
    'ACC-0001',
    1000.00,
    NOW()
);
