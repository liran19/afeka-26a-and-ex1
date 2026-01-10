# Space Dodger 🚀

An Android space racing game built with Kotlin. Dodge falling meteors, collect fuel, and compete on the leaderboard.

![Gameplay screenshot](screenshots/gameplay.png)

## Features

### 🎮 Game Modes
- **Button Mode**: Use on-screen buttons to switch lanes
- **Sensor Mode**: Tilt your device left/right to move
  - **Speed Boost**: Tilt forward and backward repeatedly to activate a 2-second speed boost! 🚀

### ⚡ Speed Settings
- **Normal Mode**: Standard game speed (1000ms per tick)
- **Fast Mode**: Double speed challenge (500ms per tick)
  - Earn 1.5x score multiplier for the extra difficulty!

### 🎯 Gameplay Features
- **Obstacle Course**: 8x5 grid with falling meteors
- **Power-ups**: Collect rocket fuel for +10 bonus points
- **Lives System**: 3 hearts - avoid 3 crashes or game over
- **Distance Tracker**: See how far you've traveled
- **Score System**: Distance + collected fuel = total score

### 🔊 Audio & Feedback
- **Sound Effects**: Crash sounds and coin collection noises
- **Background Music**: Ambient space music during gameplay
- **Vibration Feedback**: Feel the impact when you crash

### 🏆 Leaderboard
- **Top 10 High Scores**: Save your best runs with your name
- **GPS Location Tracking**: Records where you played
- **Interactive Map**: See where top players are from (requires Google Maps API key)
- **Score Breakdown**: View distance, coins, and mode for each entry

## How to Play

### Starting the Game
1. Launch the app and choose your control mode:
   - **Button Mode** - Tap left/right arrows
   - **Sensor Mode** - Tilt your device
2. Toggle **Fast Mode** if you want a challenge (1.5x score!)
3. Tap the mode button to start

### During Gameplay
- **Avoid meteors** by switching lanes
- **Collect rocket fuel** (the yellow icons) for bonus points
- **In Sensor Mode**: Tilt forward/backward repeatedly for a speed boost!
- Watch your lives (hearts) at the top
- Track your distance and score in real-time

### Game Over
1. Enter your name when prompted
2. Your score is automatically saved with GPS location
3. Choose what to do next:
   - **View Top 10** - See the leaderboard
   - **Play Again** - Restart with same settings
   - **Back to Menu** - Change mode or speed

## 🗺️ Google Maps Setup

The leaderboard map needs a Google Maps API key to show locations. The app works fine without it - you'll just see coordinates as text instead of an interactive map.

**To enable the map**: See [docs/google-maps-setup.md](docs/google-maps-setup.md) for instructions.

## 📱 Controls

### Button Mode
- **Left Button** (←): Move one lane left
- **Right Button** (→): Move one lane right

### Sensor Mode
- **Tilt Left**: Move to left lanes
- **Tilt Right**: Move to right lanes
- **Tilt Forward/Backward** (repeatedly): Activate 2-second speed boost! 🚀
  - Game speed doubles temporarily
  - Great for dodging tricky patterns

## 🎵 Audio System

- **Background Music**: Loops continuously during gameplay (30% volume)
- **Crash Sound**: Plays on every meteor collision
- **Coin Sound**: Plays when collecting rocket fuel
- **Auto-Pause**: Music and effects pause when you leave the app

## 📊 Scoring System

```
Base Score = Distance + (Coins × 10)
Total Score = Base Score × Speed Multiplier

Speed Multipliers:
- Normal Mode: 1.0x
- Fast Mode: 1.5x

Example Scores:
- Normal: (100 distance + 50 coins) = 150 points
- Fast: (100 distance + 50 coins) × 1.5 = 225 points
```

## 🏗️ Technical Details

- **Platform**: Android (Kotlin)
- **Min SDK**: API 24 (Android 7.0)
- **Architecture**: Activity-based with fragments
- **Sensors**: Accelerometer for tilt controls
- **Audio**: SoundPool for effects, MediaPlayer for music
- **Storage**: SharedPreferences + Gson for score persistence
- **Maps**: Google Maps SDK for location visualization
