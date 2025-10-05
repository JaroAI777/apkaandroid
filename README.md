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

## Jak to uruchomić w Android Studio

1. **Otwórz projekt.** W Android Studio wybierz **File → Open…** i wskaż katalog z tym repozytorium. Po otwarciu poczekaj, aż zakończy się synchronizacja Gradle.
2. **Zainstaluj wymagane SDK.** Jeśli pojawi się monit o brakujące komponenty (np. Android SDK 34 lub Google Play Services), pozwól Android Studio je doinstalować.
3. **Skonfiguruj urządzenie testowe.** Podłącz fizyczny telefon z włączonym `USB debugging` albo utwórz emulator w **Device Managerze** (minimum Android 7.0 / API 24).
4. **Uruchom aplikację.** Na pasku narzędzi wybierz konfigurację „app” i kliknij **Run** ▶️. Aplikacja zostanie zainstalowana i wystartuje na wybranym urządzeniu.
5. **Włącz tryb kiosk (opcjonalnie na potrzeby testów).** Aby aplikacja mogła blokować system, ustaw ją jako `device owner`:
   ```bash
   adb shell dpm set-device-owner com.tasklock.app/.device.AdminReceiver
   ```
   Po tej operacji ponownie uruchom aplikację – automatycznie przełączy się w tryb zablokowanego urządzenia.
6. **Testuj przepływ lekcji.** Odtwórz wideo, zmień ustawienia lekcji lub użyj kodu administratora, aby odblokować urządzenie.
7. **Wyłącz tryb kiosk po testach.** Jeśli chcesz wrócić do normalnego działania telefonu lub emulatora, usuń uprawnienia administratora:
   ```bash
   adb shell dpm remove-active-admin com.tasklock.app/.device.AdminReceiver
   adb shell dpm set-device-owner ''
   ```

> Jeżeli urządzenie zgłosi, że `Device owner` został już ustawiony dla innej aplikacji, zresetuj emulator (`adb emu avd wipe-data`) lub przywróć telefon do ustawień fabrycznych przed ponowną próbą.

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

## Jak opublikować repozytorium i pobrać APK

1. **Utwórz repozytorium.** Na GitHubie kliknij **New repository**, nadaj mu nazwę (np. `TaskLock`) i zostaw je puste.
2. **Powiąż lokalne źródła.** W katalogu projektu wykonaj:

   ```bash
   git remote add origin git@github.com:<twoja_nazwa_użytkownika>/TaskLock.git
   git push -u origin main
   ```

   > Jeżeli używasz HTTPS, zamień adres na `https://github.com/<twoja_nazwa_użytkownika>/TaskLock.git`.
3. **Włącz GitHub Actions.** Wejdź na kartę **Actions** w repozytorium i zaakceptuj monit „I understand my workflows, go ahead and enable them”.
4. **Uruchom workflow.** Przy każdym `git push` pipeline `Android CI` startuje automatycznie. Możesz też kliknąć **Run workflow** i wybrać gałąź `main`.
5. **Pobierz artefakt APK.** Po zakończeniu workflow:
   - Otwórz wynik uruchomienia (Run).
   - W sekcji **Artifacts** kliknij `TaskLock-apk` – pobierzesz archiwum `.zip` z plikami `app-debug.apk` i `app-release-unsigned.apk`.
6. **Zainstaluj na urządzeniu.** Rozpakuj archiwum, skopiuj `app-debug.apk` na telefon i zainstaluj je (wcześniej zezwól na instalację z nieznanych źródeł).

Po podpisaniu `app-release-unsigned.apk` własnym kluczem możesz publikować aplikację w Google Play lub dystrybuować ją użytkownikom końcowym.

## Customising the lesson

- Open **Settings** inside the kiosk experience to set a streaming URL for the lesson video and define the unlock code.
- The default lesson points to the Big Buck Bunny sample video. Replace it in the settings or change `DEFAULT_LESSON_URL` inside `LessonRepository.kt` for a different default.

## Testing checklist

- `LessonActivity` plays the configured video and unlocks the device after completion.
- `UnlockActivity` accepts the unlock code for administrator override.
- `SettingsActivity` persists lesson configuration changes.
- `KioskService` ensures lock task mode is enforced on boot and during lesson playback.

Contributions welcome! Feel free to open issues or submit PRs for enhancements.
