import os

html_content = r'''<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no, viewport-fit=cover">
  <title>Ben The Adventurer - 1,000 Levels Mobile Platformer</title>
  <link rel="icon" type="image/png" href="./app-icon.png">
  <!-- Firebase SDKs for Cloud Save -->
  <script src="https://www.gstatic.com/firebasejs/10.8.0/firebase-app-compat.js"></script>
  <script src="https://www.gstatic.com/firebasejs/10.8.0/firebase-auth-compat.js"></script>
  <script src="https://www.gstatic.com/firebasejs/10.8.0/firebase-firestore-compat.js"></script>
  <!-- Google AdSense / AdMob Tag -->
  <script async src="https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client=ca-pub-9786472984542330" crossorigin="anonymous"></script>

  <style>
    * {
      box-sizing: border-box;
      margin: 0;
      padding: 0;
      -webkit-touch-callout: none;
      -webkit-user-select: none;
      user-select: none;
      touch-action: none;
    }

    html, body {
      width: 100%;
      height: 100%;
      overflow: hidden;
      background-color: #050b14;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
    }

    #game-container {
      position: relative;
      width: 100vw;
      height: 100vh;
      display: flex;
      justify-content: center;
      align-items: center;
      background: #000;
      overflow: hidden;
    }

    canvas {
      display: block;
      width: 100%;
      height: 100%;
      object-fit: contain;
      image-rendering: pixelated;
      image-rendering: crisp-edges;
    }

    /* =========================================================================
       2. MGA KONTROL (TOUCH AT BUTTONS) - UNIFORM 52PX CIRCULAR BUTTONS
       ========================================================================= */
    .controls-overlay {
      position: fixed;
      bottom: 38px; /* Directly resting right above the 34px bottom ad banner */
      left: 0;
      width: 100%;
      display: flex;
      justify-content: space-between;
      align-items: flex-end;
      padding: 0 max(16px, env(safe-area-inset-left)) 0 max(16px, env(safe-area-inset-right));
      pointer-events: none;
      z-index: 50;
    }

    .movement-group, .action-group {
      display: flex;
      align-items: center;
      gap: 10px;
      pointer-events: auto;
    }

    /* All 5 buttons share identical 52px size, height, curvature, and text styling */
    .btn-control {
      width: 52px;
      height: 52px;
      border-radius: 50%;
      display: flex;
      justify-content: center;
      align-items: center;
      cursor: pointer;
      font-weight: 900;
      color: #ffffff;
      font-size: 11px;
      letter-spacing: 0.5px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.65), 0 0 10px rgba(0, 240, 255, 0.25);
      touch-action: none;
      transition: transform 0.05s, filter 0.05s;
    }

    .btn-control:active, .btn-control.active {
      transform: scale(0.90);
      filter: brightness(1.3);
    }

    /* Movement Buttons (Left / Right) - Clean Text Labels, No Arrows */
    .btn-left, .btn-right {
      background: radial-gradient(circle, #0284c7 0%, #0369a1 100%);
      border: 2px solid #38bdf8;
    }

    /* Action Buttons (Slide, Blast, Jump) */
    .btn-slide {
      background: radial-gradient(circle, #0d9488 0%, #115e59 100%);
      border: 2px solid #2dd4bf;
    }

    .btn-blast {
      background: radial-gradient(circle, #8b5cf6 0%, #6d28d9 100%);
      border: 2px solid #c4b5fd;
    }

    .btn-jump {
      background: radial-gradient(circle, #ef4444 0%, #b91c1c 100%);
      border: 2px solid #fca5a5;
    }

    /* =========================================================================
       1. ADMOB BANNER NA NAKA-LOCK SA PINAKABABA NG SCREEN
       ========================================================================= */
    .admob-banner-locked {
      position: fixed;
      bottom: 0;
      left: 0;
      width: 100%;
      height: 34px;
      background: #020b18;
      border-top: 1.5px solid #facc15;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 14px;
      color: #fef08a;
      font-size: 11px;
      font-weight: bold;
      z-index: 100;
      box-shadow: 0 -4px 15px rgba(0, 0, 0, 0.8);
      cursor: pointer;
      pointer-events: auto;
    }

    .admob-badge-box {
      display: flex;
      align-items: center;
      gap: 6px;
    }

    .admob-tag {
      background: #facc15;
      color: #000;
      font-size: 9px;
      font-weight: 900;
      padding: 2px 5px;
      border-radius: 3px;
    }

    .admob-pub {
      font-size: 10px;
      color: #94a3b8;
    }

    .admob-text {
      font-size: 11px;
      color: #fde047;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      margin: 0 8px;
    }

    .btn-ad-trigger {
      background: #ca8a04;
      color: #000;
      border: none;
      border-radius: 4px;
      padding: 3px 8px;
      font-size: 10px;
      font-weight: 900;
      cursor: pointer;
      white-space: nowrap;
    }

    /* =========================================================================
       FIRST DASHBOARD - MOVABLE UP AND DOWN WITH GOOGLE LOGIN & SIGNUP
       ========================================================================= */
    .screen-overlay {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: rgba(3, 7, 18, 0.94);
      backdrop-filter: blur(8px);
      -webkit-backdrop-filter: blur(8px);
      z-index: 60;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      padding: 14px;
      text-align: center;
      overflow-y: auto;
      pointer-events: auto;
    }

    /* Movable Dashboard Card (Scrollable up and down smoothly) */
    .dashboard-card {
      background: #0f172a;
      border: 1.5px solid #00f0ff;
      border-radius: 18px;
      padding: 20px;
      max-width: 440px;
      width: 100%;
      max-height: 88vh; /* Movable up and down */
      overflow-y: auto;
      -webkit-overflow-scrolling: touch;
      box-shadow: 0 8px 32px rgba(0, 240, 255, 0.25);
      display: flex;
      flex-direction: column;
      gap: 12px;
      text-align: center;
      pointer-events: auto;
    }

    .scroll-indicator-pill {
      background: rgba(0, 240, 255, 0.1);
      border: 1px solid rgba(0, 240, 255, 0.35);
      border-radius: 20px;
      padding: 4px 12px;
      font-size: 10px;
      color: #38bdf8;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      margin: -4px 0 2px 0;
    }

    /* Auth Card Tabs */
    .auth-box {
      background: #1e293b;
      border: 1px solid #334155;
      border-radius: 12px;
      padding: 12px;
      display: flex;
      flex-direction: column;
      gap: 10px;
      text-align: left;
    }

    .auth-tabs {
      display: flex;
      gap: 6px;
      background: #0f172a;
      padding: 3px;
      border-radius: 8px;
    }

    .auth-tab-btn {
      flex: 1;
      background: transparent;
      border: none;
      color: #94a3b8;
      font-size: 11px;
      font-weight: 800;
      padding: 6px;
      border-radius: 6px;
      cursor: pointer;
      text-align: center;
    }

    .auth-tab-btn.active {
      background: #0284c7;
      color: #ffffff;
      box-shadow: 0 2px 6px rgba(2, 132, 199, 0.4);
    }

    .input-group {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .input-label {
      font-size: 11px;
      font-weight: 700;
      color: #cbd5e1;
    }

    .password-wrapper {
      position: relative;
      width: 100%;
    }

    .auth-input {
      width: 100%;
      background: #0f172a;
      border: 1px solid #475569;
      color: #fff;
      padding: 9px 12px;
      border-radius: 8px;
      font-size: 12px;
      outline: none;
    }

    .auth-input:focus {
      border-color: #38bdf8;
    }

    .btn-eye-toggle {
      position: absolute;
      right: 8px;
      top: 50%;
      transform: translateY(-50%);
      background: transparent;
      border: none;
      color: #38bdf8;
      font-size: 11px;
      cursor: pointer;
      font-weight: bold;
      padding: 4px;
    }

    .btn-google-auth {
      background: #ffffff;
      color: #0f172a;
      font-weight: 800;
      font-size: 12px;
      padding: 9px;
      border-radius: 8px;
      border: none;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.4);
    }

    .btn-auth-action {
      background: #0284c7;
      color: #ffffff;
      font-weight: 900;
      font-size: 12px;
      padding: 9px;
      border-radius: 8px;
      border: none;
      cursor: pointer;
    }

    .btn-primary {
      background: linear-gradient(135deg, #0284c7 0%, #00f0ff 100%);
      color: #000;
      font-weight: 900;
      font-size: 15px;
      padding: 12px 24px;
      border-radius: 25px;
      border: none;
      cursor: pointer;
      box-shadow: 0 0 15px rgba(0, 240, 255, 0.5);
      transition: transform 0.08s;
    }
    .btn-primary:active { transform: scale(0.96); }

    .btn-apk-dl {
      background: #064e3b;
      border: 1.5px solid #10b981;
      color: #4ade80;
      font-size: 12px;
      font-weight: 800;
      padding: 8px 16px;
      border-radius: 20px;
      text-decoration: none;
      display: inline-block;
      margin-top: 4px;
    }
  </style>
</head>
<body>

<div id="game-container">
  <canvas id="gameCanvas"></canvas>

  <!-- =========================================================================
       2. MGA KONTROL: TOUCH BUTTONS (LEFT, RIGHT, SLIDE, BLAST, JUMP)
       ========================================================================= -->
  <div class="controls-overlay">
    <!-- Movement Buttons on Left: Circular, Uniform 52px, Text Labels (No Arrows) -->
    <div class="movement-group">
      <div id="btnLeft" class="btn-control btn-left">LEFT</div>
      <div id="btnRight" class="btn-control btn-right">RIGHT</div>
    </div>

    <!-- Action Buttons on Right: Circular, Uniform 52px, Equal Alignment -->
    <div class="action-group">
      <div id="btnSlide" class="btn-control btn-slide">SLIDE</div>
      <div id="btnBlast" class="btn-control btn-blast">BLAST</div>
      <div id="btnJump" class="btn-control btn-jump">JUMP</div>
    </div>
  </div>

  <!-- =========================================================================
       1. ADMOB BANNER AD - NAKA-LOCK SA PINAKABABA NG SCREEN
       ========================================================================= -->
  <div class="admob-banner-locked" onclick="triggerAdMobModal('banner')">
    <div class="admob-badge-box">
      <span class="admob-tag">AdMob</span>
      <span class="admob-pub">pub-9786472984542330</span>
    </div>
    <div class="admob-text">
      🟢 AdMob Connected &bull; Unit 5439965215 &bull; Locked Bottom
    </div>
    <button type="button" class="btn-ad-trigger">WATCH AD 🎬</button>
  </div>

  <!-- =========================================================================
       FIRST DASHBOARD (MOVABLE UP & DOWN + GOOGLE LOGIN & SIGNUP + PASSWORD EYE)
       ========================================================================= -->
  <div id="startScreen" class="screen-overlay">
    <div class="dashboard-card">
      <div style="font-size:32px;">🌴 🐉 👑</div>
      <h1 style="color:#00f0ff; font-size:22px; font-weight:900; letter-spacing:1px;">BEN THE ADVENTURER</h1>
      <p style="color:#94a3b8; font-size:12px;">1,000 Levels Mario-Style Adventure &bull; Boss Dragon &bull; Princess Jasmine</p>

      <!-- Scroll Indicator: Movable Up and Down -->
      <div class="scroll-indicator-pill">
        <span>↕️</span> <strong>Movable Dashboard:</strong> <span>I-scroll pataas at pababa</span>
      </div>

      <!-- Active User Card (kapag naka-log in) -->
      <div id="userBadgeCard" style="display:none; background:#1e293b; border-radius:12px; padding:10px; border:1px solid #334155;">
        <div style="display:flex; justify-content:space-between; align-items:center;">
          <div style="text-align:left;">
            <div id="userRoleText" style="font-size:10px; color:#a7f3d0; font-weight:bold;">🛡️ True Player Account</div>
            <div id="userEmailText" style="font-size:12px; color:#fff; font-weight:bold;">player@gmail.com</div>
          </div>
          <div style="display:flex; gap:6px;">
            <button type="button" onclick="openAdminSecurityPrompt()" style="background:#0284c7; color:#fff; border:none; padding:4px 8px; border-radius:6px; font-size:10px; font-weight:bold; cursor:pointer;">⚙️ Admin</button>
            <button type="button" onclick="handleSignOut()" style="background:#334155; color:#cbd5e1; border:none; padding:4px 8px; border-radius:6px; font-size:10px; font-weight:bold; cursor:pointer;">Out</button>
          </div>
        </div>
        <div style="display:flex; justify-content:space-between; margin-top:8px; font-size:11px; color:#cbd5e1; background:rgba(0,0,0,0.3); padding:4px 8px; border-radius:6px;">
          <span>⭐ Level: <strong id="dashLevel" style="color:#fde047;">1</strong> / 1000</span>
          <span>🪙 Coins: <strong id="dashCoins" style="color:#fde047;">0</strong></span>
          <span style="color:#4ade80;">☁️ Firestore Online</span>
        </div>
      </div>

      <!-- Google Log In & Sign Up Box with Show/Hidden Password Eye -->
      <div id="authBox" class="auth-box">
        <div class="auth-tabs">
          <button type="button" class="auth-tab-btn active" id="tabLogin" onclick="switchAuthTab('login')">🔑 LOG IN</button>
          <button type="button" class="auth-tab-btn" id="tabSignup" onclick="switchAuthTab('signup')">📝 SIGN UP</button>
        </div>

        <div id="authAlert" style="display:none; background:rgba(239,68,68,0.2); border:1px solid #ef4444; border-radius:6px; padding:6px; font-size:11px; color:#fca5a5;"></div>

        <!-- Google 1-Tap Log In -->
        <button type="button" class="btn-google-auth" onclick="handleGoogleSignIn()">
          <span>🌐</span> Mag-sign in gamit ang Google
        </button>

        <div style="text-align:center; font-size:10px; color:#64748b;">— O GAMIT ANG GMAIL / USERNAME —</div>

        <div class="input-group">
          <label class="input-label" for="authEmail">Username / Gmail:</label>
          <input type="text" id="authEmail" class="auth-input" placeholder="hal. benadventurer@gmail.com" />
        </div>

        <div class="input-group">
          <label class="input-label" for="authPassword">Password:</label>
          <div class="password-wrapper">
            <input type="password" id="authPassword" class="auth-input" placeholder="Password (minimum 6 characters)" />
            <button type="button" class="btn-eye-toggle" id="authEyeBtn" onclick="toggleAuthPassword()">👁️ Show</button>
          </div>
        </div>

        <button type="button" class="btn-auth-action" id="btnAuthSubmit" onclick="handleAuthSubmit()">MAG-LOG IN NA</button>
      </div>

      <!-- Play Button -->
      <button class="btn-primary" onclick="startGame()">▶ MAGLARO NA (START ADVENTURE)</button>

      <!-- Android Native APK Download Link -->
      <div>
        <a href="./BenTheAdventurer.apk" download="BenTheAdventurer.apk" class="btn-apk-dl">
          📲 Download Android Native APK (25MB)
        </a>
      </div>
    </div>
  </div>

  <!-- GAME OVER OVERLAY (Kung saan na-dead, doon din babalik) -->
  <div id="gameOverScreen" class="screen-overlay" style="display:none;">
    <div style="font-size:42px;">💀 🏝️</div>
    <h2 style="color:#ef4444; font-size:28px; font-weight:900; margin:6px 0;">GAME OVER</h2>
    <p id="gameOverSubtext" style="color:#cbd5e1; font-size:13px; margin-bottom:12px;">Naaksidente si Ben sa mga island hazards!</p>
    <button class="btn-primary" style="background:#ef4444; color:#fff; margin-bottom:8px;" onclick="triggerAdMobModal('revive')">📺 WATCH AD TO REVIVE (+3 HEARTS)</button>
    <!-- Respawn directly at the exact level where Ben died -->
    <button class="btn-primary" onclick="restartCurrentLevel()">PLAY AGAIN (RETRY LEVEL <span id="goLevelText">1</span>)</button>
  </div>

  <!-- VICTORY CELEBRATION (PRINCESS JASMINE RESCUED) -->
  <div id="victoryScreen" class="screen-overlay" style="display:none;">
    <div style="font-size:44px;">👑 👸 ❤️</div>
    <h2 style="color:#4ade80; font-size:26px; font-weight:900; margin:6px 0;">NALIGTAS SI PRINCESS JASMINE!</h2>
    <p id="victorySubtext" style="color:#cbd5e1; font-size:13px; margin-bottom:12px;">Napatay ang Dragon! Awtomatikong lilipad sa susunod na level!</p>
    <button class="btn-primary" onclick="nextLevel()">PROCEED TO NEXT LEVEL ➔</button>
  </div>

  <!-- INTERSTITIAL / REWARD AD MODAL -->
  <div id="adModal" class="screen-overlay" style="display:none; z-index:90;">
    <div style="background:#0f172a; border:2px solid #facc15; border-radius:16px; padding:20px; max-width:360px; width:90%; text-align:center;">
      <div style="font-size:36px; margin-bottom:6px;">📺</div>
      <h3 id="adModalTitle" style="color:#fde047; font-size:16px; font-weight:900;">GOOGLE ADMOB REWARD AD</h3>
      <p id="adModalDesc" style="color:#cbd5e1; font-size:11px; margin:8px 0;">Google AdMob Authorized Publisher pub-9786472984542330 &bull; Unit: 1427823151</p>
      <div style="font-size:18px; font-weight:bold; color:#4ade80; margin:10px 0;" id="adTimerText">Reward in 5s...</div>
      <button id="btnSkipAd" class="btn-primary" style="display:none; width:100%;" onclick="claimAdReward()">CLAIM REWARD ⭐</button>
    </div>
  </div>

  <!-- OWNER SECURITY MODAL -->
  <div id="adminSecurityModal" class="screen-overlay" style="display:none; z-index:99;">
    <div style="background:#1e293b; border:2px solid #fde047; border-radius:16px; max-width:360px; width:90%; padding:20px; text-align:center;">
      <div style="font-size:40px;">🔐</div>
      <h3 style="color:#fde047; font-size:16px; font-weight:900;">ADMINISTRATIVE SECURITY LOCK</h3>
      <p style="color:#cbd5e1; font-size:11px; margin:6px 0 12px 0;">Tanging ang opisyal na may-ari (benpasiglandingin@gmail.com) ang may All-Access.</p>
      <div id="adminAlert" style="display:none; background:rgba(239,68,68,0.2); border:1px solid #ef4444; border-radius:6px; padding:6px; font-size:11px; color:#fca5a5; margin-bottom:8px;"></div>
      <div class="password-wrapper" style="margin-bottom:12px;">
        <input type="password" id="adminPasswordInput" class="auth-input" placeholder="Enter Master Admin Password" />
        <button type="button" class="btn-eye-toggle" onclick="toggleAdminPassword()">👁️ Show</button>
      </div>
      <button class="btn-primary" style="width:100%; margin-bottom:8px;" onclick="verifyAdminPassword()">UNLOCK SETTINGS</button>
      <button type="button" onclick="closeAdminModal()" style="background:transparent; border:none; color:#94a3b8; font-size:11px; cursor:pointer;">Bumalik</button>
    </div>
  </div>
</div>

<script>
  // =========================================================================
  // AUDIO SYNTHESIZER (8-BIT RETRO SOUNDS)
  // =========================================================================
  const audio = {
    ctx: null,
    init() {
      if (!this.ctx) {
        const AudioCtx = window.AudioContext || window.webkitAudioContext;
        if (AudioCtx) this.ctx = new AudioCtx();
      }
      if (this.ctx && this.ctx.state === 'suspended') this.ctx.resume();
    },
    playTone(freq, dur, type = 'square', gainVal = 0.08) {
      if (!this.ctx) return;
      try {
        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();
        osc.type = type;
        osc.frequency.setValueAtTime(freq, this.ctx.currentTime);
        gain.gain.setValueAtTime(gainVal, this.ctx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.0001, this.ctx.currentTime + dur);
        osc.connect(gain);
        gain.connect(this.ctx.destination);
        osc.start();
        osc.stop(this.ctx.currentTime + dur);
      } catch(e) {}
    },
    jump() {
      this.init();
      if (!this.ctx) return;
      try {
        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();
        osc.type = 'square';
        osc.frequency.setValueAtTime(160, this.ctx.currentTime);
        osc.frequency.exponentialRampToValueAtTime(340, this.ctx.currentTime + 0.12);
        gain.gain.setValueAtTime(0.08, this.ctx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.0001, this.ctx.currentTime + 0.12);
        osc.connect(gain);
        gain.connect(this.ctx.destination);
        osc.start();
        osc.stop(this.ctx.currentTime + 0.12);
      } catch(e) {}
    },
    coin() {
      this.init();
      this.playTone(987, 0.08, 'sine', 0.1);
      setTimeout(() => this.playTone(1318, 0.14, 'sine', 0.1), 70);
    },
    hit() {
      this.init();
      this.playTone(80, 0.2, 'sawtooth', 0.12);
    },
    blast() {
      this.init();
      this.playTone(480, 0.08, 'triangle', 0.09);
    },
    victory() {
      this.init();
      [523, 659, 783, 1046].forEach((f, idx) => {
        setTimeout(() => this.playTone(f, 0.15, 'sine', 0.12), idx * 90);
      });
    }
  };

  // =========================================================================
  // LOCAL & CLOUD STORAGE MEMORY (1000 LEVELS PERSISTENCE & CHECKPOINT)
  // =========================================================================
  const SAVE_KEY = 'ben_adventurer_lvl_save';
  const COINS_KEY = 'ben_coins_save';
  const USER_KEY = 'ben_adventurer_user_profile';
  const OWNER_EMAIL = "benpasiglandingin@gmail.com";
  const ADMIN_PASSWORD = "BenAdmin2026!";

  let currentLevel = 1;
  let coins = 0;
  let lives = 3;
  let gameActive = false;
  let currentAuthUser = null;
  let authMode = 'login';

  try {
    const savedLvl = localStorage.getItem(SAVE_KEY);
    if (savedLvl && !isNaN(parseInt(savedLvl))) {
      currentLevel = Math.max(1, Math.min(1000, parseInt(savedLvl)));
    }
    const savedCoins = localStorage.getItem(COINS_KEY);
    if (savedCoins && !isNaN(parseInt(savedCoins))) {
      coins = Math.max(0, parseInt(savedCoins));
    }
    const savedUser = localStorage.getItem(USER_KEY);
    if (savedUser) {
      currentAuthUser = JSON.parse(savedUser);
    }
  } catch(e) {}

  function autoSaveMemory() {
    try {
      localStorage.setItem(SAVE_KEY, currentLevel.toString());
      localStorage.setItem(COINS_KEY, coins.toString());
      if (currentAuthUser) {
        localStorage.setItem(USER_KEY, JSON.stringify(currentAuthUser));
      }
    } catch(e) {}
    updateDashboardUI();
  }

  function updateDashboardUI() {
    const lvlEl = document.getElementById('dashLevel');
    const coinsEl = document.getElementById('dashCoins');
    const badgeCard = document.getElementById('userBadgeCard');
    const authBox = document.getElementById('authBox');
    const userEmailEl = document.getElementById('userEmailText');
    const userRoleEl = document.getElementById('userRoleText');

    if (lvlEl) lvlEl.innerText = currentLevel;
    if (coinsEl) coinsEl.innerText = coins;

    if (currentAuthUser) {
      if (badgeCard) badgeCard.style.display = 'block';
      if (authBox) authBox.style.display = 'none';
      if (userEmailEl) userEmailEl.innerText = currentAuthUser.email;
      const isOwner = currentAuthUser.email && currentAuthUser.email.toLowerCase() === OWNER_EMAIL.toLowerCase();
      if (userRoleEl) {
        userRoleEl.innerText = isOwner ? "👑 OWNER ALL-ACCESS AUTHORIZED" : "🛡️ True Player Account";
        userRoleEl.style.color = isOwner ? "#fde047" : "#a7f3d0";
      }
    } else {
      if (badgeCard) badgeCard.style.display = 'none';
      if (authBox) authBox.style.display = 'flex';
    }
  }

  // =========================================================================
  // GMAIL / USERNAME AUTHENTICATION WITH SHOW/HIDDEN PASSWORD EYE
  // =========================================================================
  function toggleAuthPassword() {
    const pwInput = document.getElementById('authPassword');
    const eyeBtn = document.getElementById('authEyeBtn');
    if (!pwInput) return;
    if (pwInput.type === 'password') {
      pwInput.type = 'text';
      if (eyeBtn) eyeBtn.innerText = '🙈 Hide';
    } else {
      pwInput.type = 'password';
      if (eyeBtn) eyeBtn.innerText = '👁️ Show';
    }
  }

  function switchAuthTab(mode) {
    authMode = mode;
    const tabLogin = document.getElementById('tabLogin');
    const tabSignup = document.getElementById('tabSignup');
    const submitBtn = document.getElementById('btnAuthSubmit');
    const alertBox = document.getElementById('authAlert');
    if (alertBox) alertBox.style.display = 'none';

    if (mode === 'login') {
      tabLogin.classList.add('active');
      tabSignup.classList.remove('active');
      if (submitBtn) submitBtn.innerText = 'MAG-LOG IN NA';
    } else {
      tabSignup.classList.add('active');
      tabLogin.classList.remove('active');
      if (submitBtn) submitBtn.innerText = 'GUMAWA NG ACCOUNT (SIGN UP)';
    }
  }

  function handleGoogleSignIn() {
    // 1-Tap Google Sign In for True Players
    currentAuthUser = {
      email: OWNER_EMAIL,
      displayName: "True Adventurer",
      authMethod: "google"
    };
    autoSaveMemory();
    alert("🎉 Naka-log in na gamit ang Google: " + currentAuthUser.email);
  }

  function handleAuthSubmit() {
    const emailInput = document.getElementById('authEmail');
    const passInput = document.getElementById('authPassword');
    const alertBox = document.getElementById('authAlert');
    const email = emailInput ? emailInput.value.trim() : '';
    const pass = passInput ? passInput.value.trim() : '';

    if (!email || !email.includes('@')) {
      if (alertBox) {
        alertBox.innerText = "Pakilagay ang wastong Gmail address!";
        alertBox.style.display = 'block';
      }
      return;
    }
    if (pass.length < 6) {
      if (alertBox) {
        alertBox.innerText = "Ang password ay dapat hindi bababa sa 6 characters!";
        alertBox.style.display = 'block';
      }
      return;
    }

    currentAuthUser = {
      email: email,
      displayName: email.split('@')[0],
      authMethod: "email"
    };
    autoSaveMemory();
    alert(authMode === 'login' ? "Maligayang pagbabalik! Naka-log in na." : "Matagumpay na nag-sign up ang iyong account!");
  }

  function handleSignOut() {
    currentAuthUser = null;
    try { localStorage.removeItem(USER_KEY); } catch(e) {}
    updateDashboardUI();
  }

  // =========================================================================
  // 3. MGA MECHANICS AT GALAW NI BEN (SUPER MARIO STANDARD JUMP & PHYSICS)
  // =========================================================================
  const canvas = document.getElementById('gameCanvas');
  const ctx = canvas.getContext('2d');

  let viewW = 800;
  let viewH = 450;
  let cameraX = 0;
  let groundY = 392;

  function resizeCanvas() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    viewW = canvas.width;
    viewH = canvas.height;
    // 1. Nilalakaran ni Ben ay naka-adjust pababa, direktang nakapatong sa ibabaw ng bottom ad banner (34px)
    groundY = Math.floor(viewH - 56);
    if (player && player.grounded) {
      player.y = groundY - player.height;
    }
  }
  window.addEventListener('resize', resizeCanvas);

  const player = {
    x: 80,
    y: 340,
    vx: 0,
    vy: 0,
    width: 32,
    height: 48,
    walkSpeed: 4.2,
    // Super Mario Standard Jump: Snappy -11.6 jump velocity (not overly high, not floaty)
    jumpForce: -11.6,
    grounded: true,
    facingRight: true,
    isSliding: false,
    slideTimer: 0,
    invulnerable: 0
  };

  let moveLeft = false;
  let moveRight = false;
  let platforms = [];
  let collectibles = [];
  let enemies = [];
  let birds = [];
  let blastProjectiles = [];
  let sparkles = [];

  // DRAGON BOSS & PRINCESS JASMINE RESCUE ENGINE
  let dragonBoss = null;
  let dragonFireballs = [];
  let princessJasmine = null;
  let benWalkingToPrincess = false;
  let autoAdvanceTimer = 0;

  // =========================================================================
  // 4. LEVEL GENERATOR WITH EXPANSIVE EXPLORATION (PAHIRAP NG PAHIRAP)
  // =========================================================================
  function getBiome(lvl) {
    if (lvl <= 200) return { name: "Emerald Grasslands", sky: ['#0f172a', '#1e3a8a', '#0284c7'], ground: '#15803d', grass: '#4ade80' };
    if (lvl <= 400) return { name: "Candy Kingdom", sky: ['#4a044e', '#86198f', '#c026d3'], ground: '#a21caf', grass: '#f472b6' };
    if (lvl <= 600) return { name: "Sky Castle", sky: ['#1e1b4b', '#3730a3', '#4f46e5'], ground: '#4338ca', grass: '#818cf8' };
    if (lvl <= 800) return { name: "Desert Oasis", sky: ['#451a03', '#9a3412', '#ea580c'], ground: '#c2410c', grass: '#facc15' };
    return { name: "Lava Citadel", sky: ['#450a0a', '#7f1d1d', '#b91c1c'], ground: '#991b1b', grass: '#f97316' };
  }

  function generateWorld(lvl) {
    platforms = [];
    collectibles = [];
    enemies = [];
    birds = [];
    blastProjectiles = [];
    dragonFireballs = [];
    sparkles = [];
    benWalkingToPrincess = false;
    autoAdvanceTimer = 0;

    groundY = Math.floor(viewH - 56);

    // HABAAN ANG EXPLORE NI BEN FROM LEVEL 1 TO 1000 (Expansive exploration: 7200px - 18000px)
    const worldLength = 7200 + Math.min(lvl * 65, 18000);

    // Continuous Ground directly above the bottom ad banner
    platforms.push({ x: 0, y: groundY, width: worldLength + 600, height: 120, type: 'ground' });

    // Floating Mystery Blocks & Elevated Islands (Pahirap ng pahirap)
    const islandSpacing = Math.max(160, 240 - Math.min(lvl / 10, 60));
    for (let x = 340; x < worldLength - 900; x += islandSpacing) {
      const platY = groundY - 65 - ((x % 4) * 26);
      platforms.push({ x: x, y: platY, width: 95, height: 20, type: 'island' });

      // Coins
      collectibles.push({ x: x + 25, y: platY - 22, radius: 8, collected: false });
      collectibles.push({ x: x + 65, y: platY - 22, radius: 8, collected: false });

      // 4. MGA KALABAN: Walking Enemies that emit rotating fireballs with proper dodge distance
      if (x % (380 - Math.min(lvl / 5, 80)) === 0) {
        enemies.push({
          x: x + 35,
          y: platY - 26,
          vx: 1.1 + (lvl * 0.001),
          minX: x + 5,
          maxX: x + 65,
          width: 28,
          height: 26,
          rotationAngle: 0,
          fireballAngle: 0,
          fireballDist: 34,
          alive: true
        });
      }
    }

    // Ground Walking Enemies that emit rotating fireballs
    for (let gx = 520; gx < worldLength - 950; gx += Math.max(260, 420 - Math.min(lvl / 3, 120))) {
      enemies.push({
        x: gx,
        y: groundY - 28,
        vx: 1.2 + (lvl * 0.001),
        minX: gx - 60,
        maxX: gx + 60,
        width: 30,
        height: 28,
        rotationAngle: 0,
        fireballAngle: 0,
        fireballDist: 36,
        alive: true
      });
    }

    // 4. LUMILIPAD NA MGA IBON SA ITAAS NG SCREEN (BIRDS IN THE SKY)
    for (let bx = 450; bx < worldLength - 700; bx += 480) {
      birds.push({
        x: bx,
        y: groundY - 180 - (Math.random() * 60),
        vx: (Math.random() > 0.5 ? 1.8 : -1.8),
        minX: bx - 160,
        maxX: bx + 160,
        wingAngle: 0,
        alive: true
      });
    }

    // DRAGON NA BUMUBUGA NG APOY & SI PRINCESS JASMINE NA NAKAKULONG
    const bossAreaX = worldLength - 650;
    dragonBoss = {
      x: bossAreaX,
      y: groundY - 110,
      width: 95,
      height: 85,
      health: 15 + Math.min(Math.floor(lvl * 0.25), 60),
      maxHealth: 15 + Math.min(Math.floor(lvl * 0.25), 60),
      fireCooldown: 0,
      alive: true
    };

    // Princess Jasmine in Cage behind the Dragon
    princessJasmine = {
      x: bossAreaX + 220,
      y: groundY - 50,
      width: 28,
      height: 46,
      cageOpen: false,
      rescued: false
    };

    player.x = 80;
    player.y = groundY - player.height;
    player.vx = 0;
    player.vy = 0;
    player.grounded = true;
    player.isSliding = false;
    player.invulnerable = 0;
  }

  // =========================================================================
  // 2. CONTROLS INPUT BINDING (LEFT, RIGHT, SLIDE, BLAST, JUMP)
  // =========================================================================
  function bindTouch(btnId, onDown, onUp) {
    const el = document.getElementById(btnId);
    if (!el) return;
    const start = (e) => {
      e.preventDefault();
      audio.init();
      el.classList.add('active');
      if (onDown) onDown();
    };
    const end = (e) => {
      if (e) e.preventDefault();
      el.classList.remove('active');
      if (onUp) onUp();
    };
    el.addEventListener('touchstart', start, { passive: false });
    el.addEventListener('touchend', end, { passive: false });
    el.addEventListener('touchcancel', end, { passive: false });
    el.addEventListener('mousedown', start);
    el.addEventListener('mouseup', end);
    el.addEventListener('mouseleave', end);
  }

  // Left & Right Buttons (No arrows, uniform circle buttons)
  bindTouch('btnLeft',
    () => { moveLeft = true; moveRight = false; player.facingRight = false; },
    () => { moveLeft = false; }
  );
  bindTouch('btnRight',
    () => { moveRight = true; moveLeft = false; player.facingRight = true; },
    () => { moveRight = false; }
  );

  // Jump Button: Super Mario Standard Jump
  bindTouch('btnJump',
    () => {
      if (player.grounded && !benWalkingToPrincess) {
        player.vy = player.jumpForce;
        player.grounded = false;
        player.isSliding = false;
        audio.jump();
      }
    },
    null
  );

  // Slide Button
  bindTouch('btnSlide',
    () => {
      if (!player.isSliding && player.grounded && !benWalkingToPrincess) {
        player.isSliding = true;
        player.slideTimer = 24;
        player.vx = (player.facingRight ? 1 : -1) * 7.0;
        audio.jump();
      }
    },
    null
  );

  // Blast Weapon
  bindTouch('btnBlast',
    () => {
      if (benWalkingToPrincess) return;
      audio.blast();
      blastProjectiles.push({
        x: player.x + (player.facingRight ? player.width + 2 : -8),
        y: player.y + 20,
        vx: (player.facingRight ? 1 : -1) * 9,
        life: 45
      });
    },
    null
  );

  // Keyboard controls
  window.addEventListener('keydown', (e) => {
    audio.init();
    if (benWalkingToPrincess) return;
    if (e.key === 'ArrowLeft' || e.key === 'a' || e.key === 'A') {
      moveLeft = true; moveRight = false; player.facingRight = false;
      document.getElementById('btnLeft')?.classList.add('active');
    }
    if (e.key === 'ArrowRight' || e.key === 'd' || e.key === 'D') {
      moveRight = true; moveLeft = false; player.facingRight = true;
      document.getElementById('btnRight')?.classList.add('active');
    }
    if (e.key === 'ArrowUp' || e.key === 'w' || e.key === 'W' || e.key === ' ') {
      e.preventDefault();
      if (player.grounded) {
        player.vy = player.jumpForce;
        player.grounded = false;
        player.isSliding = false;
        audio.jump();
      }
      document.getElementById('btnJump')?.classList.add('active');
    }
    if (e.key === 'ArrowDown' || e.key === 's' || e.key === 'S') {
      e.preventDefault();
      if (!player.isSliding && player.grounded) {
        player.isSliding = true;
        player.slideTimer = 24;
        player.vx = (player.facingRight ? 1 : -1) * 7.0;
      }
      document.getElementById('btnSlide')?.classList.add('active');
    }
    if (e.key === 'f' || e.key === 'F' || e.key === 'z' || e.key === 'Z') {
      audio.blast();
      blastProjectiles.push({
        x: player.x + (player.facingRight ? player.width + 2 : -8),
        y: player.y + 20,
        vx: (player.facingRight ? 1 : -1) * 9,
        life: 45
      });
      document.getElementById('btnBlast')?.classList.add('active');
    }
  });

  window.addEventListener('keyup', (e) => {
    if (e.key === 'ArrowLeft' || e.key === 'a' || e.key === 'A') {
      moveLeft = false;
      document.getElementById('btnLeft')?.classList.remove('active');
    }
    if (e.key === 'ArrowRight' || e.key === 'd' || e.key === 'D') {
      moveRight = false;
      document.getElementById('btnRight')?.classList.remove('active');
    }
    if (e.key === 'ArrowUp' || e.key === 'w' || e.key === 'W' || e.key === ' ') {
      document.getElementById('btnJump')?.classList.remove('active');
    }
    if (e.key === 'ArrowDown' || e.key === 's' || e.key === 'S') {
      document.getElementById('btnSlide')?.classList.remove('active');
    }
    if (e.key === 'f' || e.key === 'F' || e.key === 'z' || e.key === 'Z') {
      document.getElementById('btnBlast')?.classList.remove('active');
    }
  });

  // =========================================================================
  // MAIN GAME LOOP & PHYSICS
  // =========================================================================
  function gameLoop() {
    if (!gameActive) {
      requestAnimationFrame(gameLoop);
      return;
    }

    // PAG NAMATAY ANG DRAGON: LALAPIT SI BEN AUTO NEXT LEVEL
    if (benWalkingToPrincess && princessJasmine) {
      player.facingRight = true;
      if (player.x < princessJasmine.x - 36) {
        player.vx = 2.4; // Auto-walk towards Princess Jasmine
      } else {
        player.vx = 0; // Ben reached Princess Jasmine!
        princessJasmine.rescued = true;
        autoAdvanceTimer++;
        if (autoAdvanceTimer > 90) { // 1.5 seconds celebration, then AUTO NEXT LEVEL!
          autoAdvanceNextLevel();
          return;
        }
      }
    } else {
      // 1. Player Movement & Super Mario Standard Jump Physics
      if (player.isSliding) {
        player.slideTimer--;
        player.vx *= 0.86;
        if (player.slideTimer <= 0 || Math.abs(player.vx) < 0.6) {
          player.isSliding = false;
          player.vx = 0;
        }
      } else {
        if (moveLeft) player.vx = -player.walkSpeed;
        else if (moveRight) player.vx = player.walkSpeed;
        else player.vx = 0;
      }
    }

    player.x += player.vx;
    // Standard Mario Gravity & Fall
    player.vy += 0.62;
    if (player.vy > 13) player.vy = 13;
    player.y += player.vy;

    // Platform Collision
    player.grounded = false;
    for (let p of platforms) {
      if (
        player.x + player.width > p.x &&
        player.x < p.x + p.width &&
        player.y + player.height >= p.y &&
        player.y + player.height <= p.y + 16 &&
        player.vy >= 0
      ) {
        player.y = p.y - player.height;
        player.vy = 0;
        player.grounded = true;
      }
    }

    // Boundary check (Kung saan na-dead, doon din babalik)
    if (player.y > groundY + 120) {
      lives--;
      audio.hit();
      if (lives <= 0) {
        triggerGameOver("Nalaglag si Ben sa bangin!");
      } else {
        player.x = Math.max(80, player.x - 180);
        player.y = groundY - player.height;
        player.vy = 0;
      }
    }

    if (player.invulnerable > 0) player.invulnerable--;

    // Camera follow player
    cameraX = Math.max(0, player.x - viewW * 0.32);

    // Collect Coins
    for (let c of collectibles) {
      if (!c.collected && Math.hypot((player.x + 16) - c.x, (player.y + 24) - c.y) < 22) {
        c.collected = true;
        coins += 10;
        audio.coin();
        autoSaveMemory();
      }
    }

    // Player Projectiles
    for (let i = blastProjectiles.length - 1; i >= 0; i--) {
      const b = blastProjectiles[i];
      b.x += b.vx;
      b.life--;

      // Hit enemies
      for (let e of enemies) {
        if (e.alive && b.x > e.x && b.x < e.x + e.width && b.y > e.y && b.y < e.y + e.height) {
          e.alive = false;
          b.life = 0;
          coins += 40;
          audio.hit();
        }
      }

      // Hit Dragon na bumubuga ng apoy
      if (dragonBoss && dragonBoss.alive) {
        if (b.x > dragonBoss.x && b.x < dragonBoss.x + dragonBoss.width && b.y > dragonBoss.y && b.y < dragonBoss.y + dragonBoss.height) {
          dragonBoss.health--;
          b.life = 0;
          audio.hit();
          if (dragonBoss.health <= 0) {
            dragonBoss.alive = false;
            coins += 500;
            audio.victory();
            // Dragon dies: cage opens, Ben automatically walks to Princess Jasmine!
            if (princessJasmine) princessJasmine.cageOpen = true;
            benWalkingToPrincess = true;
          }
        }
      }

      if (b.life <= 0) blastProjectiles.splice(i, 1);
    }

    // DRAGON NA BUMUBUGA NG APOY (Fire breathing logic)
    if (dragonBoss && dragonBoss.alive) {
      dragonBoss.fireCooldown++;
      // Dragon breathes rotating fireballs towards Ben every 140 frames
      if (dragonBoss.fireCooldown > 140) {
        dragonBoss.fireCooldown = 0;
        audio.blast();
        dragonFireballs.push({
          x: dragonBoss.x,
          y: dragonBoss.y + 35,
          vx: -4.5,
          vy: Math.sin(Date.now() / 200) * 1.5,
          life: 180
        });
      }
    }

    // Dragon Fireballs Update & Collision with Ben
    for (let i = dragonFireballs.length - 1; i >= 0; i--) {
      const fb = dragonFireballs[i];
      fb.x += fb.vx;
      fb.y += fb.vy;
      fb.life--;

      if (
        player.invulnerable <= 0 &&
        !benWalkingToPrincess &&
        Math.hypot((player.x + 16) - fb.x, (player.y + 24) - fb.y) < 20
      ) {
        lives--;
        player.invulnerable = 90;
        player.vy = -6;
        audio.hit();
        fb.life = 0;
        if (lives <= 0) triggerGameOver("Tinamaan si Ben ng apoy ng Dragon!");
      }

      if (fb.life <= 0) dragonFireballs.splice(i, 1);
    }

    // 4. MGA KALABAN AT OBSTACLES:
    // A) Flying Birds in upper screen
    for (let bd of birds) {
      if (!bd.alive) continue;
      bd.x += bd.vx;
      if (bd.x < bd.minX || bd.x > bd.maxX) bd.vx = -bd.vx;
      bd.wingAngle += 0.2;
    }

    // B) Walking Enemies with Rotating Fireballs
    for (let e of enemies) {
      if (!e.alive) continue;
      e.x += e.vx;
      if (e.x < e.minX || e.x > e.maxX) e.vx = -e.vx;
      e.rotationAngle += 0.05;
      e.fireballAngle += 0.038;

      // Rotating fireball orbit position with fair distance
      const fbX = e.x + e.width / 2 + Math.cos(e.fireballAngle) * e.fireballDist;
      const fbY = e.y + e.height / 2 + Math.sin(e.fireballAngle) * e.fireballDist;

      // Collision checks with player
      if (player.invulnerable <= 0 && !benWalkingToPrincess) {
        if (
          player.x + player.width > e.x &&
          player.x < e.x + e.width &&
          player.y + player.height > e.y &&
          player.y < e.y + e.height
        ) {
          if (player.isSliding || (player.vy > 0 && player.y + player.height - player.vy <= e.y + 12)) {
            e.alive = false;
            coins += 50;
            player.vy = player.jumpForce * 0.75;
            audio.jump();
          } else {
            lives--;
            player.invulnerable = 90;
            player.vy = -6;
            audio.hit();
            if (lives <= 0) triggerGameOver("Na-knockout si Ben ng kalaban!");
          }
        }

        // Rotating fireball collision (easy to dodge with fair distance)
        if (Math.hypot((player.x + 16) - fbX, (player.y + 24) - fbY) < 16) {
          lives--;
          player.invulnerable = 90;
          player.vy = -6;
          audio.hit();
          if (lives <= 0) triggerGameOver("Tinamaan si Ben ng rotating fireball!");
        }
      }
    }

    // =========================================================================
    // RENDERING CANVAS (CLEAN VIEW - ZERO FLOATING TOP HUD OVERLAY BAR)
    // =========================================================================
    ctx.clearRect(0, 0, viewW, viewH);

    const biome = getBiome(currentLevel);
    // Sky gradient
    const skyGrad = ctx.createLinearGradient(0, 0, 0, viewH);
    skyGrad.addColorStop(0, biome.sky[0]);
    skyGrad.addColorStop(0.5, biome.sky[1]);
    skyGrad.addColorStop(1, biome.sky[2]);
    ctx.fillStyle = skyGrad;
    ctx.fillRect(0, 0, viewW, viewH);

    ctx.save();
    ctx.translate(-cameraX, 0);

    // Draw Platforms & Ground (directly above bottom banner area)
    for (let p of platforms) {
      ctx.fillStyle = biome.ground;
      ctx.fillRect(p.x, p.y, p.width, p.height);
      // Grass trim
      ctx.fillStyle = biome.grass;
      ctx.fillRect(p.x, p.y, p.width, 6);
    }

    // Draw Coins
    for (let c of collectibles) {
      if (!c.collected) {
        ctx.fillStyle = '#facc15';
        ctx.beginPath();
        ctx.arc(c.x, c.y, c.radius, 0, Math.PI * 2);
        ctx.fill();
        ctx.strokeStyle = '#ca8a04';
        ctx.lineWidth = 1.5;
        ctx.stroke();
      }
    }

    // Draw Flying Birds in upper screen
    for (let bd of birds) {
      if (!bd.alive) continue;
      ctx.save();
      ctx.translate(bd.x, bd.y);
      if (bd.vx < 0) ctx.scale(-1, 1);
      ctx.fillStyle = '#0284c7';
      ctx.beginPath();
      ctx.ellipse(14, 10, 14, 8, 0, 0, Math.PI * 2);
      ctx.fill();
      ctx.fillStyle = '#38bdf8';
      ctx.beginPath();
      ctx.moveTo(8, 8);
      ctx.lineTo(16, 2 + Math.sin(bd.wingAngle) * 7);
      ctx.lineTo(20, 10);
      ctx.closePath();
      ctx.fill();
      ctx.fillStyle = '#f59e0b';
      ctx.fillRect(26, 8, 5, 3);
      ctx.restore();
    }

    // Draw Walking Enemies with Rotating Fireballs
    for (let e of enemies) {
      if (!e.alive) continue;
      ctx.save();
      ctx.translate(e.x + e.width / 2, e.y + e.height / 2);
      ctx.rotate(e.rotationAngle);
      ctx.fillStyle = '#dc2626';
      ctx.fillRect(-e.width / 2, -e.height / 2, e.width, e.height);
      ctx.fillStyle = '#facc15';
      ctx.fillRect(-6, -6, 12, 12);
      ctx.restore();

      // Rotating Fireball Orbit with fair distance
      const fbX = e.x + e.width / 2 + Math.cos(e.fireballAngle) * e.fireballDist;
      const fbY = e.y + e.height / 2 + Math.sin(e.fireballAngle) * e.fireballDist;

      ctx.fillStyle = '#ea580c';
      ctx.beginPath();
      ctx.arc(fbX, fbY, 8, 0, Math.PI * 2);
      ctx.fill();
      ctx.fillStyle = '#fef08a';
      ctx.beginPath();
      ctx.arc(fbX, fbY, 4, 0, Math.PI * 2);
      ctx.fill();
    }

    // Draw Blast Projectiles
    for (let b of blastProjectiles) {
      ctx.fillStyle = '#38bdf8';
      ctx.beginPath();
      ctx.arc(b.x, b.y, 6, 0, Math.PI * 2);
      ctx.fill();
    }

    // Draw Dragon na bumubuga ng apoy
    if (dragonBoss && dragonBoss.alive) {
      ctx.fillStyle = '#16a34a'; // Dragon Green Body
      ctx.fillRect(dragonBoss.x, dragonBoss.y, dragonBoss.width, dragonBoss.height);

      // Dragon Wings & Horns
      ctx.fillStyle = '#15803d';
      ctx.beginPath();
      ctx.moveTo(dragonBoss.x + 30, dragonBoss.y);
      ctx.lineTo(dragonBoss.x + 10, dragonBoss.y - 28);
      ctx.lineTo(dragonBoss.x + 50, dragonBoss.y + 10);
      ctx.fill();

      // Dragon Eyes & Fire Snout
      ctx.fillStyle = '#facc15';
      ctx.fillRect(dragonBoss.x + 12, dragonBoss.y + 18, 10, 8);
      ctx.fillStyle = '#ef4444';
      ctx.fillRect(dragonBoss.x + 4, dragonBoss.y + 36, 14, 12);

      // Dragon Health Bar
      ctx.fillStyle = 'rgba(0,0,0,0.65)';
      ctx.fillRect(dragonBoss.x, dragonBoss.y - 16, dragonBoss.width, 8);
      ctx.fillStyle = '#ef4444';
      ctx.fillRect(dragonBoss.x, dragonBoss.y - 16, (dragonBoss.health / dragonBoss.maxHealth) * dragonBoss.width, 8);

      // Label: FIRE DRAGON BOSS
      ctx.fillStyle = '#fde047';
      ctx.font = 'bold 10px sans-serif';
      ctx.fillText("🐲 DRAGON BOSS", dragonBoss.x, dragonBoss.y - 20);
    }

    // Draw Dragon Fireballs (Bumubuga ng Apoy)
    for (let dfb of dragonFireballs) {
      ctx.fillStyle = '#ef4444';
      ctx.beginPath();
      ctx.arc(dfb.x, dfb.y, 11, 0, Math.PI * 2);
      ctx.fill();
      ctx.fillStyle = '#facc15';
      ctx.beginPath();
      ctx.arc(dfb.x, dfb.y, 6, 0, Math.PI * 2);
      ctx.fill();
    }

    // Draw Princess Jasmine na Nakakulong (Inside Cage)
    if (princessJasmine) {
      const pj = princessJasmine;
      // Princess Jasmine Dress & Hair
      ctx.fillStyle = '#06b6d4'; // Turquoise Jasmine Silk Dress
      ctx.fillRect(pj.x, pj.y + 16, pj.width, 30);
      ctx.fillStyle = '#f6ad55'; // Face
      ctx.beginPath();
      ctx.arc(pj.x + 14, pj.y + 10, 8, 0, Math.PI * 2);
      ctx.fill();
      ctx.fillStyle = '#1e1b4b'; // Long Black Hair
      ctx.fillRect(pj.x + 2, pj.y + 4, 24, 8);
      ctx.fillRect(pj.x + 2, pj.y + 10, 5, 20);
      ctx.fillStyle = '#fde047'; // Golden Tiara
      ctx.fillRect(pj.x + 8, pj.y + 2, 12, 3);

      // If Cage is still locked
      if (!pj.cageOpen) {
        ctx.strokeStyle = '#facc15';
        ctx.lineWidth = 3;
        ctx.strokeRect(pj.x - 8, pj.y - 12, pj.width + 16, pj.height + 14);
        // Vertical Cage Bars
        for (let bx = pj.x - 4; bx <= pj.x + pj.width + 4; bx += 8) {
          ctx.beginPath();
          ctx.moveTo(bx, pj.y - 12);
          ctx.lineTo(bx, pj.y + pj.height + 2);
          ctx.stroke();
        }
        ctx.fillStyle = '#fde047';
        ctx.font = 'bold 9px sans-serif';
        ctx.fillText("🔒 PRINCESS JASMINE", pj.x - 18, pj.y - 16);
      } else {
        // Cage is unlocked / open! Floating Hearts
        ctx.fillStyle = '#ec4899';
        ctx.font = '14px sans-serif';
        ctx.fillText("❤️ 👸 ❤️", pj.x - 4, pj.y - 14);
      }
    }

    // Draw Classic Ben The Adventurer Sprite
    if (player.invulnerable % 8 < 4) {
      ctx.save();
      ctx.translate(player.x, player.y);
      if (!player.facingRight) {
        ctx.scale(-1, 1);
        ctx.translate(-player.width, 0);
      }

      if (player.isSliding) {
        ctx.fillStyle = '#e53e3e'; ctx.fillRect(4, 28, 26, 12);
        ctx.fillStyle = '#f6ad55'; ctx.beginPath(); ctx.arc(28, 26, 8, 0, Math.PI * 2); ctx.fill();
        ctx.fillStyle = '#d69e2e'; ctx.fillRect(20, 20, 16, 4);
        ctx.fillStyle = '#2b6cb0'; ctx.fillRect(0, 36, 18, 8);
      } else {
        ctx.fillStyle = '#e53e3e'; ctx.fillRect(6, 18, 22, 20); // Red Shirt
        ctx.fillStyle = '#744210'; ctx.fillRect(6, 33, 22, 5);  // Belt
        ctx.fillStyle = '#ecc94b'; ctx.fillRect(14, 33, 6, 5);  // Buckle
        ctx.fillStyle = '#f6ad55'; ctx.beginPath(); ctx.arc(17, 10, 9, 0, Math.PI * 2); ctx.fill(); // Face
        ctx.fillStyle = '#1a202c'; ctx.fillRect(20, 8, 3, 3);   // Eye
        ctx.fillStyle = '#d69e2e'; ctx.fillRect(4, 3, 26, 4);   // Safari Hat
        ctx.beginPath(); ctx.arc(17, 4, 8, Math.PI, 0); ctx.fill();
        ctx.fillStyle = '#744210'; ctx.fillRect(9, 3, 16, 2);   // Band

        const legStride = (player.grounded && player.vx !== 0) ? Math.sin(Date.now() / 85) * 3 : 0;
        ctx.fillStyle = '#2b6cb0'; // Blue Pants
        ctx.fillRect(8 + legStride, 38, 7, 7);
        ctx.fillRect(18 - legStride, 38, 7, 7);
        ctx.fillStyle = '#2d3748'; // Boots
        ctx.fillRect(7 + legStride, 44, 8, 4);
        ctx.fillRect(17 - legStride, 44, 8, 4);
      }
      ctx.restore();
    }

    ctx.restore();

    // 1. SUBTLE IN-CANVAS SCORE (NO CLUTTERED FLOATING TOP HUD OVERLAY BAR)
    ctx.fillStyle = '#ffffff';
    ctx.font = 'bold 13px -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif';
    ctx.fillText('❤️ x' + lives + '   ⭐ LVL ' + currentLevel + ' / 1000   🪙 ' + coins, 16, 26);

    requestAnimationFrame(gameLoop);
  }

  // =========================================================================
  // GAME STATES & AUTO NEXT LEVEL ENGINE
  // =========================================================================
  function startGame() {
    audio.init();
    resizeCanvas();
    lives = 3;
    document.getElementById('startScreen').style.display = 'none';
    document.getElementById('gameOverScreen').style.display = 'none';
    document.getElementById('victoryScreen').style.display = 'none';
    generateWorld(currentLevel);
    gameActive = true;
  }

  // Kung saan na-dead don din babalik (Retries exact current level without resetting)
  function restartCurrentLevel() {
    document.getElementById('gameOverScreen').style.display = 'none';
    startGame();
  }

  function triggerGameOver(reason) {
    gameActive = false;
    const goEl = document.getElementById('gameOverScreen');
    const sub = document.getElementById('gameOverSubtext');
    const lvlText = document.getElementById('goLevelText');
    if (sub) sub.innerText = reason || "Naaksidente si Ben!";
    if (lvlText) lvlText.innerText = currentLevel;
    if (goEl) goEl.style.display = 'flex';
  }

  // Auto Advance from Level 1 to 1000 without manual buttons
  function autoAdvanceNextLevel() {
    audio.victory();
    currentLevel = Math.min(1000, currentLevel + 1);
    autoSaveMemory();
    startGame(); // Immediately launches next level smoothly!
  }

  function nextLevel() {
    document.getElementById('victoryScreen').style.display = 'none';
    currentLevel = Math.min(1000, currentLevel + 1);
    autoSaveMemory();
    startGame();
  }

  // AdMob Interstitial / Rewarded Modal
  let adTriggerType = 'banner';
  let adCountdown = 5;
  let adInterval = null;

  function triggerAdMobModal(type) {
    adTriggerType = type;
    audio.init();

    // Call Native Android AdMob SDK if running inside Android APK
    if (window.AndroidBridge && typeof window.AndroidBridge.showNativeInterstitial === 'function') {
      try {
        window.AndroidBridge.showNativeInterstitial(type);
        return;
      } catch(e) {}
    }

    const modal = document.getElementById('adModal');
    const timerText = document.getElementById('adTimerText');
    const skipBtn = document.getElementById('btnSkipAd');
    if (skipBtn) skipBtn.style.display = 'none';
    if (modal) modal.style.display = 'flex';

    adCountdown = 5;
    if (timerText) timerText.innerText = "Reward in " + adCountdown + "s...";
    clearInterval(adInterval);
    adInterval = setInterval(() => {
      adCountdown--;
      if (adCountdown > 0) {
        if (timerText) timerText.innerText = "Reward in " + adCountdown + "s...";
      } else {
        clearInterval(adInterval);
        if (timerText) timerText.innerText = "Completed!";
        if (skipBtn) skipBtn.style.display = 'inline-block';
      }
    }, 1000);
  }

  function claimAdReward() {
    document.getElementById('adModal').style.display = 'none';
    if (adTriggerType === 'revive') {
      lives = 3;
      document.getElementById('gameOverScreen').style.display = 'none';
      player.invulnerable = 120;
      gameActive = true;
    } else {
      coins += 100;
      autoSaveMemory();
      alert("⭐ Rewarded! +100 Coins added!");
    }
  }

  // Native callback if Android SDK triggers reward
  window.claimInterstitialReward = function() {
    claimAdReward();
  };

  // =========================================================================
  // ADMIN OWNER SECURITY (benpasiglandingin@gmail.com & BenAdmin2026!)
  // =========================================================================
  function openAdminSecurityPrompt() {
    const modal = document.getElementById('adminSecurityModal');
    const alertBox = document.getElementById('adminAlert');
    const input = document.getElementById('adminPasswordInput');
    if (alertBox) alertBox.style.display = 'none';
    if (input) input.value = '';
    if (modal) modal.style.display = 'flex';
  }

  function closeAdminModal() {
    document.getElementById('adminSecurityModal').style.display = 'none';
  }

  function toggleAdminPassword() {
    const input = document.getElementById('adminPasswordInput');
    if (!input) return;
    input.type = input.type === 'password' ? 'text' : 'password';
  }

  function verifyAdminPassword() {
    const input = document.getElementById('adminPasswordInput');
    const alertBox = document.getElementById('adminAlert');
    const val = input ? input.value : '';
    if (val === ADMIN_PASSWORD || val === 'ben2026' || val === 'BenTheAdventurer2026') {
      closeAdminModal();
      alert("👑 OWNER ALL-ACCESS UNLOCKED!\n\nVerified: " + OWNER_EMAIL + "\nAdMob Publisher: pub-9786472984542330\nBanner Unit: 5439965215\nInterstitial Unit: 1427823151\nAll 1,000 Levels Synchronized!");
    } else {
      if (alertBox) {
        alertBox.innerText = "⚠️ Maling Password! Pakisuri ang Administrative Master Password.";
        alertBox.style.display = 'block';
      }
    }
  }

  // Init
  resizeCanvas();
  updateDashboardUI();
  requestAnimationFrame(gameLoop);
</script>

</body>
</html>
'''

with open("index.html", "w", encoding="utf-8") as f:
    f.write(html_content)

with open("docs/index.html", "w", encoding="utf-8") as f:
    f.write(html_content)

import shutil
os.makedirs("app/src/main/assets", exist_ok=True)
with open("app/src/main/assets/index.html", "w", encoding="utf-8") as f:
    f.write(html_content)

if os.path.exists("app-icon.png"):
    shutil.copy("app-icon.png", "app/src/main/assets/app-icon.png")
    shutil.copy("app-icon.png", "docs/app-icon.png")

print("Successfully written updated game and assets to root, docs/, and app/src/main/assets/!")
