/**
 * GameSave Model Schema with Direct Firestore Auto-Input
 * Automatically synchronizes 1-hour timer, overtime, checkpoints, scores directly to Firestore!
 */

const { firestore, cache, persistData } = require('../config/db');

class GameSave {
  constructor({
    userId,
    username,
    levelIndex = 0,
    timerSeconds = 3600,
    totalPlayedSeconds = 0,
    overtimeSeconds = 0,
    score = 0,
    coinsCount = 0,
    deathCount = 0,
    checkpointX = 80,
    checkpointY = 300,
    isVictory = false
  }) {
    this.userId = userId;
    this.username = username;
    this.levelIndex = Number(levelIndex);
    this.timerSeconds = Number(timerSeconds);
    this.totalPlayedSeconds = Number(totalPlayedSeconds);
    this.overtimeSeconds = Number(overtimeSeconds);
    this.score = Number(score);
    this.coinsCount = Number(coinsCount);
    this.deathCount = Number(deathCount);
    this.checkpoint = { x: Number(checkpointX), y: Number(checkpointY) };
    this.isVictory = Boolean(isVictory);
    this.updatedAt = Date.now();
  }

  static async findByUserId(userId) {
    // 1. Try Firestore direct read
    if (firestore) {
      try {
        const doc = await firestore.collection('game_saves').doc(userId).get();
        if (doc.exists) {
          const data = doc.data();
          cache.saves.set(userId, data);
          return data;
        }
      } catch (err) {
        console.warn(`[FIRESTORE] Notice reading save for ${userId}:`, err.message);
      }
    }
    // 2. Fallback to cache/local store
    return cache.saves.get(userId) || null;
  }

  static async upsert(saveData) {
    const existing = cache.saves.get(saveData.userId);
    const save = new GameSave({
      ...existing,
      ...saveData
    });

    // 1. Direct Auto-Input to Cloud Firestore
    if (firestore) {
      try {
        await firestore.collection('game_saves').doc(saveData.userId).set({
          userId: save.userId,
          username: save.username,
          levelIndex: save.levelIndex,
          timerSeconds: save.timerSeconds,
          totalPlayedSeconds: save.totalPlayedSeconds,
          overtimeSeconds: save.overtimeSeconds,
          score: save.score,
          coinsCount: save.coinsCount,
          deathCount: save.deathCount,
          checkpoint: save.checkpoint,
          isVictory: save.isVictory,
          updatedAt: save.updatedAt
        }, { merge: true });

        // Also auto-update adventurer profile stats
        await firestore.collection('adventurer_profiles').doc(saveData.userId).set({
          userId: save.userId,
          playerName: save.username,
          totalScore: save.score,
          coins: save.coinsCount,
          currentWorld: `WORLD_${save.levelIndex + 1}`,
          totalPlayTimeSeconds: save.totalPlayedSeconds,
          isOneHourMilestoneAchieved: save.totalPlayedSeconds >= 3600,
          updatedAt: save.updatedAt
        }, { merge: true });

        console.log(`[FIRESTORE] Auto-saved progress for ${save.username} to Cloud Firestore!`);
      } catch (err) {
        console.warn(`[FIRESTORE] Offline fallback saving for ${save.username}:`, err.message);
      }
    }

    // 2. Cache & persistent backup
    cache.saves.set(saveData.userId, save);
    persistData('saves');
    return save;
  }

  static async delete(userId) {
    if (firestore) {
      try {
        await firestore.collection('game_saves').doc(userId).delete();
      } catch (err) {
        console.warn(`[FIRESTORE] Delete error:`, err.message);
      }
    }
    const deleted = cache.saves.delete(userId);
    persistData('saves');
    return deleted;
  }
}

module.exports = GameSave;
