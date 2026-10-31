import re
import shutil

print("Reading index.html...")
with open("index.html", "r") as f:
    html = f.read()

# 1. Update CSS
css_additions = """
    /* HUD Buttons (Sound toggle, Watch Ad, etc.) */
    .hud-btn {
      background: rgba(0, 240, 255, 0.15);
      border: 1px solid rgba(0, 240, 255, 0.4);
      border-radius: 6px;
      cursor: pointer;
      color: #fff;
      font-size: 11px;
      font-weight: 700;
      padding: 3px 8px;
      display: inline-flex;
      align-items: center;
      gap: 4px;
      transition: all 0.15s ease;
      touch-action: manipulation;
    }
    .hud-btn:active {
      transform: scale(0.94);
      background: rgba(0, 240, 255, 0.3);
    }
    .hud-ad-btn {
      background: rgba(241, 168, 10, 0.22);
      border-color: #f1a80a;
      color: #fde047;
    }
    .hud-ad-btn:active {
      background: rgba(241, 168, 10, 0.45);
    }

    /* AdMob Responsive Banner Styling */
    .admob-banner {
      width: 96%;
      max-width: 480px;
      min-height: 50px;
      background: #020b18;
      border: 1px solid #1a3d66;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      position: relative;
      overflow: hidden;
      padding: 4px 10px;
      color: #7aa2cf;
      font-size: 11px;
      pointer-events: auto;
      cursor: pointer;
      transition: all 0.2s ease;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.6);
      margin-top: 4px;
    }
    .admob-banner:hover {
      background: #0a192f;
      border-color: #38bdf8;
    }
    .admob-fallback-content {
      width: 100%;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 8px;
    }
    .admob-badge-group {
      display: flex;
      align-items: center;
      gap: 6px;
      flex-shrink: 0;
    }
    .ad-tag {
      background: #f1a80a;
      color: #000;
      font-weight: 900;
      padding: 2px 6px;
      border-radius: 4px;
      font-size: 9px;
      letter-spacing: 0.5px;
    }
    .ad-pub-text {
      color: #fff;
      font-weight: bold;
      font-size: 10px;
    }
    .admob-ticker {
      flex: 1;
      text-align: center;
      font-size: 11px;
      color: #00f0ff;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      padding: 0 4px;
    }
    .btn-ad-action {
      background: linear-gradient(135deg, #f59e0b, #d97706);
      color: #000;
      font-weight: 900;
      font-size: 10px;
      padding: 6px 10px;
      border-radius: 6px;
      border: none;
      cursor: pointer;
      flex-shrink: 0;
      transition: transform 0.1s;
    }
    .btn-ad-action:active {
      transform: scale(0.92);
    }

    /* Interstitial Ad Modal (Full-Screen Overlay) */
    .interstitial-overlay {
      position: fixed;
      top: 0; left: 0; width: 100%; height: 100%;
      background: rgba(2, 6, 17, 0.94);
      backdrop-filter: blur(8px);
      z-index: 9999;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 16px;
      animation: fadeIn 0.2s ease-out;
    }
    .interstitial-card {
      background: #0f172a;
      border: 2px solid #0284c7;
      border-radius: 16px;
      width: 100%;
      max-width: 440px;
      box-shadow: 0 10px 40px rgba(0, 240, 255, 0.25);
      overflow: hidden;
      display: flex;
      flex-direction: column;
    }
    .interstitial-header {
      background: #020b18;
      padding: 10px 14px;
      border-bottom: 1px solid #1e293b;
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
    .btn-skip-ad {
      background: #334155;
      color: #94a3b8;
      border: 1px solid #475569;
      padding: 4px 10px;
      border-radius: 6px;
      font-size: 11px;
      font-weight: bold;
      cursor: not-allowed;
      transition: all 0.2s;
    }
    .btn-skip-ad.enabled {
      background: #ef4444;
      color: #fff;
      border-color: #f87171;
      cursor: pointer;
      box-shadow: 0 0 10px rgba(239, 68, 68, 0.4);
    }
    .interstitial-body {
      padding: 20px 18px;
      text-align: center;
      display: flex;
      flex-direction: column;
      align-items: center;
    }
    .interstitial-sponsor-badge {
      font-size: 10px;
      font-weight: 800;
      color: #38bdf8;
      letter-spacing: 1.5px;
      text-transform: uppercase;
      background: rgba(56, 189, 248, 0.1);
      padding: 3px 8px;
      border-radius: 12px;
      border: 1px solid rgba(56, 189, 248, 0.3);
    }
    .interstitial-visual-box {
      width: 100%;
      background: radial-gradient(circle, rgba(14, 165, 233, 0.15) 0%, rgba(15, 23, 42, 0.6) 100%);
      border: 1px dashed rgba(56, 189, 248, 0.4);
      border-radius: 12px;
      padding: 16px;
      margin: 12px 0 16px 0;
    }
    .interstitial-animated-icon {
      font-size: 38px;
      animation: floatHop 1.5s ease-in-out infinite alternate;
    }
    @keyframes floatHop {
      0% { transform: translateY(0); }
      100% { transform: translateY(-6px); }
    }
    .btn-interstitial-cta {
      width: 100%;
      background: linear-gradient(135deg, #10b981, #059669);
      color: #fff;
      font-weight: 900;
      font-size: 14px;
      padding: 12px 0;
      border-radius: 10px;
      border: 1px solid #34d399;
      cursor: pointer;
      box-shadow: 0 4px 15px rgba(16, 185, 129, 0.4);
      transition: all 0.15s;
    }
    .btn-interstitial-cta:active {
      transform: scale(0.97);
    }
    .btn-ad-revive {
      width: 100%;
      max-width: 280px;
      background: linear-gradient(135deg, #f59e0b, #b45309);
      color: #fff;
      font-weight: 900;
      font-size: 13px;
      padding: 11px 0;
      border-radius: 8px;
      border: 1px solid #fde047;
      cursor: pointer;
      box-shadow: 0 4px 15px rgba(245, 158, 11, 0.4);
      margin: 8px 0;
      transition: all 0.15s;
    }
    .btn-ad-revive:active {
      transform: scale(0.97);
    }
"""

