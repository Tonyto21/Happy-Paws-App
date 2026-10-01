import { initializeApp, getApps, getApp } from 'firebase/app';
import { getAuth, connectAuthEmulator } from 'firebase/auth';
import {
  initializeFirestore,
  persistentLocalCache,
  persistentMultipleTabManager,
  connectFirestoreEmulator,
  getFirestore
} from 'firebase/firestore';
import { getStorage, connectStorageEmulator } from 'firebase/storage';

const projectId = import.meta.env.VITE_FIREBASE_PROJECT_ID || 'demo-happypaws-liberia';

const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY || 'AIzaSyDemoKeyForHappyPawsLiberiaRescue01',
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN || `${projectId}.firebaseapp.com`,
  projectId: projectId,
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET || `${projectId}.appspot.com`,
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID || '123456789012',
  appId: import.meta.env.VITE_FIREBASE_APP_ID || '1:123456789012:web:abcdef1234567890'
};

// Singleton App initialization
export const app = getApps().length === 0 ? initializeApp(firebaseConfig) : getApp();

// Auth instance
export const auth = getAuth(app);

// Firestore instance with multi-tab IndexedDB offline persistence
let firestoreInstance;
try {
  firestoreInstance = initializeFirestore(app, {
    localCache: persistentLocalCache({
      tabManager: persistentMultipleTabManager()
    })
  });
} catch (e) {
  // If already initialized or in fallback context
  firestoreInstance = getFirestore(app);
}
export const db = firestoreInstance;

// Storage instance
export const storage = getStorage(app);

// Connect to local emulators if running locally and emulator is detected
const isLocalhost =
  typeof window !== 'undefined' &&
  (window.location.hostname === 'localhost' ||
    window.location.hostname === '127.0.0.1' ||
    window.location.hostname.endsWith('.internal'));

if (isLocalhost && import.meta.env.VITE_USE_FIREBASE_EMULATORS === 'true') {
  try {
    const firestorePort = parseInt(import.meta.env.VITE_FIRESTORE_EMULATOR_PORT || '8898', 10);
    const authPort = parseInt(import.meta.env.VITE_AUTH_EMULATOR_PORT || '9099', 10);
    
    connectFirestoreEmulator(db, '127.0.0.1', firestorePort);
    connectAuthEmulator(auth, `http://127.0.0.1:${authPort}`, { disableWarnings: true });
    connectStorageEmulator(storage, '127.0.0.1', 9199);
    console.info(`[Happy Paws PWA] Connected to local Firebase emulators on Firestore:${firestorePort}`);
  } catch (err) {
    console.warn('[Happy Paws PWA] Emulator connection skipped or already initialized:', err);
  }
}
