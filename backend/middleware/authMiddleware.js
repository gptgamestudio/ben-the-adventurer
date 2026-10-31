/**
 * Authentication Middleware
 * Supports JWT Tokens and Guest/Anonymous Sessions
 */

const jwt = require('jsonwebtoken');

const JWT_SECRET = process.env.JWT_SECRET || 'super_secret_ben_the_adventurer_jwt_key_2026';

function requireAuth(req, res, next) {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    // If guest ID is passed in headers, allow guest authentication
    const guestId = req.headers['x-guest-id'] || req.headers['x-username'];
    if (guestId) {
      req.user = { id: `guest_${guestId}`, username: guestId, isGuest: true };
      return next();
    }
    return res.status(401).json({
      success: false,
      message: 'Access denied. Missing authorization token or guest identifier.'
    });
  }

  const token = authHeader.split(' ')[1];
  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    req.user = decoded;
    next();
  } catch (err) {
    return res.status(403).json({
      success: false,
      message: 'Invalid or expired authorization token.'
    });
  }
}

function generateToken(payload) {
  return jwt.sign(payload, JWT_SECRET, { expiresIn: '30d' });
}

module.exports = {
  requireAuth,
  generateToken,
  JWT_SECRET
};