# Insert CSS additions before </style>
html = html.replace("</style>", css_additions + "\n  </style>", 1)

# 2. Update Top HUD to add Sound button and Ad button
old_top_hud = """  <header id="topHud" class="top-hud">
    <div class="hud-item" id="hudLives">❤️ x10</div>
    <div class="hud-item">⏱️ <span id="timerText">05:00</span></div>
    <div class="hud-item" id="hudCoins">🪙 180</div>
    <div class="hud-item" id="hudLevel">⭐ LVL 1</div>
    <div class="hud-item" id="hudCloud" style="color:#38bdf8; font-size:10px;">☁️ Firestore: Online</div>
    <a href="./BenTheAdventurer.apk" download="BenTheAdventurer.apk" class="hud-apk-link" title="Download APK">
      APK ⬇
    </a>
  </header>"""

new_top_hud = """  <header id="topHud" class="top-hud">
    <div class="hud-item" id="hudLives">❤️ x10</div>
    <div class="hud-item">⏱️ <span id="timerText">05:00</span></div>
    <div class="hud-item" id="hudCoins">🪙 180</div>
    <div class="hud-item" id="hudLevel">⭐ LVL 1</div>
    <div class="hud-item" id="hudCloud" style="color:#38bdf8; font-size:10px;">☁️ Firestore: Online</div>
    <button type="button" class="hud-item hud-btn" id="hudSoundToggle" onclick="toggleAudio()" title="Sound On / Off">🔊 Sound: ON</button>
    <button type="button" class="hud-item hud-btn hud-ad-btn" onclick="showWebInterstitial('hud')" title="Watch Ad for +100 Coins">🎬 Ad +100🪙</button>
    <a href="./BenTheAdventurer.apk" download="BenTheAdventurer.apk" class="hud-apk-link" title="Download APK">
      APK ⬇
    </a>
  </header>"""

