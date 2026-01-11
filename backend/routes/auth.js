/**
 * Authentication Routes
 * 
 * Endpoints:
 * POST /api/auth/register - Register new user
 * POST /api/auth/login - Login user
 * POST /api/auth/forgot-password - Request password reset
 * POST /api/auth/reset-password - Reset password with token
 */

const express = require('express');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const { v4: uuidv4 } = require('uuid');
const { query } = require('../config/database');
const { sendVerificationCode } = require('../services/emailService');
require('dotenv').config();

const router = express.Router();

/**
 * POST /api/auth/register
 * Register new user - trimite cod de verificare pe email
 * NU creează contul direct - așteaptă verificarea codului
 */
router.post('/register', async (req, res) => {
    try {
        const { email, password, nickname } = req.body;
        
        // Validare input
        if (!email || !password || !nickname) {
            return res.status(400).json({ 
                error: 'Missing required fields: email, password, nickname' 
            });
        }
        
        // Validare email format
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            return res.status(400).json({ error: 'Invalid email format' });
        }
        
        // Validare parolă: minim 8 caractere, literă mare, cifră
        if (password.length < 8) {
            return res.status(400).json({ 
                error: 'Password must be at least 8 characters long' 
            });
        }
        
        // Verifică dacă parola conține cel puțin o literă mare
        if (!/[A-Z]/.test(password)) {
            return res.status(400).json({ 
                error: 'Password must contain at least one uppercase letter' 
            });
        }
        
        // Verifică dacă parola conține cel puțin o cifră
        if (!/[0-9]/.test(password)) {
            return res.status(400).json({ 
                error: 'Password must contain at least one digit' 
            });
        }
        
        // Verifică dacă email există deja
        const existingUser = await query(
            'SELECT id FROM users WHERE email = $1',
            [email]
        );
        
        if (existingUser.rows.length > 0) {
            return res.status(409).json({ error: 'Email already exists' });
        }
        
        // Generează cod de verificare (6 cifre)
        const verificationCode = Math.floor(100000 + Math.random() * 900000).toString();
        
        // Expiră în 10 minute
        const expiresAt = new Date();
        expiresAt.setMinutes(expiresAt.getMinutes() + 10);
        
        // Șterge codurile vechi pentru acest email
        await query(
            'DELETE FROM email_verification_codes WHERE email = $1',
            [email]
        );
        
        // Salvează codul în baza de date
        await query(
            `INSERT INTO email_verification_codes (email, code, expires_at)
             VALUES ($1, $2, $3)`,
            [email, verificationCode, expiresAt]
        );
        
        // Hash parolă temporar (va fi salvată după verificare)
        const saltRounds = 10;
        const passwordHash = await bcrypt.hash(password, saltRounds);
        
        // Salvează datele temporare (email, password_hash, nickname) în cod
        // Pentru simplitate, le salvăm în code-ul de verificare (în producție, folosește un tabel separat)
        // Sau poți folosi un tabel pending_registrations
        
        // Trimite email cu codul de verificare
        const emailSent = await sendVerificationCode(email, verificationCode);
        
        if (!emailSent) {
            // Dacă email-ul nu a putut fi trimis, returnează eroare
            return res.status(500).json({ 
                error: 'Failed to send verification email. Please check your email configuration.' 
            });
        }
        
        // Returnează success - codul a fost trimis pe email
        res.json({
            message: 'Verification code sent to email',
            // Pentru testare în development, returnează codul (șterge în producție!)
            code: process.env.NODE_ENV === 'development' ? verificationCode : undefined
        });
    } catch (error) {
        console.error('Register error:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

/**
 * POST /api/auth/verify-email
 * Verifică codul de email și creează contul
 */
router.post('/verify-email', async (req, res) => {
    try {
        const { email, code, password, nickname } = req.body;
        
        // Validare input
        if (!email || !code || !password || !nickname) {
            return res.status(400).json({ 
                error: 'Missing required fields: email, code, password, nickname' 
            });
        }
        
        // Găsește codul de verificare
        const codeResult = await query(
            `SELECT code, expires_at 
             FROM email_verification_codes 
             WHERE email = $1 AND code = $2`,
            [email, code]
        );
        
        if (codeResult.rows.length === 0) {
            return res.status(400).json({ error: 'Invalid verification code' });
        }
        
        const { expires_at } = codeResult.rows[0];
        
        // Verifică dacă codul a expirat
        if (new Date(expires_at) < new Date()) {
            // Șterge codul expirat
            await query(
                'DELETE FROM email_verification_codes WHERE email = $1',
                [email]
            );
            return res.status(400).json({ error: 'Verification code has expired' });
        }
        
        // Verifică dacă email există deja (edge case)
        const existingUser = await query(
            'SELECT id FROM users WHERE email = $1',
            [email]
        );
        
        if (existingUser.rows.length > 0) {
            return res.status(409).json({ error: 'Email already exists' });
        }
        
        // Hash parolă cu bcrypt (10 rounds)
        const saltRounds = 10;
        const passwordHash = await bcrypt.hash(password, saltRounds);
        
        // Creează user în baza de date
        const result = await query(
            `INSERT INTO users (email, password_hash, nickname)
             VALUES ($1, $2, $3)
             RETURNING id, email, nickname, profile_picture_url, bio, created_at`,
            [email, passwordHash, nickname]
        );
        
        const user = result.rows[0];
        
        // Șterge codul de verificare (folosit o singură dată)
        await query(
            'DELETE FROM email_verification_codes WHERE email = $1',
            [email]
        );
        
        // Generează JWT token
        const token = jwt.sign(
            { userId: user.id },
            process.env.JWT_SECRET,
            { expiresIn: process.env.JWT_EXPIRES_IN || '7d' }
        );
        
        res.status(201).json({
            token,
            user: {
                userId: user.id,
                email: user.email,
                nickname: user.nickname,
                profilePictureUrl: user.profile_picture_url,
                bio: user.bio
            }
        });
    } catch (error) {
        console.error('Verify email error:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

/**
 * POST /api/auth/login
 * Login user with email and password
 */
router.post('/login', async (req, res) => {
    try {
        const { email, password } = req.body;
        
        // Validare input
        if (!email || !password) {
            return res.status(400).json({ 
                error: 'Missing email or password' 
            });
        }
        
        // Găsește user după email
        const result = await query(
            'SELECT * FROM users WHERE email = $1',
            [email]
        );
        
        if (result.rows.length === 0) {
            return res.status(401).json({ error: 'Invalid email or password' });
        }
        
        const user = result.rows[0];
        
        // Verifică parola
        const validPassword = await bcrypt.compare(password, user.password_hash);
        if (!validPassword) {
            return res.status(401).json({ error: 'Invalid email or password' });
        }
        
        // Generează JWT token
        const token = jwt.sign(
            { userId: user.id },
            process.env.JWT_SECRET,
            { expiresIn: process.env.JWT_EXPIRES_IN || '7d' }
        );
        
        res.json({
            token,
            user: {
                userId: user.id,
                email: user.email,
                nickname: user.nickname,
                profilePictureUrl: user.profile_picture_url,
                bio: user.bio
            }
        });
    } catch (error) {
        console.error('Login error:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

/**
 * POST /api/auth/forgot-password
 * Request password reset - trimite cod de verificare pe email (similar cu register)
 */
router.post('/forgot-password', async (req, res) => {
    try {
        const { email } = req.body;
        
        if (!email) {
            return res.status(400).json({ error: 'Email is required' });
        }
        
        // Găsește user după email
        const result = await query(
            'SELECT id FROM users WHERE email = $1',
            [email]
        );
        
        if (result.rows.length === 0) {
            // Pentru securitate, nu dezvăluie dacă email-ul există
            // Returnează același mesaj pentru a nu dezvălui dacă email-ul există
            return res.json({ 
                message: 'If the email exists, a reset code has been sent',
                code: null
            });
        }
        
        // Generează cod de verificare (6 cifre)
        const resetCode = Math.floor(100000 + Math.random() * 900000).toString();
        
        // Expiră în 1 oră
        const expiresAt = new Date();
        expiresAt.setHours(expiresAt.getHours() + 1);
        
        // Șterge codurile vechi pentru acest email
        await query(
            'DELETE FROM email_verification_codes WHERE email = $1',
            [email]
        );
        
        // Salvează codul în baza de date
        await query(
            `INSERT INTO email_verification_codes (email, code, expires_at)
             VALUES ($1, $2, $3)`,
            [email, resetCode, expiresAt]
        );
        
        // Trimite email cu codul de verificare
        const { sendPasswordResetCode } = require('../services/emailService');
        const emailSent = await sendPasswordResetCode(email, resetCode);
        
        if (!emailSent) {
            return res.status(500).json({ 
                error: 'Failed to send reset code. Please check your email configuration.' 
            });
        }
        
        // Returnează success - codul a fost trimis pe email
        res.json({
            message: 'Reset code sent to email',
            // Pentru testare în development, returnează codul (șterge în producție!)
            code: process.env.NODE_ENV === 'development' ? resetCode : undefined
        });
    } catch (error) {
        console.error('Forgot password error:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

/**
 * POST /api/auth/reset-password
 * Reset password using code from forgot-password (similar cu verify-email)
 */
router.post('/reset-password', async (req, res) => {
    try {
        const { email, code, newPassword } = req.body;
        
        if (!email || !code || !newPassword) {
            return res.status(400).json({ 
                error: 'Email, code, and new password are required' 
            });
        }
        
        // Validare parolă: minim 8 caractere, literă mare, cifră
        if (newPassword.length < 8) {
            return res.status(400).json({ 
                error: 'Password must be at least 8 characters long' 
            });
        }
        
        // Verifică dacă parola conține cel puțin o literă mare
        if (!/[A-Z]/.test(newPassword)) {
            return res.status(400).json({ 
                error: 'Password must contain at least one uppercase letter' 
            });
        }
        
        // Verifică dacă parola conține cel puțin o cifră
        if (!/[0-9]/.test(newPassword)) {
            return res.status(400).json({ 
                error: 'Password must contain at least one digit' 
            });
        }
        
        // Găsește codul de verificare
        const codeResult = await query(
            `SELECT code, expires_at 
             FROM email_verification_codes 
             WHERE email = $1 AND code = $2`,
            [email, code]
        );
        
        if (codeResult.rows.length === 0) {
            return res.status(400).json({ error: 'Invalid or expired reset code' });
        }
        
        const { expires_at } = codeResult.rows[0];
        
        // Verifică dacă codul a expirat
        if (new Date(expires_at) < new Date()) {
            // Șterge codul expirat
            await query(
                'DELETE FROM email_verification_codes WHERE email = $1',
                [email]
            );
            return res.status(400).json({ error: 'Reset code has expired' });
        }
        
        // Găsește user-ul după email
        const userResult = await query(
            'SELECT id FROM users WHERE email = $1',
            [email]
        );
        
        if (userResult.rows.length === 0) {
            return res.status(404).json({ error: 'User not found' });
        }
        
        const userId = userResult.rows[0].id;
        
        // Hash noua parolă
        const saltRounds = 10;
        const passwordHash = await bcrypt.hash(newPassword, saltRounds);
        
        // Update parolă în users
        await query(
            'UPDATE users SET password_hash = $1 WHERE id = $2',
            [passwordHash, userId]
        );
        
        // Șterge codul de verificare (folosit o singură dată)
        await query(
            'DELETE FROM email_verification_codes WHERE email = $1',
            [email]
        );
        
        res.json({ message: 'Password reset successful' });
    } catch (error) {
        console.error('Reset password error:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

module.exports = router;

