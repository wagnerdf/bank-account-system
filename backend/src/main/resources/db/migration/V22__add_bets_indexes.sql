CREATE INDEX idx_bet_game ON bets(game_id);
CREATE INDEX idx_bet_score ON bets(predicted_score);
CREATE INDEX idx_bet_game_score ON bets(game_id, predicted_score);
