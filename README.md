# TestAndroidGame_ActionGame3D

Offline 3D FPS prototype (LibGDX + Kotlin) — build APK on phone (Termux / IDE).

## Clone

```bash
git clone https://github.com/leccgamesandapps-art/TestAndroidGame_ActionGame3D.git
cd TestAndroidGame_ActionGame3D
```

## Build APK (Termux)

```bash
# Need: JDK + Android SDK already set up (ANDROID_HOME)
chmod +x gradlew
./gradlew :app:assembleDebug --no-daemon
```

## Install APK (copy to Download)

```bash
cp app/build/outputs/apk/debug/app-debug.apk /storage/emulated/0/Download/ActionGame3D.apk
# or:
cp app/build/outputs/apk/debug/app-debug.apk ~/storage/shared/Download/ActionGame3D.apk
termux-open /storage/emulated/0/Download/ActionGame3D.apk
```

## Controls
- WASD move, mouse look, LMB shoot, R reload, 1/2/3 weapons, Shift sprint, Ctrl crouch, Space jump

## Notes
- Offline bots only (v0.1.0)
- Native `.so` files are extracted on first build into `app/libs/`
