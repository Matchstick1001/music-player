# Music Player (Kotlin + Jetpack Compose)

This scaffold implements a modern Android music player using Kotlin, Jetpack Compose, and Media3 ExoPlayer.

Features included in scaffold:
- MediaStore scanning for audio files
- ExoPlayer wrapper (`PlayerManager`) for play/pause/seek
- Background `PlayerService` skeleton using Media3 session service
- Compose UI: All Songs screen, simple navigation, now playing bar
- Coil for album art
- Material3 dark theme with glass-like color scheme

Next steps you can ask me to do:
- Wire up MediaSessionConnector and full notification controls
- Implement Playlists persistence (Room)
- Improve album art lookup and caching
- Add animations and glassmorphism blur effects

To build: open in Android Studio, sync Gradle, run on device (ensure runtime audio permission granted).

Build APK (quick guide)

- Using Android Studio: Open the project, let Gradle sync, then Build > Build Bundle(s) / APK(s) > Build APK(s). The generated APK will appear in the Build output or under `app/build/outputs/apk`.

- Using command line (if you have Gradle wrapper or Gradle installed):

```bash
# From project root
./gradlew assembleDebug    # debug APK
./gradlew assembleRelease  # signed release APK (uses debug signing in this scaffold)
```

Notes:
- This scaffold configures a testable release build that uses the default debug signing for convenience. Replace the signing configuration in `app/build.gradle` with your own keystore for Play distribution.
- If you see resource or build errors, open the project in Android Studio to auto-fix SDK/Gradle toolchain settings.

CI: Firebase App Distribution

This repo includes a GitHub Actions workflow that builds the Android App Bundle and uploads it to Firebase App Distribution: `.github/workflows/firebase-distribute.yml`.

Secrets required (GitHub repository secrets):
- `FIREBASE_TOKEN` — generate via `firebase login:ci` on your machine and copy the token.
- `FIREBASE_APP_ID` — your Firebase Android App ID (format like `1:1234567890:android:abcdef12345`).
- `FIREBASE_GROUPS` (optional) — comma-separated tester groups configured in Firebase App Distribution (e.g. `qa,internal`).

How the workflow works:
1. On push to `main` (or manual run), the workflow checks out the repo and sets up JDK.
2. It runs `./gradlew bundleRelease` to produce `app-release.aab`.
3. It installs the Firebase CLI and runs `firebase appdistribution:distribute` using the provided secrets to upload the bundle.

Generating `FIREBASE_TOKEN`:
1. Install Firebase CLI (`npm i -g firebase-tools`).
2. Run `firebase login:ci` and copy the token printed in the terminal.
3. Add it to your GitHub repository secrets as `FIREBASE_TOKEN`.

Add `FIREBASE_APP_ID` from the Firebase console (Project settings → Your apps → App ID).

After a successful workflow run, invited testers (or groups) will receive the new build via Firebase App Distribution.

Publish/download directly from GitHub (optional)

This repo now includes a workflow that creates a GitHub Release and attaches built artifacts when you push a tag matching `v*` (for example `v1.0.0`). The release will contain the AAB and APK so users can download directly from GitHub Releases.

How to create a release and publish artifacts via GitHub Actions:

1. Stage and commit any changes you want included in the release:

```bash
git add .
git commit -m "release: prepare v1.0.0"
```

2. Create a semver tag and push it (replace `v1.0.0` with your tag):

```bash
git tag v1.0.0
git push origin v1.0.0
```

3. The workflow `.github/workflows/release-upload.yml` will run on the pushed tag, build `assembleRelease` and `bundleRelease`, then create a GitHub Release and attach `app-release.aab` and `app-release.apk`.

4. Visit your repository's Releases page to download the artifacts or share the release URL with users.

Notes:
- The release artifacts produced by CI are built with the signing configuration in `app/build.gradle`. For production publishing, replace the debug signing with your secure keystore and re-run the release flow.
- If you prefer to upload artifacts manually, build locally and create a release on GitHub, then use the web UI to attach the APK/AAB.
