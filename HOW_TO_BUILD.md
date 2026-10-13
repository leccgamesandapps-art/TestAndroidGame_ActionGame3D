# How to build & install APK

## 1. Clone

```bash
git clone https://github.com/leccgamesandapps-art/TestAndroidGame_ActionGame3D.git
cd TestAndroidGame_ActionGame3D
```

## 2. Build (Termux)

Need JDK + Android SDK (`ANDROID_HOME` set).

```bash
# If no gradlew:
gradle wrapper --gradle-version 8.4
chmod +x gradlew

./gradlew :app:assembleDebug --no-daemon
```

## 3. Copy APK to Download (Storage)

```bash
cp app/build/outputs/apk/debug/app-debug.apk /storage/emulated/0/Download/ActionGame3D.apk
```

Or:

```bash
cp app/build/outputs/apk/debug/app-debug.apk ~/storage/shared/Download/ActionGame3D.apk
termux-open /storage/emulated/0/Download/ActionGame3D.apk
```

Then install from the Files app.
