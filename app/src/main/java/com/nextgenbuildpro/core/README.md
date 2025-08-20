# Secure API Key Management in NextGenBuildPro

This document describes the approach used for securely managing API keys and other sensitive configuration values in the NextGenBuildPro application.

## Overview

API keys and other sensitive information should never be hardcoded in the source code or committed to version control. Instead, they should be stored in a secure location and loaded at runtime.

In NextGenBuildPro, we use the following approach:

1. Store API keys in the `local.properties` file, which is excluded from version control
2. Use the `ApiKeyManager` utility class to load and access these values at runtime
3. Provide default fallback values for development and testing

## Implementation Details

### 1. Storing API Keys

API keys are stored in the `local.properties` file in the project root directory. This file is automatically excluded from version control by the `.gitignore` file.

Example `local.properties` file:

```properties
# SDK location
sdk.dir=C:\\Users\\Username\\android\\android-sdk

# API Keys
google.api.key=your_google_api_key_here
firebase.storage.url=gs://your-firebase-storage-bucket.appspot.com
```

### 2. Loading API Keys

The `ApiKeyManager` class is responsible for loading the API keys from the `local.properties` file. It is initialized in the `NextGenBuildProApplication` class during application startup.

```kotlin
// In NextGenBuildProApplication.kt
override fun onCreate() {
    super.onCreate()
    
    // Initialize API Key Manager
    initializeApiKeyManager()
    
    // Initialize Firebase
    initializeFirebase()
}

private fun initializeApiKeyManager() {
    try {
        val success = ApiKeyManager.initialize(this)
        if (success) {
            Log.d(TAG, "API Key Manager initialized successfully")
        } else {
            Log.w(TAG, "API Key Manager initialization failed, using default values")
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error initializing API Key Manager", e)
    }
}
```

### 3. Accessing API Keys

The `ApiKeyManager` provides methods to access the API keys:

```kotlin
// Get the Google API key
val googleApiKey = ApiKeyManager.getGoogleApiKey()

// Get the Firebase Storage URL
val firebaseStorageUrl = ApiKeyManager.getFirebaseStorageUrl()

// Get any property by key
val customValue = ApiKeyManager.getProperty("custom.key", "default_value")
```

### 4. Fallback Values

If an API key is not found in the `local.properties` file, the `ApiKeyManager` will return `null`. Components that use these values should provide default fallback values for development and testing.

Example:

```kotlin
// In FirebaseStorageInitializer.kt
private const val DEFAULT_FIREBASE_STORAGE_URL = "gs://nextgenbuildpro.firebasestorage.app"

fun initialize(): FirebaseStorage {
    // Get the storage bucket URL from ApiKeyManager or use the default
    val bucketUrl = ApiKeyManager.getFirebaseStorageUrl() ?: DEFAULT_FIREBASE_STORAGE_URL
    
    if (ApiKeyManager.getFirebaseStorageUrl() == null) {
        Log.w(TAG, "Firebase Storage URL not found in ApiKeyManager, using default URL")
    }
    
    // Use the bucket URL
    return FirebaseStorage.getInstance(bucketUrl)
}
```

## Security Considerations

1. **Never commit API keys to version control**: The `local.properties` file should be listed in `.gitignore` to prevent it from being committed.
2. **Use different API keys for development and production**: Each environment should have its own API keys.
3. **Restrict API key usage**: Configure API key restrictions in the service provider's console (e.g., Google Cloud Console).
4. **Obfuscate the code**: Use ProGuard/R8 to obfuscate the code and make it harder to extract API keys.
5. **Consider additional encryption**: For highly sensitive keys, consider encrypting them in the `local.properties` file and decrypting them at runtime.

## Adding New API Keys

To add a new API key:

1. Add the key to your `local.properties` file:
   ```properties
   new.api.key=your_new_api_key_here
   ```

2. Access the key in your code:
   ```kotlin
   val newApiKey = ApiKeyManager.getProperty("new.api.key")
   ```

3. Provide a default fallback value if needed:
   ```kotlin
   val newApiKey = ApiKeyManager.getProperty("new.api.key") ?: "default_value"
   ```

## Distribution

When distributing the app to other developers, provide a template `local.properties.template` file with placeholder values:

```properties
# SDK location
sdk.dir=/path/to/your/android/sdk

# API Keys
google.api.key=YOUR_GOOGLE_API_KEY
firebase.storage.url=gs://YOUR_FIREBASE_STORAGE_BUCKET.appspot.com
```

Instruct developers to copy this file to `local.properties` and replace the placeholder values with their own API keys.