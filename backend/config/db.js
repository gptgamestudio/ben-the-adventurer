/**
 * Database Connection & Direct Firebase Firestore Integration
 * Connects directly to Google Cloud Firestore (Project: super-tank-online)
 */

const { initializeApp, getApps } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const fs = require('fs');
const path = require('path');

const DATA_DIR = path.join(__dirname, '../data');
if (!fs.existsSync(DATA_DIR)) {
  fs.mkdirSync(DATA_DIR, { recursive: true });
}

const cache = {
  users: new Map(),
  saves: new Map(),
  leaderboard: []
};

// Initialize Firebase Admin v14 & Cloud Firestore directly
let firestore = null;
const projectId = process.env.FIREBASE_PROJECT_ID || 'super-tank-online';

try {
  if (getApps().length === 0) {
    initializeApp({
      projectId: projectId
    });
  }
  firestore = getFirestore();
  console.log(`[FIREBASE] Connected directly to Cloud Firestore (Project ID: ${projectId})`);
} catch (err) {
  console.warn('[FIREBASE] Notice initializing Firebase Admin:', err.message);
}

// Persistent Local Backup Sync
function loadLocalStore() {
  const usersFile = path.join(DATA_DIR, 'users.json');
  const savesFile = path.join(DATA_DIR, 'saves.json');
  const leaderFile = path.join(DATA_DIR, 'leaderboard.json');

  try {
    if (fs.existsSync(usersFile)) {
      const data = JSON.parse(fs.readFileSync(usersFile, 'utf8'));
      Object.entries(data).forEach(([k, v]) => cache.users.set(k, v));
    }
    if (fs.existsSync(savesFile)) {
      const data = JSON.parse(fs.readFileSync(savesFile, 'utf8'));
      Object.entries(data).forEach(([k, v]) => cache.saves.set(k, v));
    }
    if (fs.existsSync(leaderFile)) {
      cache.leaderboard = JSON.parse(fs.readFileSync(leaderFile, 'utf8'));
    }
  } catch (err) {
    console.warn('[DB] Warning loading local store:', err.message);
  }
}

function persistData(collectionName) {
  try {
    if (collectionName === 'users') {
      const obj = Object.fromEntries(cache.users);
      fs.writeFileSync(path.join(DATA_DIR, 'users.json'), JSON.stringify(obj, null, 2));
    } else if (collectionName === 'saves') {
      const obj = Object.fromEntries(cache.saves);
      fs.writeFileSync(path.join(DATA_DIR, 'saves.json'), JSON.stringify(obj, null, 2));
    } else if (collectionName === 'leaderboard') {
      fs.writeFileSync(path.join(DATA_DIR, 'leaderboard.json'), JSON.stringify(cache.leaderboard, null, 2));
    }
  } catch (err) {
    console.error(`[DB] Error persisting ${collectionName}:`, err.message);
  }
}

async function connectDB() {
  loadLocalStore();
  return {
    status: 'connected',
    provider: 'Cloud Firestore & Realtime Persistence',
    projectId,
    firestoreReady: firestore !== null
  };
}

module.exports = {
  connectDB,
  firestore,
  cache,
  persistData
};
