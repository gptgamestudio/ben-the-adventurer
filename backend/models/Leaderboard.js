/**
 * Leaderboard Model Schema
 */

const { cache, persistData } = require('../config/db');

class Leaderboard {
  static async getTop(limit = 20) {
    return [...cache.leaderboard]
      .sort((a, b) => b.score - a.score)
      .slice(0, limit);
  }

  static async submitScore({ userId, username, score, levelReached, totalTimeSeconds }) {
    const existingIndex = cache.leaderboard.findIndex(e => e.userId === userId);
    const entry = {
      userId,
      username,
      score: Number(score),
      levelReached: Number(levelReached),
      totalTimeSeconds: Number(totalTimeSeconds),
      date: new Date().toISOString()
    };

    if (existingIndex >= 0) {
      if (score > cache.leaderboard[existingIndex].score) {
        cache.leaderboard[existingIndex] = entry;
      }
    } else {
      cache.leaderboard.push(entry);
    }

    persistData('leaderboard');
    return entry;
  }
}

module.exports = Leaderboard;
