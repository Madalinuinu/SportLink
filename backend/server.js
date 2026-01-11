/**
 * SportLink Backend Server
 * 
 * Express server pentru API REST cu PostgreSQL
 * Entry point pentru backend
 */

const express = require('express');
const cors = require('cors');
require('dotenv').config();

const authRoutes = require('./routes/auth');
const userRoutes = require('./routes/users');
const lobbyRoutes = require('./routes/lobbies');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors({
    origin: process.env.CORS_ORIGIN || '*',
    credentials: true
}));
app.use(express.json()); // Parse JSON bodies
app.use(express.urlencoded({ extended: true })); // Parse URL-encoded bodies

// Logging middleware (pentru debugging)
app.use((req, res, next) => {
    console.log(`${new Date().toISOString()} - ${req.method} ${req.path}`);
    next();
});

// Routes
app.use('/api/auth', authRoutes);
app.use('/api/users', userRoutes);
app.use('/api/lobbies', lobbyRoutes);

// Health check endpoint
app.get('/api/health', (req, res) => {
    res.json({ 
        status: 'OK', 
        message: 'SportLink Backend API is running',
        timestamp: new Date().toISOString()
    });
});

// Root endpoint
app.get('/', (req, res) => {
    res.json({ 
        message: 'SportLink Backend API',
        version: '1.0.0',
        endpoints: {
            health: '/api/health',
            auth: {
                register: 'POST /api/auth/register (sends verification code)',
                verifyEmail: 'POST /api/auth/verify-email (verify code and create account)',
                login: 'POST /api/auth/login',
                forgotPassword: 'POST /api/auth/forgot-password',
                resetPassword: 'POST /api/auth/reset-password'
            },
            users: {
                getProfile: 'GET /api/users/profile',
                updateProfile: 'PUT /api/users/profile',
                deleteAccount: 'DELETE /api/users/account'
            },
            lobbies: {
                getAll: 'GET /api/lobbies',
                getById: 'GET /api/lobbies/:id',
                create: 'POST /api/lobbies'
            }
        }
    });
});

// Error handling middleware
app.use((err, req, res, next) => {
    console.error('Error:', err);
    res.status(500).json({ 
        error: 'Internal server error',
        message: err.message 
    });
});

// 404 handler
app.use((req, res) => {
    res.status(404).json({ error: 'Route not found' });
});

// Start server
app.listen(PORT, () => {
    console.log('🚀 SportLink Backend Server');
    console.log(`📡 Server running on http://localhost:${PORT}`);
    console.log(`📊 Environment: ${process.env.NODE_ENV || 'development'}`);
    console.log(`🗄️  Database: ${process.env.DB_NAME}@${process.env.DB_HOST}:${process.env.DB_PORT}`);
    console.log('');
    console.log('Available endpoints:');
    console.log('  GET  /api/health');
    console.log('  POST /api/auth/register (sends verification code)');
    console.log('  POST /api/auth/verify-email (verify code and create account)');
    console.log('  POST /api/auth/login');
    console.log('  POST /api/auth/forgot-password');
    console.log('  POST /api/auth/reset-password');
    console.log('  GET  /api/users/profile');
    console.log('  PUT  /api/users/profile');
    console.log('  DELETE /api/users/account');
    console.log('  GET  /api/lobbies');
    console.log('  GET  /api/lobbies/:id');
    console.log('  POST /api/lobbies');
});

