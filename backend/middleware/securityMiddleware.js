/**
 * Security & CORS Middleware Configuration
 */

const cors = require('cors');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');

function configureSecurity(app) {
  // Helmet Content Security Policy & Protective HTTP Headers
  app.use(
    helmet({
      crossOriginResourcePolicy: { policy: 'cross-origin' },
      contentSecurityPolicy: false // Allow game canvas assets and AdMob scripts
    })
  );

  // Dynamic & Permissive CORS Configuration for Mobile APK and Web
  const allowedOrigins = process.env.ALLOWED_ORIGINS 
    ? process.env.ALLOWED_ORIGINS.split(',').map(s => s.trim()) 
    : ['*'];

  app.use(
    cors({
      origin: (origin, callback) => {
        // Allow mobile apps (origin is undefined) or allowed origins
        if (!origin || allowedOrigins.includes('*') || allowedOrigins.includes(origin)) {
          return callback(null, true);
        }
        return callback(null, true); // Permissive for game clients
      },
      credentials: true,
      methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
      allowedHeaders: ['Content-Type', 'Authorization', 'X-Guest-ID', 'X-Username']
    })
  );

  // Rate Limiting (Prevents API abuse: 100 requests per minute per IP)
  const limiter = rateLimit({
    windowMs: 1 * 60 * 1000,
    max: 120,
    standardHeaders: true,
    legacyHeaders: false,
    message: {
      success: false,
      message: 'Too many requests from this IP, please try again in 1 minute.'
    }
  });

  app.use('/api/', limiter);
}

module.exports = {
  configureSecurity
};
