/**
 * User & Companion REST API Routes
 * Endpoints:
 * - GET  /api/users/profile
 * - PUT  /api/users/profile
 * - POST /api/users/upgrade-companion
 */

const express = require('express');
const router = express.Router();
const User = require('../models/User');
const { requireAuth } = require('../middleware/authMiddleware');

// Get User Profile
router.get('/profile', requireAuth, async (req, res) => {
  try {
    const user = await User.findById(req.user.id);
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found.' });
    }
    res.json({ success: true, profile: user });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// Update Profile
router.put('/profile', requireAuth, async (req, res) => {
  try {
    const { username, avatar } = req.body;
    const updated = await User.update(req.user.id, { username, avatar });
    res.json({ success: true, message: 'Profile updated.', profile: updated });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// Upgrade Dex the Companion Pet
router.post('/upgrade-companion', requireAuth, async (req, res) => {
  try {
    const user = await User.findById(req.user.id);
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found.' });
    }

    const currentLvl = user.companion?.level || 1;
    const newLvl = currentLvl + 1;
    const bonuses = {
      2: '+15% Coin Magnet & Dash Booster',
      3: '+25% Super Jump & Island Radar',
      4: '+35% Coin Duplicator & Shield Aura',
      5: '+50% Legendary Aura & Max All Perks'
    };
    const bonus = bonuses[newLvl] || `+${newLvl * 10}% Master Perks`;

    const updated = await User.update(req.user.id, {
      companion: {
        name: 'Dex the Monkey',
        level: newLvl,
        bonus
      }
    });

    res.json({
      success: true,
      message: `Dex the Monkey upgraded to Level ${newLvl}!`,
      companion: updated.companion
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

module.exports = router;
