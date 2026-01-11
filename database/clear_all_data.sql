-- ============================================
-- Script pentru ștergerea COMPLETĂ a tuturor datelor
-- ATENȚIE: Această operațiune este IRREVERSIBILĂ!
-- ============================================

-- Șterge toate datele din tabele (în ordinea corectă pentru a respecta foreign keys)

-- 1. Șterge joined_lobbies (depinde de users)
DELETE FROM joined_lobbies;

-- 2. Șterge user_photos (depinde de users)
DELETE FROM user_photos;

-- 3. Șterge password_reset_tokens (depinde de users)
DELETE FROM password_reset_tokens;

-- 4. Șterge email_verification_codes (nu depinde de users, dar e bine să o ștergem)
DELETE FROM email_verification_codes;

-- 5. Șterge users (ultimul, pentru că alte tabele depind de el)
DELETE FROM users;

-- Verifică că totul a fost șters
SELECT 'Users count: ' || COUNT(*) FROM users;
SELECT 'Email verification codes count: ' || COUNT(*) FROM email_verification_codes;
SELECT 'Password reset tokens count: ' || COUNT(*) FROM password_reset_tokens;
SELECT 'User photos count: ' || COUNT(*) FROM user_photos;
SELECT 'Joined lobbies count: ' || COUNT(*) FROM joined_lobbies;

-- Mesaj de confirmare
SELECT '✅ Toate datele au fost șterse cu succes!' AS status;

