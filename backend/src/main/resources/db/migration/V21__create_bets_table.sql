CREATE TABLE bets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    game_id BIGINT NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    net_amount NUMERIC(19,2) NOT NULL,
    predicted_score VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user
        FOREIGN KEY (user_id) REFERENCES user_accounts(id),

    CONSTRAINT fk_game
        FOREIGN KEY (game_id) REFERENCES games(id)
);
