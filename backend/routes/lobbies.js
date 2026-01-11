/**
 * Lobby Routes
 * 
 * Endpoints:
 * GET /api/lobbies - Get all lobbies
 * GET /api/lobbies/:id - Get lobby by ID
 * POST /api/lobbies - Create new lobby
 */

const express = require('express');
const { query } = require('../config/database');
const { authenticateToken } = require('../middleware/auth');

const router = express.Router();

/**
 * GET /api/lobbies
 * Get all lobbies
 * 
 * Note: Pentru moment, returnează date mockate
 * În viitor, poți stoca lobbies în PostgreSQL
 */
router.get('/', async (req, res) => {
    try {
        // Returnează lista goală - nu există lobby-uri predefinite
        // Utilizatorii pot crea lobby-uri noi prin POST /api/lobbies
        res.json([]);
    } catch (error) {
        console.error('Get lobbies error:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

/**
 * GET /api/lobbies/:id
 * Get lobby by ID
 */
router.get('/:id', async (req, res) => {
    try {
        const { id } = req.params;
        
        // Pentru moment, returnează date mockate
        // În viitor, poți căuta în PostgreSQL
        const mockLobby = {
            id: id,
            sportName: "Fotbal",
            location: "Parcul Central, București",
            date: "2024-03-15T18:00:00Z",
            maxPlayers: 10,
            joinedPlayers: 5,
            imageUrl: "https://via.placeholder.com/300",
            description: "Meci de fotbal în Parcul Central"
        };
        
        res.json(mockLobby);
    } catch (error) {
        console.error('Get lobby by ID error:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

/**
 * POST /api/lobbies
 * Create new lobby
 * 
 * Requires authentication (JWT token)
 */
router.post('/', authenticateToken, async (req, res) => {
    try {
        const { sportName, location, date, maxPlayers, imageUrl, description } = req.body;
        const userId = req.userId; // From JWT token
        
        // Validare input
        if (!sportName || !location || !date || !maxPlayers) {
            return res.status(400).json({ 
                error: 'Missing required fields: sportName, location, date, maxPlayers' 
            });
        }
        
        // Pentru moment, returnează date mockate
        // În viitor, poți salva în PostgreSQL
        const newLobby = {
            id: Date.now().toString(), // Temporary ID
            sportName,
            location,
            date,
            maxPlayers: parseInt(maxPlayers),
            joinedPlayers: 1, // Creator joins automatically
            imageUrl: imageUrl || null,
            description: description || null,
            createdAt: new Date().toISOString()
        };
        
        res.status(201).json(newLobby);
    } catch (error) {
        console.error('Create lobby error:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

module.exports = router;

