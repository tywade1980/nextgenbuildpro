// Firebase configuration and initialization
// This file provides the Firebase Firestore instance used by CatalogueDataService

import { initializeApp, getApps, getApp } from 'firebase/app';
import { getFirestore } from 'firebase/firestore';

// Firebase configuration object - uses environment variables with validation
const firebaseConfig = {
  apiKey: process.env.REACT_APP_FIREBASE_API_KEY || (() => {
    console.warn('Firebase API key not configured. Set REACT_APP_FIREBASE_API_KEY environment variable.');
    return "demo-api-key";
  })(),
  authDomain: process.env.REACT_APP_FIREBASE_AUTH_DOMAIN || "nextgenbuildpro-demo.firebaseapp.com",
  projectId: process.env.REACT_APP_FIREBASE_PROJECT_ID || "nextgenbuildpro-demo",
  storageBucket: process.env.REACT_APP_FIREBASE_STORAGE_BUCKET || "nextgenbuildpro-demo.appspot.com",
  messagingSenderId: process.env.REACT_APP_FIREBASE_MESSAGING_SENDER_ID || "123456789000",
  appId: process.env.REACT_APP_FIREBASE_APP_ID || "1:123456789000:web:demo"
};

// Validate configuration
const validateFirebaseConfig = () => {
  const requiredKeys = ['apiKey', 'authDomain', 'projectId'];
  const missingKeys = requiredKeys.filter(key => 
    !firebaseConfig[key as keyof typeof firebaseConfig] || 
    firebaseConfig[key as keyof typeof firebaseConfig].includes('demo') ||
    firebaseConfig[key as keyof typeof firebaseConfig].includes('your-')
  );
  
  if (missingKeys.length > 0) {
    console.warn(`Firebase configuration incomplete. Missing or placeholder values for: ${missingKeys.join(', ')}`);
    console.warn('Please set proper environment variables for production use.');
  }
  
  return missingKeys.length === 0;
};

// Check configuration on load
const isConfigured = validateFirebaseConfig();

// Initialize Firebase (avoid double initialization)
const app = getApps().length > 0 ? getApp() : initializeApp(firebaseConfig);

// Initialize Firestore and export it
export const firestore = getFirestore(app);

// Export the app instance for other Firebase services if needed
export default app;

// Export configuration status for debugging
export { isConfigured as isFirebaseConfigured, validateFirebaseConfig };
