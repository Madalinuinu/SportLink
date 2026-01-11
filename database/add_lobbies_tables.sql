-- ============================================
-- Script SQL pentru adăugarea tabelelor lobbies și lobby_participants
-- Rulează acest script în pgAdmin pe baza de date sportlink_db
-- ============================================

-- ============================================
-- Tabel: lobbies (Lobby-uri create de utilizatori)
-- ============================================
CREATE TABLE IF NOT EXISTS lobbies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    creator_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    sport_name VARCHAR(100) NOT NULL,
    location VARCHAR(255) NOT NULL,
    location_lat DECIMAL(10, 8), -- Latitudine pentru Google Maps
    location_lng DECIMAL(11, 8), -- Longitudine pentru Google Maps
    date TIMESTAMP NOT NULL, -- Data și ora meciului
    max_players INTEGER NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexuri pentru performanță
CREATE INDEX IF NOT EXISTS idx_lobbies_creator_id ON lobbies(creator_id);
CREATE INDEX IF NOT EXISTS idx_lobbies_sport_name ON lobbies(sport_name);
CREATE INDEX IF NOT EXISTS idx_lobbies_date ON lobbies(date);
CREATE INDEX IF NOT EXISTS idx_lobbies_created_at ON lobbies(created_at DESC);

-- ============================================
-- Tabel: lobby_participants (Participanții la lobby-uri)
-- ============================================
CREATE TABLE IF NOT EXISTS lobby_participants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lobby_id UUID NOT NULL REFERENCES lobbies(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(lobby_id, user_id) -- Un user nu poate join de două ori același lobby
);

-- Indexuri pentru performanță
CREATE INDEX IF NOT EXISTS idx_lobby_participants_lobby_id ON lobby_participants(lobby_id);
CREATE INDEX IF NOT EXISTS idx_lobby_participants_user_id ON lobby_participants(user_id);

-- ============================================
-- Verificare: Verifică dacă tabelele au fost create cu succes
-- ============================================
SELECT 
    'lobbies' as table_name,
    COUNT(*) as row_count
FROM lobbies
UNION ALL
SELECT 
    'lobby_participants' as table_name,
    COUNT(*) as row_count
FROM lobby_participants;