if old_top_hud in html:
    html = html.replace(old_top_hud, new_top_hud, 1)
    print("Updated top HUD with audio and ad buttons.")
else:
    print("Exact old_top_hud not matched, searching with regex...")
    pattern = r'<header id="topHud" class="top-hud">.*?</header>'
    html = re.sub(pattern, new_top_hud, html, flags=re.DOTALL)
    print("Substituted top HUD via regex.")

# 3. Update Bottom Banner
old_banner = """    <!-- Bottom Ad Banner Space (AdMob pub-9786472984542330 / Unit: 6300978111) -->
    <div class="admob-banner" onclick="claimAdReward()">
      <div style="display:flex; align-items:center; gap:6px;">
        <span class="ad-tag">AdMob</span>
        <span style="color:#fff; font-weight:bold;">pub-9786472984542330</span>
      </div>
      <span id="adRewardText" style="color:#00f0ff;">Tap for +50 Coins (Unit: 6300978111)</span>
    </div>"""

new_banner = """    <!-- Bottom Ad Banner Space (AdMob pub-9786472984542330 / Unit: 6300978111) -->
    <div class="admob-banner" id="admobBannerBox" onclick="showWebInterstitial('banner')">
      <ins class="adsbygoogle"
           style="display:inline-block;width:320px;height:50px"
           data-ad-client="ca-pub-9786472984542330"
           data-ad-slot="6300978111"
           data-ad-format="horizontal"></ins>
      <div id="admobFallbackContent" class="admob-fallback-content">
        <div class="admob-badge-group">
          <span class="ad-tag">AdMob</span>
          <span class="ad-pub-text">pub-9786472984542330</span>
        </div>
        <div class="admob-ticker">
          <span id="adRewardText">📺 Sponsored: Watch Ad for +100 Coins &bull; Unit: 6300978111</span>
        </div>
        <button type="button" class="btn-ad-action">WATCH AD 🎬</button>
      </div>
    </div>"""

if old_banner in html:
    html = html.replace(old_banner, new_banner, 1)
    print("Updated bottom banner HTML.")
else:
    print("Searching banner via regex...")
    pattern = r'<!-- Bottom Ad Banner Space.*?<div class="admob-banner".*?</div>\s*</div>'
    # Fallback replace
    html = re.sub(r'<div class="admob-banner" onclick="claimAdReward\(\)">.*?</div>', new_banner, html, flags=re.DOTALL)
    print("Substituted bottom banner.")

# 4. Add Interstitial Ad Modal and Revive Buttons to Game Over / Victory Screens
old_game_over_btn = '<button class="btn-start" onclick="startGame()">PLAY AGAIN</button>'
new_game_over_btn = """<button type="button" class="btn-ad-revive" onclick="showWebInterstitial('revive')">📺 WATCH AD TO REVIVE (+3 HEARTS)</button>
    <button class="btn-start" onclick="startGame()">PLAY AGAIN</button>"""

if old_game_over_btn in html:
    html = html.replace(old_game_over_btn, new_game_over_btn, 1)
    print("Added Revive Ad button to Game Over Screen.")

old_victory_btn = '<button class="btn-start" onclick="nextLevel()">NEXT LEVEL ▶</button>'
new_victory_btn = """<button type="button" class="btn-ad-revive" style="background:linear-gradient(135deg, #f59e0b, #d97706);" onclick="showWebInterstitial('double_coins')">🎬 WATCH AD (DOUBLE COINS x2)</button>
    <button class="btn-start" onclick="nextLevel()">NEXT LEVEL ▶</button>"""

