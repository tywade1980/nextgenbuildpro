// Firebase configuration and initialization
// This file provides the Firebase Firestore instance used by CatalogueDataService

import { initializeApp, getApps, getApp } from 'firebase/app';
import { getFirestore } from 'firebase/firestore';

// Firebase configuration object - replace with your actual config
const firebaseConfig = {
  apiKey: process.env.REACT_APP_FIREBASE_API_KEY || "your-api-key",
  authDomain: process.env.REACT_APP_FIREBASE_AUTH_DOMAIN || "your-auth-domain",
  projectId: process.env.REACT_APP_FIREBASE_PROJECT_ID || "nextgenbuildpro",
  storageBucket: process.env.REACT_APP_FIREBASE_STORAGE_BUCKET || "your-storage-bucket",
  messagingSenderId: process.env.REACT_APP_FIREBASE_MESSAGING_SENDER_ID || "your-sender-id",
  appId: process.env.REACT_APP_FIREBASE_APP_ID || "your-app-id"
};

// Initialize Firebase (avoid double initialization)
const app = getApps().length > 0 ? getApp() : initializeApp(firebaseConfig);

// Initialize Firestore and export it
export const firestore = getFirestore(app);

// Export the app instance for other Firebase services if needed
export default app;