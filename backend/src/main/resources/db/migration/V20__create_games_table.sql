CREATE TABLE games (
    id BIGSERIAL PRIMARY KEY,
    round_id BIGINT NOT NULL,
    home_team VARCHAR(100) NOT NULL,
    away_team VARCHAR(100) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    final_score VARCHAR(20),
    status VARCHAR(20) NOT NULL,
    pool_account_id BIGINT NOT NULL,
    settled_at TIMESTAMP,

    CONSTRAINT fk_round
        FOREIGN KEY (round_id) REFERENCES rounds(id),

    CONSTRAINT fk_pool_account
        FOREIGN KEY (pool_account_id) REFERENCES bank_accounts(id)
);
