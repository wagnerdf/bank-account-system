-- 1️⃣ Adicionar coluna description na tabela transactions
ALTER TABLE transactions
ADD COLUMN description VARCHAR(255) DEFAULT 'Sem descrição';

