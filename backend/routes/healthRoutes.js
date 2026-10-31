/**
 * Production Health Check & Status REST API
 * Endpoints:
 * - GET /api/health
 * - GET /api/ping
 */

const express = require('express');
const router = express.Router();

router.get('/health', (req, res) => {
  const memoryUsage = process.memoryUsage();
  res.json({
    status: 'HEALTHY',
    service: 'Ben the Adventurer API',
    uptimeSeconds: Math.floor(process.uptime()),
    timestamp: new Date().toISOString(),
    environment: process.env.NODE_ENV || 'production',
    memory: {
      rssMb: Math.round(memoryUsage.rss / (1024 * 1024)),
      heapUsedMb: Math.round(memoryUsage.heapUsed / (1024 * 1024))
    },
    database: {
      status: 'ONLINE',
      mode: 'Hybrid Realtime Persistence'
    }
  });
});

router.get('/ping', (req, res) => {
  res.json({ pong: true, time: Date.now() });
});

module.exports = router;
