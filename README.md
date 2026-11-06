# Friday

A personal voice assistant for Android. Friday listens through the device's speech
recognizer, sends the transcript to Google's Gemini API, speaks the answer back, and is
built to grow local on-device tools over time.

- **Language / UI:** Kotlin + Jetpack Compose (Material 3)
- **minSdk:** 29 (Android 10) · **targetSdk / compileSdk:** 36
- **Application ID:** `com.friday.assistant`

> **Your API key is never hardcoded.** Friday ships with no credentials at all. You paste your
> own Gemini key into the in-app Settings screen, and it is stored on-device with
> `EncryptedSharedPreferences` (master key held by the Android keystore).

---

## Features in this first milestone

| Area | What exists |
| --- | --- |
| `ai` | `GeminiClient` (Gemini `generateContent` REST), `AssistantEngine`, typed wire models |
| `voice` | `SpeechRecognizerManager` (speech-to-text), `TextToSpeechManager` (text-to-speech) |
| `service` | `FridayService`, a microphone foreground service that keeps a session alive |
| `tools` | `Tool` abstraction + `ToolRegistry`, with a built-in `TimeTool` |
| `data` | `SecureKeyStore` (encrypted storage) and `SettingsRepository` |
| `ui` | Compose screens: a Home screen with push-to-talk and a Settings screen |

---

## Setup

### 1. Prerequisites

- Android Studio (or a standalone Android SDK with **platform 36** and **build-tools 36.0.0**)
- JDK 17 or newer
- `minSdk` 29+ device or emulator that has a speech recognition and a text-to-speech engine

### 2. Clone and point Gradle at your SDK

```bash
git clone <your-fork-url> friday
cd friday
```

Create `local.properties` in the project root (it is git-ignored) so Gradle can find your SDK:

```properties
sdk.dir=/absolute/path/to/your/Android/sdk
```

Alternatively export `ANDROID_HOME` instead of creating the file.

### 3. Get a Gemini API key

1. Open [Google AI Studio](https://aistudio.google.com/app/apikey).
2. Create an API key for the Gemini API.
3. Keep it somewhere safe — you will paste it into the app, not into any file in this repo.

### 4. Build and install

```bash
./gradlew assembleDebug
# or, with a device/emulator connected:
./gradlew installDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

### 5. Enter the key in the app

1. Launch Friday.
2. Tap the gear icon (top right) to open **Settings**.
3. Paste your key into **Gemini API key** and tap **Save**.
4. Go back and tap **Start listening**, then speak.

The key is written to `friday_secure_settings` inside the app's private storage, encrypted
with a keystore-backed master key. That file is also excluded from Android backup and
device-to-device transfer, because it cannot be decrypted off the original device.

---

## Project layout

```
app/src/main/java/com/friday/assistant/
├── FridayApp.kt            Application + AppContainer wiring
├── MainActivity.kt         Single Compose host activity
├── di/                     Manual dependency container
├── data/                   SecureKeyStore, SettingsRepository
├── ai/                     GeminiClient, AssistantEngine, wire models
├── voice/                  SpeechRecognizerManager, TextToSpeechManager
├── service/                FridayService (foreground session)
├── tools/                  Tool, ToolRegistry, built-in tools
└── ui/                     theme/, home/, settings/, FridayNavHost
```

---

## Useful commands

```bash
./gradlew assembleDebug        # build the debug APK
./gradlew testDebugUnitTest    # run JVM unit tests
./gradlew lintDebug            # Android lint
```

### Networking on a restricted machine

If your machine can only reach the internet through a proxy, put the proxy settings in your
**user-level** Gradle config (`~/.gradle/gradle.properties`) rather than the project file, so
they never get committed:

```properties
systemProp.http.proxyHost=127.0.0.1
systemProp.http.proxyPort=8080
systemProp.https.proxyHost=127.0.0.1
systemProp.https.proxyPort=8080
```

---

## Continuous integration

`.github/workflows/android.yml` runs on every push and pull request:

1. `./gradlew assembleDebug`
2. `./gradlew testDebugUnitTest`
3. Uploads the debug APK and the test reports as build artifacts

---

## License

MIT — see [LICENSE](LICENSE).