if old_victory_btn in html:
    html = html.replace(old_victory_btn, new_victory_btn, 1)
    print("Added Double Coins Ad button to Victory Screen.")

# Insert Interstitial Modal before </body>
interstitial_modal_html = """
<!-- Google AdMob Full-Screen Interstitial Ad Modal (pub-9786472984542330 / Unit: 6300978111) -->
<div id="interstitialModal" class="interstitial-overlay" style="display:none;">
  <div class="interstitial-card">
    <div class="interstitial-header">
      <div style="display:flex; align-items:center; gap:8px;">
        <span class="ad-tag">Google AdMob</span>
        <span style="font-size:10px; color:#94a3b8;">pub-9786472984542330 &bull; Unit: 6300978111</span>
      </div>
      <button type="button" id="btnSkipAd" class="btn-skip-ad" onclick="closeInterstitialAd()" disabled>Skip in 5s</button>
    </div>
    <div class="interstitial-body">
      <div class="interstitial-sponsor-badge">⭐ SPONSORED GAME SHOWCASE</div>
      <h2 id="interstitialTitle" style="color:#00f0ff; margin:10px 0 6px 0; font-size:20px; font-weight:900;">BEN THE ADVENTURER: ISLAND RESCUE</h2>
      <p id="interstitialDesc" style="color:#cbd5e1; font-size:12px; max-width:380px; margin:0 auto 12px auto; line-height:1.5;">
        Laruin ang lahat ng 1,000 Levels sa Android! May Cloud Save, Dragon Boss, at Island Rewards!
      </p>
      <div class="interstitial-visual-box">
        <div class="interstitial-animated-icon">🌴 🐉 👑 🪙</div>
        <div id="interstitialRewardBadge" style="color:#fde047; font-weight:bold; font-size:13px; margin-top:8px;">
          REWARD: +100 GOLD COINS &amp; EXTRA HEART
        </div>
      </div>
      <button type="button" class="btn-interstitial-cta" onclick="claimInterstitialReward()">
        🎁 CLAIM REWARD (+100 COINS)
      </button>
    </div>
  </div>
</div>
"""
html = html.replace("</body>", interstitial_modal_html + "\n</body>", 1)

