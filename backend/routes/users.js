/**
 * User Routes
 * 
 * Endpoints:
 * GET /api/users/profile - Get current user profile
 * PUT /api/users/profile - Update user profile
 * DELETE /api/users/account - Delete user account
 */

const express = require('express');
const { query } = require('../config/database');
const { authenticateToken } = require('../middleware/auth');

const router = express.Router();

// Toate rutele necesită autentificare
router.use(authenticateToken);

/**
 * GET /api/users/profile
 * Get current user profile with photos
 */
router.get('/profile', async (req, res) => {
    try {
        const userId = req.userId;
        
        // Obține user
        const userResult = await query(
            'SELECT * FROM users WHERE id = $1',
            [userId]
        );
        
        if (userResult.rows.length === 0) {
            return res.status(404).json({ error: 'User not found' });
        }
        
        const user = userResult.rows[0];
        
        // Obține poze
        const photosResult = await query(
            'SELECT * FROM user_photos WHERE user_id = $1 ORDER BY display_order ASC',
            [userId]
        );
        
        res.json({
            userId: user.id,
            email: user.email,
            nickname: user.nickname,
            profilePictureUrl: user.profile_picture_url,
            bio: user.bio,
            photos: photosResult.rows.map(photo => ({
                id: photo.id,
                photoUrl: photo.photo_url,
                caption: photo.caption
            }))
        });
    } catch (error) {
        console.error('Get profile error:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

/**
 * PUT /api/users/profile
 * Update user profile (nickname and/or bio)
 */
router.put('/profile', async (req, res) => {
    try {
        const userId = req.userId;
        const { nickname, bio } = req.body;
        
        // Update doar câmpurile furnizate
        const updates = [];
        const values = [];
        let paramCount = 1;
        
        if (nickname !== undefined) {
            updates.push(`nickname = $${paramCount++}`);
            values.push(nickname);
        }
        
        if (bio !== undefined) {
            updates.push(`bio = $${paramCount++}`);
            values.push(bio);
        }
        
        if (updates.length === 0) {
            return res.status(400).json({ error: 'No fields to update' });
        }
        
        values.push(userId);
        
        const result = await query(
            `UPDATE users 
             SET ${updates.join(', ')}, updated_at = CURRENT_TIMESTAMP
             WHERE id = $${paramCount}
             RETURNING id, email, nickname, profile_picture_url, bio`,
            values
        );
        
        if (result.rows.length === 0) {
            return res.status(404).json({ error: 'User not found' });
        }
        
        res.json({
            userId: result.rows[0].id,
            email: result.rows[0].email,
            nickname: result.rows[0].nickname,
            profilePictureUrl: result.rows[0].profile_picture_url,
            bio: result.rows[0].bio
        });
    } catch (error) {
        console.error('Update profile error:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

/**
 * DELETE /api/users/account
 * Delete user account (CASCADE va șterge automat user_photos și joined_lobbies)
 */
router.delete('/account', async (req, res) => {
    try {
        const userId = req.userId;
        
        // Șterge user (CASCADE va șterge automat dependențele)
        const result = await query(
            'DELETE FROM users WHERE id = $1 RETURNING id',
            [userId]
        );
        
        if (result.rows.length === 0) {
            return res.status(404).json({ error: 'User not found' });
        }
        
        res.json({ message: 'Account deleted' });
    } catch (error) {
        console.error('Delete account error:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

module.exports = router;

