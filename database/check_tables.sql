-- Verifică dacă tabelul email_verification_codes există
SELECT 
    table_name,
    column_name,
    data_type
FROM information_schema.columns
WHERE table_name = 'email_verification_codes'
ORDER BY ordinal_position;

-- Verifică dacă există date
SELECT COUNT(*) as count FROM email_verification_codes;

