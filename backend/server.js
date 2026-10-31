/**
 * ==============================================================================
 * BEN THE ADVENTURER - PRODUCTION REST API SERVER ENTRY POINT
 * ==============================================================================
 * Production server for Android Mobile APK & Web Client synchronization.
 */

require('dotenv').config();
const express = require('express');
const path = require('path');
const { connectDB } = require('./config/db');
const { configureSecurity } = require('./middleware/securityMiddleware');

const authRoutes = require('./routes/authRoutes');
const userRoutes = require('./routes/userRoutes');
const syncRoutes = require('./routes/syncRoutes');
const gameRoutes = require('./routes/gameRoutes');
const healthRoutes = require('./routes/healthRoutes');

const app = express();
const PORT = process.env.PORT || 5000;

// Body Parsers
app.use(express.json({ limit: '5mb' }));
app.use(express.urlencoded({ extended: true, limit: '5mb' }));

// Security, Helmet & CORS Configuration
configureSecurity(app);

// Request Logger in Non-Production
if (process.env.NODE_ENV !== 'production') {
  app.use((req, res, next) => {
    console.log(`[HTTP] ${req.method} ${req.url}`);
    next();
  });
}

// REST API Route Mounts
app.use('/api', healthRoutes);
app.use('/api/auth', authRoutes);
app.use('/api/users', userRoutes);
app.use('/api/sync', syncRoutes);
app.use('/api/game', gameRoutes);

// Serve app-ads.txt directly at root
app.get('/app-ads.txt', (req, res) => {
  const line = process.env.APP_ADS_TXT_ENTRY || 'google.com, pub-9786472984542330, DIRECT, f08c47fec0942fa0';
  res.type('text/plain').send(line + '\n');
});

// Serve Public Static Assets (if hosted together)
const publicPath = path.join(__dirname, '../public');
if (require('fs').existsSync(publicPath)) {
  app.use(express.static(publicPath));
}

// Server Root API Welcome & Status
app.get('/', (req, res) => {
  res.json({
    service: 'Ben the Adventurer: Island Rescue API',
    version: '1.0.0',
    status: 'ONLINE',
    documentation: {
      health: 'GET /api/health',
      auth: 'POST /api/auth/register-guest, POST /api/auth/google-login, GET /api/auth/me',
      users: 'GET /api/users/profile, PUT /api/users/profile, POST /api/users/upgrade-companion',
      sync: 'POST /api/sync/save, GET /api/sync/load, GET /api/sync/leaderboard',
      game: 'GET /api/game/levels, GET /api/game/config, GET /api/game/ads-verification',
      adsTxt: 'GET /app-ads.txt'
    }
  });
});

// 404 Route Not Found Handler
app.use((req, res) => {
  res.status(404).json({
    success: false,
    message: `Endpoint ${req.method} ${req.originalUrl} not found.`
  });
});

// Global Error Handler
app.use((err, req, res, next) => {
  console.error('[SERVER ERROR]', err);
  res.status(err.status || 500).json({
    success: false,
    message: err.message || 'Internal Server Error'
  });
});

// Server Initialization
let server;
connectDB().then(() => {
  server = app.listen(PORT, '0.0.0.0', () => {
    console.log(`=======================================================`);
    console.log(`🚀 Ben the Adventurer Production Server is LIVE!`);
    console.log(`📡 URL: http://0.0.0.0:${PORT}`);
    console.log(`🩺 Health: http://localhost:${PORT}/api/health`);
    console.log(`=======================================================`);
  });
}).catch(err => {
  console.error('[DB FATAL] Failed to initialize database:', err);
  process.exit(1);
});

// Graceful Shutdown
function handleShutdown(signal) {
  console.log(`[SHUTDOWN] Received ${signal}. Closing server gracefully...`);
  if (server) {
    server.close(() => {
      console.log('[SHUTDOWN] HTTP server closed cleanly.');
      process.exit(0);
    });
  } else {
    process.exit(0);
  }
}
process.on('SIGTERM', () => handleShutdown('SIGTERM'));
process.on('SIGINT', () => handleShutdown('SIGINT'));

module.exports = app;
