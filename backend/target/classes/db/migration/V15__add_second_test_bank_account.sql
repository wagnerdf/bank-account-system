-- ============================
-- CONTA BANCÁRIA DE TESTE 2
-- ============================
INSERT INTO bank_accounts (
    id,
    user_id,
    account_number,
    balance,
    created_at
) VALUES (
    2,
    1,               -- mesmo usuário teste
    'ACC-0002',
    500.00,          -- saldo inicial
    NOW()
);
