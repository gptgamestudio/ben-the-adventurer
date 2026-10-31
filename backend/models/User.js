/**
 * User & Adventurer Profile Model Schema with Direct Firestore Auto-Input
 */

const { firestore, cache, persistData } = require('../config/db');

class User {
  constructor({ id, username, email, googleId, avatar = 'default', companionLevel = 1, companionBonus = '+10% Speed' }) {
    this.id = id || `user_${Date.now()}_${Math.random().toString(36).substring(2, 8)}`;
    this.username = username || 'Adventurer Ben';
    this.email = email || '';
    this.googleId = googleId || null;
    this.avatar = avatar;
    this.companion = {
      name: 'Dex the Monkey',
      level: companionLevel,
      bonus: companionBonus
    };
    this.createdAt = Date.now();
    this.updatedAt = Date.now();
  }

  static async findById(id) {
    if (firestore) {
      try {
        const doc = await firestore.collection('users').doc(id).get();
        if (doc.exists) {
          const u = doc.data();
          cache.users.set(id, u);
          return u;
        }
      } catch (err) {
        console.warn(`[FIRESTORE] User lookup error:`, err.message);
      }
    }
    return cache.users.get(id) || null;
  }

  static async findByUsername(username) {
    if (firestore) {
      try {
        const snap = await firestore.collection('users')
          .where('username', '==', username)
          .limit(1)
          .get();
        if (!snap.empty) {
          const u = snap.docs[0].data();
          cache.users.set(u.id, u);
          return u;
        }
      } catch (err) {}
    }
    for (const user of cache.users.values()) {
      if (user.username && user.username.toLowerCase() === username.toLowerCase()) {
        return user;
      }
    }
    return null;
  }

  static async findByGoogleId(googleId) {
    if (firestore) {
      try {
        const snap = await firestore.collection('users')
          .where('googleId', '==', googleId)
          .limit(1)
          .get();
        if (!snap.empty) {
          const u = snap.docs[0].data();
          cache.users.set(u.id, u);
          return u;
        }
      } catch (err) {}
    }
    for (const user of cache.users.values()) {
      if (user.googleId === googleId) {
        return user;
      }
    }
    return null;
  }

  static async create(userData) {
    const newUser = new User(userData);

    // Direct Firestore Auto-Input
    if (firestore) {
      try {
        await firestore.collection('users').doc(newUser.id).set({
          id: newUser.id,
          username: newUser.username,
          email: newUser.email,
          googleId: newUser.googleId,
          avatar: newUser.avatar,
          companion: newUser.companion,
          createdAt: newUser.createdAt,
          updatedAt: newUser.updatedAt
        });

        // Also auto-create game_mates document in Firestore
        await firestore.collection('game_mates').doc(newUser.id).set({
          userId: newUser.id,
          playerName: newUser.username,
          companionName: newUser.companion.name,
          companionLevel: newUser.companion.level,
          companionBonus: newUser.companion.bonus,
          updatedAt: newUser.updatedAt
        });

        console.log(`[FIRESTORE] User & Companion for ${newUser.username} created directly in Firestore!`);
      } catch (err) {
        console.warn(`[FIRESTORE] User save notice:`, err.message);
      }
    }

    cache.users.set(newUser.id, newUser);
    persistData('users');
    return newUser;
  }

  static async update(id, updates) {
    const existing = await User.findById(id) || cache.users.get(id);
    if (!existing) return null;
    const updated = { ...existing, ...updates, updatedAt: Date.now() };

    // Direct Firestore Update
    if (firestore) {
      try {
        await firestore.collection('users').doc(id).set(updated, { merge: true });
        if (updates.companion) {
          await firestore.collection('game_mates').doc(id).set({
            companionLevel: updates.companion.level,
            companionBonus: updates.companion.bonus,
            updatedAt: Date.now()
          }, { merge: true });
        }
      } catch (err) {
        console.warn(`[FIRESTORE] Update error:`, err.message);
      }
    }

    cache.users.set(id, updated);
    persistData('users');
    return updated;
  }
}

module.exports = User;
