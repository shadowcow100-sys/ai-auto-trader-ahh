# AutoTrader Ahh — Android Companion App

Native Android (Kotlin + Jetpack Compose) companion app for **Passive Income Ahh** (`server/` Go backend). Designed to connect your phone to the PC trading server over local Wi-Fi (LAN), Tailscale/ZeroTier, or HTTPS reverse proxy.

## Features

- **PC ↔ Phone Pairing (`ConnectScreen`)**:
  - Save multiple PC server profiles (e.g., Home PC, VPS, Tailscale node)
  - Latency & health test (`GET /api/health`) and `ACCESS_PASSKEY` verification (`POST /api/auth/verify`)
  - Quick presets for LAN IP, Tailscale, and Android Emulator (`10.0.2.2:8080`)
  - Deep-link pairing support (`autotrader://connect?url=http://192.168.1.50:8080&key=...&name=MyPC`)
  - `network_security_config.xml` enabled for local HTTP LAN connections
- **Live Dashboard**:
  - Switch between traders, **Start / Stop / Resume** trading
  - Real-time Account Equity, Wallet Balance, Available Margin, Unrealized PnL
  - Native Equity Curve sparkline chart (`GET /api/equity-history`)
  - Open Positions with leverage, entry/mark price, PnL, and ROE %
  - Real-time SSE trade/decision/error alert banner (`GET /api/events`)
- **Activity & AI**:
  - Parsed AI Decisions with reasoning, confidence, leverage, SL/TP
  - Trade History & Win-Rate / PnL statistics
  - Multi-AI Debate sessions with Start/Stop controls
- **Strategies**:
  - Browse strategies, activate a strategy live, and run **AI Smart Find** pair discovery
- **Console & Settings**:
  - Live SSE server log stream (`GET /api/logs/stream`) with keyword filtering
  - View/update global PC settings (`GET/PUT /api/settings`) and switch or disconnect PCs

## Connecting PC & Phone Later

1. **Start the PC Go Server**:
   ```bash
   cd server
   go run main.go
   ```
   The server binds to `:8080` across all network interfaces.

2. **Find your PC's address**:
   - **Same Wi-Fi (LAN)**: Run `ipconfig` (Windows) or `ip addr` / `ifconfig` (Linux/macOS) to get your PC's local IP (e.g. `192.168.1.50`). Allow inbound TCP port `8080` in your OS firewall.
   - **Anywhere (Tailscale)**: Run Tailscale on both PC and Android and use the PC's `100.x.y.z` IP or MagicDNS URL.
   - **Android Studio Emulator**: Use `http://10.0.2.2:8080`.

3. **Enter Passkey (if configured)**:
   If `ACCESS_PASSKEY` is set in `server/.env`, enter the same passkey in the Android app's Connect screen. The app sends it via the `X-Access-Key` header.

## Building in Android Studio

1. Open the `android/` folder in **Android Studio** (Ladybug or newer recommended, JDK 17).
2. Let Gradle sync download dependencies (Kotlin 2.0.21, Compose BOM, Retrofit, OkHttp, DataStore).
3. Run on a physical Android phone (Android 8.0 / API 26+) or emulator, or build an APK from **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
