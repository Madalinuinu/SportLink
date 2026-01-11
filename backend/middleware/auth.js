/**
 * JWT Authentication Middleware
 * 
 * Verifică JWT token din header-ul Authorization
 * Adaugă userId în request pentru a fi folosit în routes
 */

const jwt = require('jsonwebtoken');
require('dotenv').config();

/**
 * Middleware pentru verificare JWT token
 * 
 * Header: Authorization: Bearer {token}
 */
const authenticateToken = (req, res, next) => {
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1]; // "Bearer TOKEN"
    
    if (!token) {
        return res.status(401).json({ 
            error: 'Access denied. No token provided.' 
        });
    }
    
    try {
        const decoded = jwt.verify(token, process.env.JWT_SECRET);
        req.userId = decoded.userId; // Adaugă userId în request
        next();
    } catch (error) {
        return res.status(403).json({ 
            error: 'Invalid or expired token.' 
        });
    }
};

module.exports = {
    authenticateToken
};

