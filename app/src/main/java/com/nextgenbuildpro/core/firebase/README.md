# Firebase Integration for NextGenBuildPro

This document provides an overview of the Firebase integration in the NextGenBuildPro app, including Firestore and Firebase Storage.

## Setup

### 1. Firebase Project

The app is configured to use the Firebase project with the following details:
- Project ID: `nextgenbuildpro`
- Storage Bucket: `gs://nextgenbuildpro.firebasestorage.app`

### 2. Configuration Files

- `google-services.json`: This file should be placed in the app directory. It contains the Firebase configuration for the app.

### 3. Initialization

Firebase components are initialized in the `NextGenBuildProApplication` class:

```kotlin
class NextGenBuildProApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        FirestoreInitializer.initialize()
        FirebaseStorageInitializer.initialize()
    }
}
```

## Firestore

### Collections

Firestore collections are defined in the `FirestoreCollectionNames` class:

```kotlin
object FirestoreCollectionNames {
    // Main collections
    const val LEADS = "leads"
    const val ESTIMATES = "estimates"
    const val PROJECTS = "projects"
    const val CLIENTS = "clients"
    const val TASKS = "tasks"
    // ...
}
```

### Repositories

The app uses the repository pattern for Firestore operations. Each repository implements the `FirestoreRepository` interface and extends the `BaseFirestoreRepository` class.

Example:

```kotlin
class FirestoreLeadRepository(
    firestore: FirebaseFirestore = FirebaseFirestoreInitializer.firestore,
    private val storageRepository: FirebaseStorageRepository = FirebaseStorageRepository()
) : BaseFirestoreRepository<Lead, String>(firestore), LeadRepository {
    // ...
}
```

### Usage

To use a repository, inject it using the appropriate module:

```kotlin
val leadRepository = LeadRepositoryModule.provideLeadRepository()

// Get all leads
leadRepository.getLeads().collect { leads ->
    // Process leads
}

// Save a lead
val leadId = leadRepository.saveLead(lead)
```

## Firebase Storage

### Structure

Firebase Storage is organized by module and ID:

```
/leads/{leadId}/{fileName}
/estimates/{estimateId}/{fileName}
/projects/{projectId}/{fileName}
/clients/{clientId}/{fileName}
/tasks/{taskId}/{fileName}
```

### Usage

The `FirebaseStorageRepository` class provides methods for uploading, downloading, and managing files:

```kotlin
val storageRepository = FirebaseStorageRepository()

// Upload a file
val downloadUrl = storageRepository.uploadFile(
    uri = photoUri,
    module = "leads",
    id = leadId,
    contentType = "image/jpeg"
)

// Download a file
val localFile = storageRepository.downloadFile(
    downloadUrl = downloadUrl,
    destinationFile = File(context.cacheDir, "photo.jpg")
)
```

## Test Mode

Firestore is configured to run in test mode, which enables offline persistence. This allows the app to work offline and sync data when the device is back online.

```kotlin
// Create persistent cache settings
val persistentCacheSettings = PersistentCacheSettings.newBuilder()
    .setSizeBytes(104857600) // 100MB cache size (default)
    .build()

val settings = FirebaseFirestoreSettings.Builder()
    .setLocalCacheSettings(persistentCacheSettings)  // Enable offline persistence
    .build()
firestore.firestoreSettings = settings
```

## Security Rules

Firebase Security Rules should be configured to secure your data. Here's a basic example:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

For more detailed security rules, refer to the Firebase documentation.

## Troubleshooting

### Common Issues

1. **Missing google-services.json**: Ensure the file is placed in the app directory.
2. **Firestore Offline Persistence**: If you're experiencing issues with offline persistence, check that the device has enough storage space.
3. **Storage Upload Failures**: Verify that the device has the necessary permissions (e.g., READ_EXTERNAL_STORAGE).

### Logging

The Firebase components include logging to help diagnose issues:

```
FirebaseStorage: Error initializing with custom URL
FirestoreLeadRepo: Error getting leads by status
```

Check the logs for these tags to diagnose issues.