# 5. Overhaul RetroAudio and Add Interstitial JS logic
# Find RetroAudio class definition and replace with comprehensive, robust version
old_audio_block = """  class RetroAudio {
    constructor() {
      this.ctx = null;
      this.bgmTimer = null;
      this.step = 0;
      this.melody = [261.63, 0, 329.63, 392.00, 523.25, 0, 392.00, 0, 349.23, 0, 440.00, 523.25, 587.33, 0, 523.25, 0];
    }
    init() {
      if (!this.ctx) {
        const AudioCtx = window.AudioContext || window.webkitAudioContext;
        if (AudioCtx) this.ctx = new AudioCtx();
      }
      if (this.ctx && this.ctx.state === 'suspended') {
        this.ctx.resume();
      }
    }
    tone(freq, type, dur, vol = 0.18) {
      this.init();
      if (!this.ctx) return;
      try {
        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();
        osc.type = type;
        osc.frequency.setValueAtTime(freq, this.ctx.currentTime);
        gain.gain.setValueAtTime(vol, this.ctx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + dur);
        osc.connect(gain);
        gain.connect(this.ctx.destination);
        osc.start();
        osc.stop(this.ctx.currentTime + dur);
      } catch (e) {}
    }
    jump() {
      this.init();
      if (!this.ctx) return;
      try {
        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();
        osc.type = 'sine';
        osc.frequency.setValueAtTime(180, this.ctx.currentTime);
        osc.frequency.exponentialRampToValueAtTime(540, this.ctx.currentTime + 0.14);
        gain.gain.setValueAtTime(0.18, this.ctx.currentTime);
        gain.gain.linearRampToValueAtTime(0.01, this.ctx.currentTime + 0.14);
        osc.connect(gain);
        gain.connect(this.ctx.destination);
        osc.start();
        osc.stop(this.ctx.currentTime + 0.14);
      } catch (e) {}
    }
    slide() {
      this.tone(140, 'triangle', 0.2, 0.16);
    }
    blast() {
      this.tone(880, 'sawtooth', 0.1, 0.2);
      setTimeout(() => this.tone(320, 'square', 0.08, 0.14), 25);
    }
    eggDrop() {
      this.tone(480, 'sine', 0.1, 0.12);
    }
    splat() {
      this.tone(110, 'sawtooth', 0.15, 0.25);
    }
    dragonRoar() {
      this.tone(80, 'sawtooth', 0.35, 0.3);
      setTimeout(() => this.tone(60, 'square', 0.4, 0.25), 100);
    }
    coin() {
      this.tone(987.77, 'triangle', 0.09, 0.2);
      setTimeout(() => this.tone(1318.51, 'triangle', 0.13, 0.2), 50);
    }
    hit() {
      this.tone(120, 'sawtooth', 0.2, 0.3);
    }
    victory() {
      [523.25, 659.25, 783.99, 1046.50, 1318.51].forEach((f, i) => {
        setTimeout(() => this.tone(f, 'triangle', 0.2, 0.25), i * 80);
      });
    }
    startBgm() {
      if (this.bgmTimer) return;
      this.bgmTimer = setInterval(() => {
        if (!gameActive) return;
        const note = this.melody[this.step % this.melody.length];
        if (note > 0) this.tone(note, 'triangle', 0.08, 0.025);
        this.step++;
      }, 150);
    }
    stopBgm() {
      if (this.bgmTimer) {
        clearInterval(this.bgmTimer);
        this.bgmTimer = null;
      }
    }
  }
  const audio = new RetroAudio();"""

