/**
 * Game Save & Sync REST API Routes
 * Endpoints:
 * - POST /api/sync/save
 * - GET  /api/sync/load
 * - POST /api/sync/leaderboard/submit
 * - GET  /api/sync/leaderboard
 */

const express = require('express');
const router = express.Router();
const GameSave = require('../models/GameSave');
const Leaderboard = require('../models/Leaderboard');
const { requireAuth } = require('../middleware/authMiddleware');

// Save or Update Game Progress (1-Hour Timer, Overtime, Checkpoints, Score)
router.post('/save', requireAuth, async (req, res) => {
  try {
    const {
      levelIndex,
      timerSeconds,
      totalPlayedSeconds,
      overtimeSeconds,
      score,
      coinsCount,
      deathCount,
      checkpointX,
      checkpointY,
      isVictory
    } = req.body;

    const save = await GameSave.upsert({
      userId: req.user.id,
      username: req.user.username,
      levelIndex,
      timerSeconds,
      totalPlayedSeconds,
      overtimeSeconds,
      score,
      coinsCount,
      deathCount,
      checkpointX,
      checkpointY,
      isVictory
    });

    // If final boss is cleared or score updated, record on leaderboard
    if (score > 0) {
      await Leaderboard.submitScore({
        userId: req.user.id,
        username: req.user.username,
        score,
        levelReached: (Number(levelIndex) || 0) + 1,
        totalTimeSeconds: totalPlayedSeconds || 0
      });
    }

    res.json({
      success: true,
      message: 'Game progress and 1-hour expedition timer synchronized.',
      save
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// Load Current User's Saved Game
router.get('/load', requireAuth, async (req, res) => {
  try {
    const save = await GameSave.findByUserId(req.user.id);
    if (!save) {
      return res.json({
        success: true,
        hasSave: false,
        message: 'No previous save found. Ready for fresh journey.'
      });
    }
    res.json({
      success: true,
      hasSave: true,
      save
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// Submit Score to Global Leaderboard
router.post('/leaderboard/submit', requireAuth, async (req, res) => {
  try {
    const { score, levelReached, totalTimeSeconds } = req.body;
    const entry = await Leaderboard.submitScore({
      userId: req.user.id,
      username: req.user.username,
      score,
      levelReached,
      totalTimeSeconds
    });
    res.json({ success: true, entry });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// Get Top Leaderboards
router.get('/leaderboard', async (req, res) => {
  try {
    const top = await Leaderboard.getTop(20);
    res.json({ success: true, leaderboard: top });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

module.exports = router;
