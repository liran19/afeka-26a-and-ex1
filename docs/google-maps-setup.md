# Getting Google Maps to Work

So you want to see the actual map with pins showing where people played from? Cool. Here's what you need to do.

## Why do I need this?

The app saves the GPS location when you finish a game. The Top 10 screen shows a map with markers for each score. But Google Maps won't work without an API key (it's free, just needs a Google account).

## Get the API Key

### 1. Go to Google Cloud Console
Open https://console.cloud.google.com/ in your browser.

### 2. Create a project
- Click the project dropdown at the top
- "New Project"
- Give it a name (whatever you want)
- Click Create

### 3. Enable Maps
- In the search bar, type "Maps SDK for Android"
- Click it and press "Enable"
- Wait a few seconds

### 4. Get your key
- Go to "Credentials" in the left menu
- Click "Create Credentials" → "API key"
- Copy the key that appears (starts with `AIza...`)

That's it! You now have an API key.

## Put it in the app (Secure Method)

Open `local.properties` file in your project root (same folder as `build.gradle.kts`).

Add this line at the bottom:

```properties
MAPS_API_KEY="HERE"
```

Replace the example key with your actual key from Google Cloud Console.

**Why local.properties?**
- It's automatically ignored by Git (keeps your key private)
- Each team member can use their own key
- More secure than hardcoding in AndroidManifest.xml

## Rebuild and run

1. **Sync Gradle**: Click "Sync Now" in Android Studio (or File → Sync Project with Gradle Files)
2. **Rebuild**: Build → Rebuild Project
3. **Run**: The map should work now!

**Note**: The app reads the key from `local.properties` and injects it into the AndroidManifest during build time.

## What if I don't add a key?

The app still works fine! You just get a gray map area instead of an interactive Google Map. All the location data still saves, you just can't visualize it nicely.

