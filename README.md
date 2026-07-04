# War Sound

Minimal Android app that recreates the feel of a military-style soundboard without copying a specific app's assets.

## Included

- Air raid loop
- Radio chatter pulse pattern
- Battle alarm
- March beat
- Morse-style signal
- Stop button

The app uses `ToneGenerator` and vibration patterns, so it works without bundling copyrighted sound files.

## Structure

- `app/src/main/java/com/example/warsound/MainActivity.kt`: playback logic
- `app/src/main/res/layout/activity_main.xml`: main screen
- `app/src/main/res/values/themes.xml`: visual theme

## Build

Open the folder in Android Studio and let it create/download the Gradle wrapper if needed, then run the `app` configuration.
