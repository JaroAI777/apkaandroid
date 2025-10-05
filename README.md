# TaskLock Android Application

TaskLock is a kiosk-mode Android application that locks the device until a configured lesson video finishes playing. During the lesson the user can only place phone calls or send SMS messages. Once the lesson completes (or an admin override succeeds) the rest of the device is unlocked again.

## Project structure

- `app/` – Android application module with activities, services, and device admin configuration.
- `index.html` – Architecture documentation generated in the initial planning phase.
- `.github/workflows/android-build.yml` – Continuous integration workflow that builds installable APKs.

## Building locally

1. Install the latest [Android Studio](https://developer.android.com/studio) or the Android command-line tools.
2. Open the project directory in Android Studio and let it sync the Gradle configuration.
3. Connect a device (API level 24+) or create an emulator.
4. Build and run the **app** module. Android Studio will produce `app/build/outputs/apk/debug/app-debug.apk` which can be sideloaded on your device.

### Command-line build

If you prefer the terminal, install the Android SDK and run:

```bash
./gradlew assembleRelease
```

The resulting signed (debug keystore) APK will be in `app/build/outputs/apk/release/app-release-unsigned.apk`. Sign it with your distribution key or use `assembleDebug` for a quick debug build.

## Automated builds & download link

A GitHub Actions workflow is included. After pushing this repository to GitHub:

1. Navigate to **Actions → Android CI** and trigger the workflow (on push it runs automatically).
2. Once the job finishes, open the run summary and download the `TaskLock-apk` artifact. It contains ready-to-install `app-debug.apk` and `app-release-unsigned.apk` files that you can share.

> **Note:** This environment cannot host files or produce public download links automatically. The workflow provides a sharable artifact link within GitHub once you run it in your own repository.

## Customising the lesson

- Open **Settings** inside the kiosk experience to set a streaming URL for the lesson video and define the unlock code.
- The default lesson points to the Big Buck Bunny sample video. Replace it in the settings or change `DEFAULT_LESSON_URL` inside `LessonRepository.kt` for a different default.

## Testing checklist

- `LessonActivity` plays the configured video and unlocks the device after completion.
- `UnlockActivity` accepts the unlock code for administrator override.
- `SettingsActivity` persists lesson configuration changes.
- `KioskService` ensures lock task mode is enforced on boot and during lesson playback.

Contributions welcome! Feel free to open issues or submit PRs for enhancements.
