# Space Dodger

An Android space racing game built with Kotlin. Dodge falling meteors, collect fuel, and compete on the leaderboard.

![Gameplay screenshot](screenshots/gameplay.png)

## Features

- **Two control modes**: Buttons or tilt sensors
- **Speed settings**: Normal or Fast mode (1.5x score multiplier)
- **Power-ups**: Collect rocket fuel for bonus points
- **Lives system**: 3 chances before game over
- **Top 10 Leaderboard**: Save your high scores with GPS location
- **Interactive map**: See where top players are from (requires Google Maps API key)

## How to Play

1. Launch the app and choose your mode (Button or Sensor)
2. Toggle Fast Mode if you want a challenge
3. Dodge meteors by switching lanes
4. Collect rocket fuel for extra points
5. Survive as long as possible!


### Google Maps
The leaderboard map needs a Google Maps API key to show locations. The app works fine without it - you'll just see coordinates as text instead of an interactive map.

**To enable the map**: See [docs/google-maps-setup.md](docs/google-maps-setup.md) for instructions.
