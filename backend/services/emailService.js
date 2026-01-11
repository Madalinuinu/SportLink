/**
 * Email Service
 * 
 * Serviciu pentru trimiterea email-urilor folosind nodemailer
 * Suportă Gmail, Outlook, și alte servicii SMTP
 */

const nodemailer = require('nodemailer');
require('dotenv').config();

/**
 * Creează transporter pentru nodemailer
 * Configurarea se face din .env file
 */
const createTransporter = () => {
    // Elimină spațiile din App Password (Google generează cu spații, dar nodemailer preferă fără)
    const password = (process.env.SMTP_PASSWORD || '').replace(/\s/g, '');
    
    return nodemailer.createTransport({
        host: process.env.SMTP_HOST || 'smtp.gmail.com',
        port: parseInt(process.env.SMTP_PORT || '587'),
        secure: false, // true pentru 465, false pentru alte porturi
        auth: {
            user: process.env.SMTP_USER, // Email-ul tău (ex: your-email@gmail.com)
            pass: password // App Password (fără spații)
        }
    });
};

/**
 * Trimite cod de verificare email
 * 
 * @param {string} to - Email-ul destinatarului
 * @param {string} code - Codul de verificare (6 cifre)
 * @returns {Promise<boolean>} - true dacă email-ul a fost trimis cu succes
 */
const sendVerificationCode = async (to, code) => {
    try {
        const transporter = createTransporter();
        
        const mailOptions = {
            from: `"SportLink" <${process.env.SMTP_USER}>`,
            to: to,
            subject: 'Cod de verificare SportLink',
            html: `
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #4CAF50;">Bun venit la SportLink!</h2>
                    <p>Mulțumim că te-ai înregistrat. Pentru a-ți activa contul, folosește următorul cod de verificare:</p>
                    <div style="background-color: #f4f4f4; padding: 20px; text-align: center; margin: 20px 0;">
                        <h1 style="color: #2196F3; font-size: 32px; letter-spacing: 5px; margin: 0;">${code}</h1>
                    </div>
                    <p>Acest cod expiră în 10 minute.</p>
                    <p>Dacă nu ai solicitat acest cod, poți ignora acest email.</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="color: #999; font-size: 12px;">SportLink - Aplicație pentru comunitatea sportivă</p>
                </div>
            `,
            text: `Bun venit la SportLink!\n\nCodul tău de verificare este: ${code}\n\nAcest cod expiră în 10 minute.`
        };
        
        const info = await transporter.sendMail(mailOptions);
        console.log('Email sent:', info.messageId);
        return true;
    } catch (error) {
        console.error('Error sending email:', error);
        return false;
    }
};

/**
 * Trimite cod de verificare pentru resetare parolă
 * 
 * @param {string} to - Email-ul destinatarului
 * @param {string} code - Codul de verificare (6 cifre)
 * @returns {Promise<boolean>} - true dacă email-ul a fost trimis cu succes
 */
const sendPasswordResetCode = async (to, code) => {
    try {
        const transporter = createTransporter();
        
        const mailOptions = {
            from: `"SportLink" <${process.env.SMTP_USER}>`,
            to: to,
            subject: 'Cod de resetare parolă SportLink',
            html: `
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #4CAF50;">Resetare Parolă</h2>
                    <p>Ai solicitat resetarea parolei pentru contul tău SportLink.</p>
                    <p>Folosește următorul cod pentru a reseta parola:</p>
                    <div style="background-color: #f4f4f4; padding: 20px; text-align: center; margin: 20px 0;">
                        <h1 style="color: #2196F3; font-size: 32px; letter-spacing: 5px; margin: 0;">${code}</h1>
                    </div>
                    <p>Acest cod expiră în 1 oră.</p>
                    <p>Dacă nu ai solicitat resetarea parolei, poți ignora acest email.</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="color: #999; font-size: 12px;">SportLink - Aplicație pentru comunitatea sportivă</p>
                </div>
            `,
            text: `Resetare Parolă\n\nCodul tău de resetare este: ${code}\n\nAcest cod expiră în 1 oră.\n\nDacă nu ai solicitat resetarea parolei, poți ignora acest email.`
        };
        
        const info = await transporter.sendMail(mailOptions);
        console.log('Password reset email sent:', info.messageId);
        return true;
    } catch (error) {
        console.error('Error sending password reset email:', error);
        return false;
    }
};

/**
 * Trimite email pentru resetare parolă (legacy - folosește link)
 * 
 * @param {string} to - Email-ul destinatarului
 * @param {string} resetLink - Link-ul de resetare parolă
 * @returns {Promise<boolean>} - true dacă email-ul a fost trimis cu succes
 */
const sendPasswordResetLink = async (to, resetLink) => {
    try {
        const transporter = createTransporter();
        
        const mailOptions = {
            from: `"SportLink" <${process.env.SMTP_USER}>`,
            to: to,
            subject: 'Resetare parolă SportLink',
            html: `
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #4CAF50;">Resetare Parolă</h2>
                    <p>Ai solicitat resetarea parolei pentru contul tău SportLink.</p>
                    <p>Click pe link-ul de mai jos pentru a reseta parola:</p>
                    <div style="text-align: center; margin: 20px 0;">
                        <a href="${resetLink}" style="background-color: #2196F3; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block;">Resetează Parola</a>
                    </div>
                    <p>Acest link expiră în 1 oră.</p>
                    <p>Dacă nu ai solicitat resetarea parolei, poți ignora acest email.</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="color: #999; font-size: 12px;">SportLink - Aplicație pentru comunitatea sportivă</p>
                </div>
            `,
            text: `Resetare Parolă\n\nClick pe link-ul de mai jos pentru a reseta parola:\n${resetLink}\n\nAcest link expiră în 1 oră.`
        };
        
        const info = await transporter.sendMail(mailOptions);
        console.log('Password reset email sent:', info.messageId);
        return true;
    } catch (error) {
        console.error('Error sending password reset email:', error);
        return false;
    }
};

module.exports = {
    sendVerificationCode,
    sendPasswordResetCode,
    sendPasswordResetLink
};

