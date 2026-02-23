-- Aumenta o tamanho da coluna type para suportar PLATFORM_FEE

ALTER TABLE transactions
ALTER COLUMN type TYPE VARCHAR(20);