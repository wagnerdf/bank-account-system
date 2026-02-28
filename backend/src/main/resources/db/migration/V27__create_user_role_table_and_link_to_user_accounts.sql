-- 1️⃣ Create the user_role table.
CREATE TABLE user_role (
    id SERIAL PRIMARY KEY,
    name VARCHAR(20) UNIQUE NOT NULL,
    description VARCHAR(100)
);

-- 2️⃣ Insert existing profiles
INSERT INTO user_role (name, description)
VALUES 
    ('ADMIN', 'Administrador do sistema'),
    ('USER', 'Usuário padrão'),
    ('MANAGER', 'Gerente do sistema'),
    ('AUDITOR', 'Auditor do sistema');

-- 3️⃣ Add the role_id column to user_accounts.
ALTER TABLE user_accounts
ADD COLUMN role_id INT;

-- 4️⃣ Update role_id based on the existing role column.
UPDATE user_accounts ua
SET role_id = ur.id
FROM user_role ur
WHERE ua.role = ur.name;

-- 5️⃣ Make role_id NOT NULL
ALTER TABLE user_accounts
ALTER COLUMN role_id SET NOT NULL;

-- 6️⃣ Create a foreign key for referential integrity.
ALTER TABLE user_accounts
ADD CONSTRAINT fk_user_role
FOREIGN KEY (role_id) REFERENCES user_role(id);

-- 7️⃣ Remove old column roll
ALTER TABLE user_accounts
DROP COLUMN role;