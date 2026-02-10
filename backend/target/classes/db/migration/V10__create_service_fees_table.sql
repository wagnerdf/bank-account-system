CREATE TABLE service_fees (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    percentual NUMERIC(5,2) NOT NULL,
    service_type VARCHAR(50) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);
