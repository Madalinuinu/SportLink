-- ============================================
-- SportLink Database Schema - PostgreSQL
-- ============================================
-- Baza de date pentru utilizatori, autentificare email/password, și lobby-uri
-- Compatibilă cu aplicația Android existentă

-- ============================================
-- 1. Tabel: users (Utilizatori)
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(100) NOT NULL,
    profile_picture_url TEXT,
    bio TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index pentru email (pentru login rapid)
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- ============================================
-- 2. Tabel: email_verification_codes (Coduri de verificare email)
-- ============================================
CREATE TABLE IF NOT EXISTS email_verification_codes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    code VARCHAR(6) NOT NULL, -- Cod de 6 cifre
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexuri pentru performanță
CREATE INDEX IF NOT EXISTS idx_email_verification_codes_email ON email_verification_codes(email);
CREATE INDEX IF NOT EXISTS idx_email_verification_codes_code ON email_verification_codes(code);
CREATE INDEX IF NOT EXISTS idx_email_verification_codes_expires_at ON email_verification_codes(expires_at);

-- ============================================
-- 3. Tabel: password_reset_tokens (Token-uri pentru resetare parolă)
-- ============================================
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexuri pentru performanță
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_token ON password_reset_tokens(token);
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_expires_at ON password_reset_tokens(expires_at);
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);

-- ============================================
-- 4. Tabel: user_photos (Poze utilizatori - Instagram-like)
-- ============================================
CREATE TABLE IF NOT EXISTS user_photos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    photo_url TEXT NOT NULL,
    caption TEXT,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexuri pentru performanță
CREATE INDEX IF NOT EXISTS idx_user_photos_user_id ON user_photos(user_id);
CREATE INDEX IF NOT EXISTS idx_user_photos_display_order ON user_photos(user_id, display_order);

-- ============================================
-- 5. Tabel: joined_lobbies (Lobby-uri join-uite de utilizatori)
-- ============================================
CREATE TABLE IF NOT EXISTS joined_lobbies (
    id VARCHAR(255) NOT NULL,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    sport_name VARCHAR(100) NOT NULL,
    location VARCHAR(255) NOT NULL,
    date VARCHAR(100) NOT NULL,
    max_players INTEGER NOT NULL,
    joined_players INTEGER NOT NULL,
    image_url TEXT,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id, user_id) -- Composite key: același lobby poate fi joinat de mai mulți useri
);

-- Indexuri pentru performanță
CREATE INDEX IF NOT EXISTS idx_joined_lobbies_user_id ON joined_lobbies(user_id);
CREATE INDEX IF NOT EXISTS idx_joined_lobbies_sport_name ON joined_lobbies(sport_name);

-- ============================================
-- 5. Funcții Utile
-- ============================================

-- Funcție pentru actualizare automată a updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger pentru actualizare automată updated_at în users
DROP TRIGGER IF EXISTS update_users_updated_at ON users;
CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON users
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- 6. Cleanup Function (pentru ștergere token-uri expirate)
-- ============================================

-- Funcție pentru ștergere automată a token-urilor expirate
CREATE OR REPLACE FUNCTION cleanup_expired_tokens()
RETURNS void AS $$
BEGIN
    DELETE FROM password_reset_tokens 
    WHERE expires_at < CURRENT_TIMESTAMP;
END;
$$ language 'plpgsql';

-- ============================================
-- 7. Views Utile (Opțional)
-- ============================================

-- View pentru profil complet al utilizatorului
CREATE OR REPLACE VIEW user_profiles AS
SELECT 
    u.id,
    u.email,
    u.nickname,
    u.profile_picture_url,
    u.bio,
    u.created_at,
    COUNT(DISTINCT up.id) as photo_count,
    COUNT(DISTINCT jl.id) as joined_lobbies_count
FROM users u
LEFT JOIN user_photos up ON u.id = up.user_id
LEFT JOIN joined_lobbies jl ON u.id = jl.user_id
GROUP BY u.id, u.email, u.nickname, u.profile_picture_url, u.bio, u.created_at;

-- ============================================
-- 8. Date de Test (Opțional - pentru development)
-- ============================================

-- User de test (password: "test123" - hash bcrypt: $2b$10$rOzJqZqZqZqZqZqZqZqZqOqZqZqZqZqZqZqZqZqZqZqZqZqZqZq)
-- IMPORTANT: În producție, folosește bcrypt pentru a genera hash-ul real!
-- INSERT INTO users (id, email, password_hash, nickname, profile_picture_url, bio)
-- VALUES (
--     '550e8400-e29b-41d4-a716-446655440000',
--     'test@example.com',
--     '$2b$10$rOzJqZqZqZqZqZqZqZqZqOqZqZqZqZqZqZqZqZqZqZqZqZqZqZqZqZqZqZqZqZq', -- Hash de test
--     'TestUser',
--     'https://via.placeholder.com/150',
--     'Acesta este un utilizator de test pentru development.'
-- ) ON CONFLICT (email) DO NOTHING;

-- ============================================
-- 9. Cleanup Script (Pentru resetare baza de date - DOAR DEVELOPMENT!)
-- ============================================
-- ATENȚIE: Aceste comenzi șterg TOATE datele!
-- Folosește doar în development!

-- DROP VIEW IF EXISTS user_profiles CASCADE;
-- DROP TABLE IF EXISTS joined_lobbies CASCADE;
-- DROP TABLE IF EXISTS user_photos CASCADE;
-- DROP TABLE IF EXISTS password_reset_tokens CASCADE;
-- DROP TABLE IF EXISTS users CASCADE;
-- DROP FUNCTION IF EXISTS update_updated_at_column() CASCADE;
-- DROP FUNCTION IF EXISTS cleanup_expired_tokens() CASCADE;

-- ============================================
-- END OF SCHEMA
-- ============================================

