/**
 * Authentication REST API Routes
 * Endpoints:
 * - POST /api/auth/register-guest
 * - POST /api/auth/google-login
 * - GET  /api/auth/me
 */

const express = require('express');
const router = express.Router();
const User = require('../models/User');
const { generateToken, requireAuth } = require('../middleware/authMiddleware');

// Guest / Username Registration (Instant Play)
router.post('/register-guest', async (req, res) => {
  try {
    const { username } = req.body;
    if (!username || !username.trim()) {
      return res.status(400).json({ success: false, message: 'Username is required.' });
    }

    const cleanName = username.trim();
    let user = await User.findByUsername(cleanName);
    if (!user) {
      user = await User.create({ username: cleanName });
    }

    const token = generateToken({ id: user.id, username: user.username });
    res.json({
      success: true,
      message: 'Logged in successfully.',
      token,
      user
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// Google Sign-In Exchange (Firebase Credential Manager)
router.post('/google-login', async (req, res) => {
  try {
    const { googleId, email, displayName, avatar } = req.body;
    if (!googleId) {
      return res.status(400).json({ success: false, message: 'Google ID is required.' });
    }

    let user = await User.findByGoogleId(googleId);
    if (!user) {
      user = await User.create({
        googleId,
        email: email || '',
        username: displayName || 'Adventurer Explorer',
        avatar: avatar || 'default'
      });
    }

    const token = generateToken({ id: user.id, username: user.username, email: user.email });
    res.json({
      success: true,
      message: 'Google authentication successful.',
      token,
      user
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// Get Current Logged-in User
router.get('/me', requireAuth, async (req, res) => {
  try {
    const user = await User.findById(req.user.id);
    if (!user) {
      return res.json({ success: true, user: req.user });
    }
    res.json({ success: true, user });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

module.exports = router;