new_audio_block = """  // =========================================================================
  // 1. RETRO WEB AUDIO 8-BIT SYNTHESIZER (Audible, Responsive & Mobile-Unlocked)
  // =========================================================================
  class RetroAudio {
    constructor() {
      this.ctx = null;
      this.bgmTimer = null;
      this.step = 0;
      this.isMuted = false;
      this.unlocked = false;
      // Upbeat 8-bit Mario-style melody
      this.melody = [
        261.63, 0, 329.63, 392.00, 523.25, 0, 392.00, 0,
        349.23, 0, 440.00, 523.25, 587.33, 0, 523.25, 0,
        392.00, 0, 440.00, 0, 493.88, 523.25, 0, 392.00,
        329.63, 0, 261.63, 0, 293.66, 329.63, 261.63, 0
      ];
      this.bassline = [
        130.81, 0, 130.81, 0, 164.81, 0, 164.81, 0,
        174.61, 0, 174.61, 0, 196.00, 0, 196.00, 0
      ];
    }
    init() {
      try {
        if (!this.ctx) {
          const AudioCtx = window.AudioContext || window.webkitAudioContext;
          if (AudioCtx) this.ctx = new AudioCtx();
        }
        if (this.ctx && this.ctx.state === 'suspended') {
          this.ctx.resume().then(() => {
            this.unlocked = true;
          }).catch(() => {});
        } else if (this.ctx && this.ctx.state === 'running') {
          this.unlocked = true;
        }
      } catch (e) {
        console.warn("AudioContext init error:", e);
      }
    }
    toggleMute() {
      this.isMuted = !this.isMuted;
      if (this.isMuted) {
        this.stopBgm();
      } else {
        this.init();
        if (typeof gameActive !== 'undefined' && gameActive) {
          this.startBgm();
        }
      }
      return this.isMuted;
    }
    tone(freq, type, dur, vol = 0.20) {
      if (this.isMuted) return;
      this.init();
      if (!this.ctx) return;
      try {
        const now = this.ctx.currentTime;
        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();
        osc.type = type;
        osc.frequency.setValueAtTime(freq, now);
        gain.gain.setValueAtTime(Math.max(0.001, vol), now);
        gain.gain.exponentialRampToValueAtTime(0.0001, now + dur);
        osc.connect(gain);
        gain.connect(this.ctx.destination);
        osc.start(now);
        osc.stop(now + dur + 0.05);
      } catch (e) {}
    }
    jump() {
      if (this.isMuted) return;
      this.init();
      if (!this.ctx) return;
      try {
        const now = this.ctx.currentTime;
        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();
        osc.type = 'square';
        osc.frequency.setValueAtTime(160, now);
        osc.frequency.exponentialRampToValueAtTime(580, now + 0.15);
        gain.gain.setValueAtTime(0.24, now);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 0.15);
        osc.connect(gain);
        gain.connect(this.ctx.destination);
        osc.start(now);
        osc.stop(now + 0.16);
      } catch (e) {}
    }
    slide() {
      if (this.isMuted) return;
      this.init();
      if (!this.ctx) return;
      try {
        const now = this.ctx.currentTime;
        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();
        osc.type = 'triangle';
        osc.frequency.setValueAtTime(160, now);
        osc.frequency.linearRampToValueAtTime(70, now + 0.18);
        gain.gain.setValueAtTime(0.25, now);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 0.18);
        osc.connect(gain);
        gain.connect(this.ctx.destination);
        osc.start(now);
        osc.stop(now + 0.19);
      } catch (e) {}
    }
    blast() {
      if (this.isMuted) return;
      this.init();
      if (!this.ctx) return;
      try {
        const now = this.ctx.currentTime;
        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();
        osc.type = 'sawtooth';
        osc.frequency.setValueAtTime(880, now);
        osc.frequency.exponentialRampToValueAtTime(180, now + 0.12);
        gain.gain.setValueAtTime(0.26, now);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 0.12);
        osc.connect(gain);
        gain.connect(this.ctx.destination);
        osc.start(now);
        osc.stop(now + 0.13);
      } catch (e) {}
    }
    eggDrop() {
      this.tone(480, 'sine', 0.12, 0.20);
    }
    splat() {
      this.tone(90, 'sawtooth', 0.18, 0.30);
    }
    dragonRoar() {
      if (this.isMuted) return;
      this.init();
      if (!this.ctx) return;
      try {
        const now = this.ctx.currentTime;
        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();
        osc.type = 'sawtooth';
        osc.frequency.setValueAtTime(90, now);
        osc.frequency.linearRampToValueAtTime(45, now + 0.40);
        gain.gain.setValueAtTime(0.35, now);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 0.40);
        osc.connect(gain);
        gain.connect(this.ctx.destination);
        osc.start(now);
        osc.stop(now + 0.42);
      } catch (e) {}
    }
    coin() {
      if (this.isMuted) return;
      this.tone(987.77, 'sine', 0.08, 0.30);
      setTimeout(() => { if (!this.isMuted) this.tone(1318.51, 'sine', 0.14, 0.30); }, 60);
    }
    hit() {
      if (this.isMuted) return;
      this.tone(110, 'sawtooth', 0.22, 0.35);
    }
    victory() {
      if (this.isMuted) return;
      [523.25, 659.25, 783.99, 1046.50, 1318.51].forEach((f, i) => {
        setTimeout(() => { if (!this.isMuted) this.tone(f, 'triangle', 0.22, 0.30); }, i * 85);
      });
    }
    startBgm() {
      if (this.isMuted || this.bgmTimer) return;
      this.init();
      this.bgmTimer = setInterval(() => {
        if (!gameActive || this.isMuted) return;
        const leadNote = this.melody[this.step % this.melody.length];
        const bassNote = this.bassline[this.step % this.bassline.length];
        if (leadNote > 0) this.tone(leadNote, 'square', 0.09, 0.13);
        if (bassNote > 0 && this.step % 2 === 0) this.tone(bassNote, 'triangle', 0.12, 0.12);
        this.step++;
      }, 160);
    }
    stopBgm() {
      if (this.bgmTimer) {
        clearInterval(this.bgmTimer);
        this.bgmTimer = null;
      }
    }
  }
  const audio = new RetroAudio();

  // Global user gesture audio unlocker for mobile browsers
  function unlockGlobalAudio() {
    audio.init();
    if (audio.ctx && audio.ctx.state === 'running') {
      audio.unlocked = true;
      try {
        const osc = audio.ctx.createOscillator();
        const gain = audio.ctx.createGain();
        gain.gain.value = 0.0001;
        osc.connect(gain);
        gain.connect(audio.ctx.destination);
        osc.start();
        osc.stop(audio.ctx.currentTime + 0.001);
      } catch (e) {}
    }
    window.removeEventListener('pointerdown', unlockGlobalAudio);
    window.removeEventListener('touchstart', unlockGlobalAudio);
    window.removeEventListener('click', unlockGlobalAudio);
    window.removeEventListener('keydown', unlockGlobalAudio);
  }
  window.addEventListener('pointerdown', unlockGlobalAudio, { passive: true });
  window.addEventListener('touchstart', unlockGlobalAudio, { passive: true });
  window.addEventListener('click', unlockGlobalAudio, { passive: true });
  window.addEventListener('keydown', unlockGlobalAudio, { passive: true });

  function toggleAudio() {
    const isMuted = audio.toggleMute();
    const btn = document.getElementById('hudSoundToggle');
    if (btn) {
      btn.innerText = isMuted ? '🔇 Sound: OFF' : '🔊 Sound: ON';
      btn.style.color = isMuted ? '#f87171' : '#fff';
    }
  }"""

