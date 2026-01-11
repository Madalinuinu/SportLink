-- Verifică dacă există utilizatori în baza de date
SELECT 
    id,
    email,
    nickname,
    created_at
FROM users
ORDER BY created_at DESC;

-- Verifică și codurile de verificare
SELECT 
    email,
    code,
    expires_at,
    created_at
FROM email_verification_codes
ORDER BY created_at DESC;

