-- Ajusta a sequence da tabela user_accounts para o maior ID existente

SELECT setval(
    'user_accounts_id_seq',
    COALESCE((SELECT MAX(id) FROM user_accounts), 1),
    true
);

TRUNCATE TABLE user_accounts RESTART IDENTITY CASCADE;