if old_audio_block in html:
    html = html.replace(old_audio_block, new_audio_block, 1)
    print("Replaced RetroAudio block successfully.")
else:
    print("Exact old_audio_block not matched, attempting substring replacement...")
    start_str = "class RetroAudio {"
    end_str = "const audio = new RetroAudio();"
    s_idx = html.find(start_str)
    e_idx = html.find(end_str) + len(end_str)
    if s_idx != -1 and e_idx != -1:
        html = html[:s_idx] + new_audio_block.replace("  // =========================================================================\n  // 1. RETRO WEB AUDIO 8-BIT SYNTHESIZER (Audible, Responsive & Mobile-Unlocked)\n  // =========================================================================\n  ", "") + html[e_idx:]
        print("Substituted RetroAudio via string slice.")

# 6. Ensure nextLevel() calls audio.startBgm()
old_next_level = """    eggs = [];
    updateHUD();
    requestAnimationFrame(gameLoop);
  }"""

new_next_level = """    eggs = [];
    updateHUD();
    audio.startBgm();
    requestAnimationFrame(gameLoop);
  }"""

if old_next_level in html:
    html = html.replace(old_next_level, new_next_level, 1)
    print("Added audio.startBgm() in nextLevel().")

# 7. Add Interstitial Ad Controller & AdSense init functions
interstitial_js = """
  // =========================================================================
  // 1B. GOOGLE ADMOB & ADSENSE CONTROLLER (Banner & Interstitial)
  // =========================================================================
  let interstitialTimer = null;
  let interstitialSecondsLeft = 5;
  let currentAdTrigger = 'banner';

  function showWebInterstitial(trigger = 'banner') {
    currentAdTrigger = trigger;
    audio.init();
    const modal = document.getElementById('interstitialModal');
    const skipBtn = document.getElementById('btnSkipAd');
    const titleEl = document.getElementById('interstitialTitle');
    const descEl = document.getElementById('interstitialDesc');
    const badgeEl = document.getElementById('interstitialRewardBadge');

    if (trigger === 'revive') {
      titleEl.innerText = "REVIVE BEN: EXTRA HEARTS 🛡️";
      descEl.innerText = "Panoorin ang maikling AdMob showcase para mabuhay muli si Ben at makakuha ng +3 Hearts!";
      badgeEl.innerText = "REWARD: REVIVE +3 HEARTS & +50 COINS";
    } else if (trigger === 'double_coins') {
      titleEl.innerText = "VICTORY BONUS: DOUBLE COINS ⭐";
      descEl.innerText = "Panoorin ang AdMob ad para madoble ang lahat ng nakuha mong coins ngayong Level!";
      badgeEl.innerText = "REWARD: 2X COINS MULTIPLIER";
    } else {
      titleEl.innerText = "BEN THE ADVENTURER: SEASON PASS 🌴";
      descEl.innerText = "Google AdMob Authorized Publisher pub-9786472984542330 • Ad Unit 6300978111";
      badgeEl.innerText = "REWARD: +100 GOLD COINS & EXTRA HEART";
    }

    interstitialSecondsLeft = 5;
    skipBtn.disabled = true;
    skipBtn.classList.remove('enabled');
    skipBtn.innerText = `Skip in ${interstitialSecondsLeft}s`;

    modal.style.display = 'flex';

    if (interstitialTimer) clearInterval(interstitialTimer);
    interstitialTimer = setInterval(() => {
      interstitialSecondsLeft--;
      if (interstitialSecondsLeft > 0) {
        skipBtn.innerText = `Skip in ${interstitialSecondsLeft}s`;
      } else {
        clearInterval(interstitialTimer);
        interstitialTimer = null;
        skipBtn.disabled = false;
        skipBtn.classList.add('enabled');
        skipBtn.innerText = "Skip Ad ✕";
      }
    }, 1000);
  }

  function closeInterstitialAd() {
    if (interstitialTimer) {
      clearInterval(interstitialTimer);
      interstitialTimer = null;
    }
    const modal = document.getElementById('interstitialModal');
    if (modal) modal.style.display = 'none';
  }

  function claimInterstitialReward() {
    closeInterstitialAd();
    audio.coin();

    if (currentAdTrigger === 'revive') {
      lives = Math.max(lives + 3, 5);
      gameActive = true;
      document.getElementById('gameOverScreen').style.display = 'none';
      player.x = Math.max(80, player.x - 100);
      player.y = groundY - player.height;
      player.vx = 0;
      player.vy = 0;
      player.invulnerable = 120;
      audio.startBgm();
      requestAnimationFrame(gameLoop);
    } else if (currentAdTrigger === 'double_coins') {
      coins = coins * 2;
    } else {
      coins += 100;
      lives = Math.min(lives + 1, 10);
    }

    updateHUD();
    syncToFirestore();

    const bannerText = document.getElementById('adRewardText');
    if (bannerText) {
      bannerText.innerText = "Rewarded! +100 Coins ⭐ (Unit: 6300978111)";
      bannerText.style.color = "#4ade80";
      setTimeout(() => {
        bannerText.innerText = "📺 Sponsored: Watch Ad for +100 Coins • Unit: 6300978111";
        bannerText.style.color = "#00f0ff";
      }, 4000);
    }
  }

  // Auto-initialize Google AdSense banner and handle review fallback
  window.addEventListener('load', () => {
    try {
      (window.adsbygoogle = window.adsbygoogle || []).push({});
    } catch (e) {
      console.log("AdSense initialized or pending domain approval.");
    }
  });
"""

html = html.replace("const SAVE_KEY = 'ben_adventurer_lvl_save';", interstitial_js + "\n  const SAVE_KEY = 'ben_adventurer_lvl_save';", 1)

# Write updated index.html
with open("index.html", "w") as f:
    f.write(html)
print("Updated index.html successfully.")

# Write updated docs/index.html (keep identical for GitHub Pages)
shutil.copyfile("index.html", "docs/index.html")
print("Synchronized docs/index.html with index.html.")
