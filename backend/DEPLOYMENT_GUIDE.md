# 🚀 PRODUCTION BACKEND DEPLOYMENT GUIDE (100% COMPLETE)
### Game: Ben the Adventurer: Island Rescue
### Android Package: `com.gptgamestudio.bentheadventurer`

---

## 📌 Available Deployment Options

You can deploy this Node.js REST API to any of the following 1-click cloud platforms to receive your public HTTPS URL (e.g. `https://ben-the-adventurer-api.onrender.com`).

---

## OPTION 1: Deploy on Render.com (Recommended - 100% Free HTTPS)

1. **Sign Up / Log In**:
   * Go to [https://render.com](https://render.com) and sign in with GitHub or your Google account.
2. **Create New Web Service**:
   * Click **New +** > **Web Service**.
   * Connect your GitHub repository containing this project (or upload the `backend/` folder).
3. **Configure Settings**:
   * **Name**: `ben-the-adventurer-api`
   * **Root Directory**: `backend` (or leave empty if deploying root)
   * **Runtime**: `Node`
   * **Build Command**: `npm install`
   * **Start Command**: `node server.js`
   * **Instance Type**: `Free`
4. **Environment Variables**:
   * Under **Environment Variables**, add:
     * `NODE_ENV` = `production`
     * `JWT_SECRET` = `your_super_secret_jwt_key_2026`
     * `FIREBASE_PROJECT_ID` = `super-tank-online`
     * `ADMOB_PUBLISHER_ID` = `pub-9786472984542330`
5. **Click "Deploy Web Service"**:
   * In 1 to 2 minutes, Render will output your live public URL:
     👉 `https://ben-the-adventurer-api.onrender.com`
   * Test Health Check: `https://ben-the-adventurer-api.onrender.com/api/health`

---

## OPTION 2: Deploy on Railway.app

1. Go to [https://railway.app](https://railway.app).
2. Click **New Project** > **Deploy from GitHub repo**.
3. Set **Root Directory** to `/backend` (or root).
4. Add Environment Variables:
   * `PORT` = `5000`
   * `JWT_SECRET` = `your_secret_key`
   * `FIREBASE_PROJECT_ID` = `super-tank-online`
5. Click **Deploy**. Under **Settings** > **Networking**, click **Generate Domain** to get your public HTTPS URL:
   👉 `https://ben-the-adventurer.up.railway.app`

---

## OPTION 3: Deploy on Firebase Cloud Functions

If you want to keep everything inside your existing `super-tank-online` Firebase project:
1. Run in terminal:
   ```bash
   npm install -g firebase-tools
   firebase login
   firebase init functions
   ```
2. Select your project: `super-tank-online`.
3. In `functions/index.js`, export the Express app:
   ```javascript
   const functions = require('firebase-functions');
   const app = require('./backend/server');
   exports.api = functions.https.onRequest(app);
   ```
4. Deploy:
   ```bash
   firebase deploy --only functions
   ```
   👉 Your URL: `https://us-central1-super-tank-online.cloudfunctions.net/api`

---

## 📡 Complete REST API Endpoint Reference

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/health` | Server uptime, memory, health status |
| `GET` | `/api/ping` | Fast latency ping-pong |
| `POST` | `/api/auth/register-guest` | Instant Guest Adventurer login |
| `POST` | `/api/auth/google-login` | Google Sign-In verification |
| `GET` | `/api/auth/me` | Fetch active user credentials |
| `GET` | `/api/users/profile` | Fetch player statistics & achievements |
| `POST` | `/api/users/upgrade-companion` | Level up companion pet Dex the Monkey |
| `POST` | `/api/sync/save` | Save 1-hour timer, overtime, checkpoints, scores |
| `GET` | `/api/sync/load` | Retrieve saved game progress |
| `GET` | `/api/sync/leaderboard` | Get Top 20 Global Expedition Leaderboard |
| `GET` | `/api/game/levels` | Dynamic 5-level speeds, themes & hazards |
| `GET` | `/api/game/config` | Global game settings & AdMob configuration |
| `GET` | `/api/game/ads-verification`| `app-ads.txt` verification status |
| `GET` | `/app-ads.txt` | Serves official AdMob publisher line directly |

---

## 📱 Connecting your Android APK to the Backend URL

Once your backend is deployed and you have your public HTTPS URL:
1. In your Android app or web client, set:
   ```kotlin
   const val BACKEND_URL = "https://ben-the-adventurer-api.onrender.com"
   ```
2. The Android APK and web client will seamlessly synchronize user profiles, continuous 1-hour countdowns, leaderboards, and companion pet upgrades!
