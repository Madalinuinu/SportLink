/**
 * Lobby Routes
 * 
 * Endpoints:
 * GET /api/lobbies - Get all lobbies with participants count
 * GET /api/lobbies/:id - Get lobby by ID with participants
 * POST /api/lobbies - Create new lobby
 * POST /api/lobbies/:id/join - Join a lobby
 */

const express = require('express');
const { query } = require('../config/database');
const { authenticateToken } = require('../middleware/auth');

const router = express.Router();

/**
 * GET /api/lobbies
 * Get all lobbies with participants count and creator info
 */
router.get('/', async (req, res) => {
    try {
        const result = await query(`
            SELECT 
                l.id,
                l.sport_name as "sportName",
                l.location,
                l.location_lat as "locationLat",
                l.location_lng as "locationLng",
                l.date,
                l.max_players as "maxPlayers",
                l.description,
                l.created_at as "createdAt",
                u.nickname as "creatorNickname",
                u.email as "creatorEmail",
                COUNT(DISTINCT lp.user_id) as "joinedPlayers"
            FROM lobbies l
            LEFT JOIN users u ON l.creator_id = u.id
            LEFT JOIN lobby_participants lp ON l.id = lp.lobby_id
            GROUP BY l.id, u.nickname, u.email
            ORDER BY l.created_at DESC
        `);
        
        const lobbies = result.rows.map(row => ({
            id: row.id,
            sportName: row.sportName,
            location: row.location,
            locationLat: row.locationLat ? parseFloat(row.locationLat) : null,
            locationLng: row.locationLng ? parseFloat(row.locationLng) : null,
            date: row.date,
            maxPlayers: row.maxPlayers,
            joinedPlayers: parseInt(row.joinedPlayers) || 0,
            description: row.description,
            createdAt: row.createdAt,
            creatorNickname: row.creatorNickname,
            creatorEmail: row.creatorEmail
        }));
        
        res.json(lobbies);
    } catch (error) {
        console.error('Get lobbies error:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

/**
 * GET /api/lobbies/:id
 * Get lobby by ID with participants list
 */
router.get('/:id', async (req, res) => {
    try {
        const { id } = req.params;
        
        // Get lobby details
        const lobbyResult = await query(`
            SELECT 
                l.id,
                l.creator_id as "creatorId",
                l.sport_name as "sportName",
                l.location,
                l.location_lat as "locationLat",
                l.location_lng as "locationLng",
                l.date,
                l.max_players as "maxPlayers",
                l.description,
                l.created_at as "createdAt",
                u.nickname as "creatorNickname",
                u.email as "creatorEmail"
            FROM lobbies l
            LEFT JOIN users u ON l.creator_id = u.id
            WHERE l.id = $1
        `, [id]);
        
        if (lobbyResult.rows.length === 0) {
            return res.status(404).json({ error: 'Lobby not found' });
        }
        
        const lobby = lobbyResult.rows[0];
        
        // Get participants
        const participantsResult = await query(`
            SELECT 
                lp.user_id as "userId",
                u.nickname,
                u.email,
                lp.joined_at as "joinedAt"
            FROM lobby_participants lp
            LEFT JOIN users u ON lp.user_id = u.id
            WHERE lp.lobby_id = $1
            ORDER BY lp.joined_at ASC
        `, [id]);
        
        const participants = participantsResult.rows.map(row => ({
            userId: row.userId,
            nickname: row.nickname,
            email: row.email,
            joinedAt: row.joinedAt
        }));
        
        res.json({
            id: lobby.id,
            creatorId: lobby.creatorId,
            sportName: lobby.sportName,
            location: lobby.location,
            locationLat: lobby.locationLat ? parseFloat(lobby.locationLat) : null,
            locationLng: lobby.locationLng ? parseFloat(lobby.locationLng) : null,
            date: lobby.date,
            maxPlayers: lobby.maxPlayers,
            joinedPlayers: participants.length,
            description: lobby.description,
            createdAt: lobby.createdAt,
            creatorNickname: lobby.creatorNickname,
            creatorEmail: lobby.creatorEmail,
            participants: participants
        });
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
        const { 
            sportName, 
            location, 
            locationLat, 
            locationLng, 
            date, 
            maxPlayers, 
            description 
        } = req.body;
        const userId = req.userId; // From JWT token
        
        // Validare input
        if (!sportName || !location || !date || !maxPlayers) {
            return res.status(400).json({ 
                error: 'Missing required fields: sportName, location, date, maxPlayers' 
            });
        }
        
        // Validare maxPlayers
        const maxPlayersInt = parseInt(maxPlayers);
        if (isNaN(maxPlayersInt) || maxPlayersInt < 1) {
            return res.status(400).json({ 
                error: 'maxPlayers must be a positive integer' 
            });
        }
        
        // Insert lobby
        const result = await query(`
            INSERT INTO lobbies (
                creator_id, 
                sport_name, 
                location, 
                location_lat, 
                location_lng, 
                date, 
                max_players, 
                description
            )
            VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
            RETURNING 
                id,
                created_at as "createdAt"
        `, [
            userId,
            sportName,
            location,
            locationLat || null,
            locationLng || null,
            date,
            maxPlayersInt,
            description || null
        ]);
        
        const newLobby = result.rows[0];
        
        // Creator joins automatically
        await query(`
            INSERT INTO lobby_participants (lobby_id, user_id)
            VALUES ($1, $2)
            ON CONFLICT (lobby_id, user_id) DO NOTHING
        `, [newLobby.id, userId]);
        
        // Get creator info
        const creatorResult = await query(`
            SELECT nickname, email
            FROM users
            WHERE id = $1
        `, [userId]);
        
        const creator = creatorResult.rows[0];
        
        res.status(201).json({
            id: newLobby.id,
            sportName,
            location,
            locationLat: locationLat ? parseFloat(locationLat) : null,
            locationLng: locationLng ? parseFloat(locationLng) : null,
            date,
            maxPlayers: maxPlayersInt,
            joinedPlayers: 1,
            description: description || null,
            createdAt: newLobby.createdAt,
            creatorNickname: creator.nickname,
            creatorEmail: creator.email
        });
    } catch (error) {
        console.error('Create lobby error:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

/**
 * POST /api/lobbies/:id/join
 * Join a lobby
 * 
 * Requires authentication (JWT token)
 */
router.post('/:id/join', authenticateToken, async (req, res) => {
    try {
        const { id } = req.params;
        const userId = req.userId;
        
        // Check if lobby exists
        const lobbyResult = await query(`
            SELECT max_players as "maxPlayers"
            FROM lobbies
            WHERE id = $1
        `, [id]);
        
        if (lobbyResult.rows.length === 0) {
            return res.status(404).json({ error: 'Lobby not found' });
        }
        
        const maxPlayers = lobbyResult.rows[0].maxPlayers;
        
        // Check current participants count
        const participantsResult = await query(`
            SELECT COUNT(*) as count
            FROM lobby_participants
            WHERE lobby_id = $1
        `, [id]);
        
        const currentCount = parseInt(participantsResult.rows[0].count);
        
        if (currentCount >= maxPlayers) {
            return res.status(400).json({ error: 'Lobby is full' });
        }
        
        // Check if user already joined
        const existingResult = await query(`
            SELECT id
            FROM lobby_participants
            WHERE lobby_id = $1 AND user_id = $2
        `, [id, userId]);
        
        if (existingResult.rows.length > 0) {
            return res.status(400).json({ error: 'Already joined this lobby' });
        }
        
        // Join lobby
        await query(`
            INSERT INTO lobby_participants (lobby_id, user_id)
            VALUES ($1, $2)
        `, [id, userId]);
        
        res.json({ message: 'Successfully joined lobby' });
    } catch (error) {
        console.error('Join lobby error:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

/**
 * DELETE /api/lobbies/:id/leave
 * Leave a lobby
 * 
 * If user is creator, deletes the entire lobby.
 * If user is participant, only removes from participants.
 * 
 * Requires authentication (JWT token)
 */
router.delete('/:id/leave', authenticateToken, async (req, res) => {
    try {
        const { id } = req.params;
        const userId = req.userId;
        
        // Check if lobby exists and get creator_id
        const lobbyResult = await query(`
            SELECT creator_id
            FROM lobbies
            WHERE id = $1
        `, [id]);
        
        if (lobbyResult.rows.length === 0) {
            return res.status(404).json({ error: 'Lobby not found' });
        }
        
        const creatorId = lobbyResult.rows[0].creator_id;
        
        // Check if user is creator
        if (creatorId === userId) {
            // Creator leaves - delete entire lobby (CASCADE will delete participants)
            await query(`
                DELETE FROM lobbies
                WHERE id = $1
            `, [id]);
            
            res.json({ message: 'Lobby deleted successfully' });
        } else {
            // Participant leaves - only remove from participants
            const deleteResult = await query(`
                DELETE FROM lobby_participants
                WHERE lobby_id = $1 AND user_id = $2
            `, [id, userId]);
            
            if (deleteResult.rowCount === 0) {
                return res.status(400).json({ error: 'You are not a participant of this lobby' });
            }
            
            res.json({ message: 'Left lobby successfully' });
        }
    } catch (error) {
        console.error('Leave lobby error:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

module.exports = router;

