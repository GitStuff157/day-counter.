# Days Until I Can Quit — Android app

A friendly rebuild of days_counter.py as a native Android app.

## What it does
- Starts a persistent clock the first time you open the app (stored in
  SharedPreferences, survives app restarts — like the original counter_data.json).
- Shows days passed out of 5445 (15 years), a progress bar, percentage,
  days remaining, start date and estimated finish date.
- Optional timezone picker (top button) — affects how dates are *displayed*;
  the day count itself is based on real elapsed time, same as the original script.
- "Reset start date to now" (bottom, asks for confirmation) if you want to restart the clock.
- Shows a milestone message once you hit day 5445.

## How to build the APK
This project has zero third-party dependencies beyond androidx.core and
androidx.appcompat — both already in your local Gradle cache — so it should
build fully offline, no downloads needed.

1. Open Android Studio → Open → select this `DaysCounter` folder.
2. Let it sync (should be instant / offline since everything's cached).
3. Build → Build Bundle(s) / APK(s) → Build APK(s).
4. Find it at `app/build/outputs/apk/debug/app-debug.apk`.

Or from a terminal with the SDK on PATH:
```
cd DaysCounter
gradle assembleDebug
```
(If you don't have the `gradle` command, let Android Studio create the
`gradlew` wrapper for you on first open — it'll prompt automatically.)

## Config
- `MainActivity.kt` — all the logic, `MAX_DAYS = 5445` at the top if you
  ever want to change the target.
- `TIMEZONE_CHOICES` — the curated list in the timezone dialog; add more
  IANA zone IDs there if you want others.
- minSdk 26 (Android 8.0+, ~2017 onward) — covers the vast majority of
  active devices while keeping java.time available with no extra libraries.
