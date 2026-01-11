/**
 * Test script pentru verificarea configurației email
 * Rulează: node test-email.js
 */

require('dotenv').config();
const nodemailer = require('nodemailer');

console.log('🔍 Verificare configurație email...\n');
console.log('SMTP_HOST:', process.env.SMTP_HOST);
console.log('SMTP_PORT:', process.env.SMTP_PORT);
console.log('SMTP_USER:', process.env.SMTP_USER);
console.log('SMTP_PASSWORD:', process.env.SMTP_PASSWORD ? '***' + process.env.SMTP_PASSWORD.slice(-4) : 'NOT SET');
console.log('');

// Verifică dacă toate variabilele sunt setate
if (!process.env.SMTP_HOST || !process.env.SMTP_PORT || !process.env.SMTP_USER || !process.env.SMTP_PASSWORD) {
    console.error('❌ EROARE: Lipsește configurația email în .env!');
    process.exit(1);
}

// Creează transporter
const transporter = nodemailer.createTransport({
    host: process.env.SMTP_HOST,
    port: parseInt(process.env.SMTP_PORT),
    secure: false,
    auth: {
        user: process.env.SMTP_USER,
        pass: process.env.SMTP_PASSWORD.replace(/\s/g, '') // Elimină spațiile
    }
});

// Testează conexiunea
console.log('📧 Testare conexiune SMTP...\n');

transporter.verify(function(error, success) {
    if (error) {
        console.error('❌ EROARE la conexiunea SMTP:');
        console.error(error);
        process.exit(1);
    } else {
        console.log('✅ Conexiune SMTP reușită!');
        console.log('');
        console.log('📨 Trimitere email de test...\n');
        
        // Trimite email de test
        transporter.sendMail({
            from: `"SportLink Test" <${process.env.SMTP_USER}>`,
            to: process.env.SMTP_USER, // Trimite la tine
            subject: 'Test Email - SportLink',
            text: 'Acesta este un email de test. Dacă primești acest email, configurația funcționează!',
            html: '<p>Acesta este un email de test. Dacă primești acest email, configurația funcționează!</p>'
        }, (error, info) => {
            if (error) {
                console.error('❌ EROARE la trimiterea email-ului:');
                console.error(error);
                process.exit(1);
            } else {
                console.log('✅ Email trimis cu succes!');
                console.log('Message ID:', info.messageId);
                console.log('');
                console.log('📬 Verifică inbox-ul la:', process.env.SMTP_USER);
            }
        });
    }
});

