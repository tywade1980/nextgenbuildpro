# Android SDK Setup Instructions

## Prerequisites
You need to have Android SDK installed on your system. This usually comes with:
- Android Studio
- IntelliJ IDEA with Android plugin

## Setup Steps

### 1. Find your Android SDK location
Common locations:
- **Windows**: `C:\Users\{username}\AppData\Local\Android\Sdk`
- **macOS**: `/Users/{username}/Library/Android/sdk`
- **Linux**: `/home/{username}/Android/Sdk`
- **IntelliJ IDEA**: Check File > Project Structure > SDKs

### 2. Update local.properties
Edit the `local.properties` file in the project root and update the `sdk.dir` line:

```properties
sdk.dir=/path/to/your/android-sdk
```

### 3. For IntelliJ IDEA users
If you're using IntelliJ IDEA:
1. Go to File > Project Structure
2. Click on "SDKs" in the left panel
3. Look for "Android API XX Platform" entries
4. The SDK path will be shown in the "SDK Home Path" field
5. Copy this path to your `local.properties` file

### 4. Verify the setup
Once you've updated `local.properties`, run:
```bash
./gradlew tasks
```

This should list all available Gradle tasks without errors.