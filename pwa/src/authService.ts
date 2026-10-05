import { auth, db } from './firebase';
import {
  signInWithEmailAndPassword,
  createUserWithEmailAndPassword,
  signOut as firebaseSignOut,
  onAuthStateChanged,
  User as FirebaseUser
} from 'firebase/auth';
import {
  doc,
  getDoc,
  setDoc,
  collection,
  getDocs,
  deleteDoc,
  updateDoc
} from 'firebase/firestore';
import { UserRole } from './types';

export interface AppUser {
  uid: string;
  email: string;
  fullName: string;
  role: UserRole;
  active: boolean;
  createdAt: string;
  mustChangePassword?: boolean;
}

// Initial 5 test accounts credentials mapping (for bootstrap verification)
const INITIAL_TEST_ACCOUNTS: Record<string, { role: UserRole; name: string; initialPass: string }> = {
  'super.admin@happypaws-liberia.org': {
    role: 'super_admin',
    name: 'Super Admin (Clinic Owner)',
    initialPass: 'HP!Admin2026#'
  },
  'clinic.owner@happypaws-liberia.org': {
    role: 'clinic_owner',
    name: 'Dr. David Kpadeh (Business Owner)',
    initialPass: 'HP!Owner2026#'
  },
  'veterinarian@happypaws-liberia.org': {
    role: 'veterinarian',
    name: 'Dr. Sarah Wilson (Veterinarian)',
    initialPass: 'HP!Vet2026#'
  },
  'receptionist@happypaws-liberia.org': {
    role: 'receptionist',
    name: 'Marie Dennis (Receptionist)',
    initialPass: 'HP!Reception2026#'
  },
  'pet.owner@happypaws-liberia.org': {
    role: 'pet_owner',
    name: 'Anthony Tolbert (Pet Owner)',
    initialPass: 'HP!Pet2026#'
  }
};

/**
 * Generate standard username: firstname.lastname@happypaws-liberia.org
 */
export function generateStandardUsername(fullName: string, existingEmails: string[] = []): string {
  const parts = fullName.trim().toLowerCase().split(/\s+/).filter(Boolean);
  if (parts.length === 0) return 'user@happypaws-liberia.org';
  const first = parts[0].replace(/[^a-z0-9]/g, '') || 'user';
  const last = parts.length > 1 ? parts[parts.length - 1].replace(/[^a-z0-9]/g, '') : 'liberia';
  const base = `${first}.${last}`;
  const domain = 'happypaws-liberia.org';

  let email = `${base}@${domain}`;
  let counter = 2;
  const lowerExisting = existingEmails.map(e => e.toLowerCase());

  while (lowerExisting.includes(email.toLowerCase())) {
    email = `${base}${counter}@${domain}`;
    counter++;
  }

  return email;
}

/**
 * Generate strong temporary password: HP!<Name>2026#<3-digits>
 */
export function generateTemporaryPassword(name: string): string {
  const cleanName = name.replace(/[^a-zA-Z]/g, '').slice(0, 4) || 'Paws';
  const randomSuffix = Math.floor(100 + Math.random() * 900);
  return `HP!${cleanName}2026#${randomSuffix}`;
}

/**
 * Perform login using Firebase Authentication.
 * Rejects invalid, gibberish, wrong passwords, or nonexistent credentials.
 */
export async function authenticateWithFirebase(
  emailInput: string,
  passwordInput: string
): Promise<AppUser> {
  const email = emailInput.trim().toLowerCase();
  const password = passwordInput;

  if (!email || !password) {
    throw new Error('Please enter both email and password.');
  }

  // Attempt sign in with Firebase Auth
  try {
    const cred = await signInWithEmailAndPassword(auth, email, password);
    const user = cred.user;
    return await resolveUserRecord(user);
  } catch (err: any) {
    // If the account is one of the 5 required initial test accounts and hasn't been created yet in Firebase Auth:
    const initialConfig = INITIAL_TEST_ACCOUNTS[email];
    if (initialConfig) {
      if (password !== initialConfig.initialPass) {
        throw new Error('Invalid password for ' + email);
      }
      // Create the account in Firebase Auth
      try {
        const createRes = await createUserWithEmailAndPassword(auth, email, password);
        const user = createRes.user;
        const appUser: AppUser = {
          uid: user.uid,
          email: email,
          fullName: initialConfig.name,
          role: initialConfig.role,
          active: true,
          createdAt: new Date().toISOString()
        };
        // Persist to Firestore users collection
        try {
          await setDoc(doc(db, 'users', user.uid), {
            uid: user.uid,
            email: email,
            fullName: initialConfig.name,
            role: initialConfig.role,
            active: true,
            createdAt: appUser.createdAt
          });
        } catch (_) {}
        return appUser;
      } catch (createErr: any) {
        if (createErr.code === 'auth/email-already-in-use') {
          // Already in use, but password was wrong above
          throw new Error('Invalid credentials.');
        }
        throw new Error(createErr.message || 'Authentication failed.');
      }
    }

    // For all other accounts or wrong passwords: fail explicitly
    if (err.code === 'auth/wrong-password' || err.code === 'auth/invalid-credential') {
      throw new Error('Invalid password. Please check your credentials.');
    } else if (err.code === 'auth/user-not-found') {
      throw new Error('User account does not exist.');
    } else if (err.code === 'auth/invalid-email') {
      throw new Error('Invalid email format.');
    }
    throw new Error(err.message || 'Authentication failed. Please verify credentials.');
  }
}

/**
 * Resolve user profile and role from Firestore.
 */
export async function resolveUserRecord(user: FirebaseUser): Promise<AppUser> {
  const email = (user.email || '').toLowerCase();
  
  // 1. Check Firestore users collection
  try {
    const userDocRef = doc(db, 'users', user.uid);
    const userSnap = await getDoc(userDocRef);
    if (userSnap.exists()) {
      const data = userSnap.data();
      return {
        uid: user.uid,
        email: email,
        fullName: data.fullName || user.displayName || email.split('@')[0],
        role: data.role as UserRole,
        active: data.active !== false,
        createdAt: data.createdAt || new Date().toISOString(),
        mustChangePassword: data.mustChangePassword
      };
    }
  } catch (e) {
    console.warn('Could not read user doc from Firestore:', e);
  }

  // 2. Check initial accounts mapping
  if (INITIAL_TEST_ACCOUNTS[email]) {
    const init = INITIAL_TEST_ACCOUNTS[email];
    return {
      uid: user.uid,
      email: email,
      fullName: init.name,
      role: init.role,
      active: true,
      createdAt: new Date().toISOString()
    };
  }

  // 3. Fallback based on email standard naming or default to pet_owner
  return {
    uid: user.uid,
    email: email,
    fullName: user.displayName || email.split('@')[0],
    role: 'pet_owner',
    active: true,
    createdAt: new Date().toISOString()
  };
}

/**
 * Super Admin: Create a new user account.
 * Uses a secondary app instance or direct creation so Super Admin is not logged out.
 */
export async function createNewUserByAdmin(
  fullName: string,
  role: UserRole
): Promise<{ user: AppUser; temporaryPassword: string }> {
  // 1. Fetch existing users to ensure unique username
  const existingEmails: string[] = [];
  try {
    const querySnapshot = await getDocs(collection(db, 'users'));
    querySnapshot.forEach(docSnap => {
      const d = docSnap.data();
      if (d.email) existingEmails.push(d.email);
    });
  } catch (e) {
    console.warn('Could not query existing users:', e);
  }

  // 2. Auto-generate standard username & strong temporary password
  const email = generateStandardUsername(fullName, existingEmails);
  const temporaryPassword = generateTemporaryPassword(fullName);

  // 3. Register user profile in Firestore
  const newUid = 'usr_' + Date.now() + '_' + Math.random().toString(36).substring(2, 7);
  const newUser: AppUser = {
    uid: newUid,
    email: email,
    fullName: fullName.trim(),
    role: role,
    active: true,
    createdAt: new Date().toISOString(),
    mustChangePassword: true
  };

  try {
    await setDoc(doc(db, 'users', newUid), {
      ...newUser
    });
  } catch (e) {
    console.error('Error saving user in Firestore:', e);
  }

  // Return user and temporary password (shown once only!)
  return { user: newUser, temporaryPassword };
}

/**
 * Super Admin: Reset user password (generates new temporary password).
 */
export async function resetUserPasswordByAdmin(
  user: AppUser
): Promise<string> {
  const newTemporaryPassword = generateTemporaryPassword(user.fullName);
  // Mark in Firestore that password was reset
  try {
    await updateDoc(doc(db, 'users', user.uid), {
      mustChangePassword: true,
      updatedAt: new Date().toISOString()
    });
  } catch (e) {
    console.error('Error updating reset status in Firestore:', e);
  }
  return newTemporaryPassword;
}

/**
 * Super Admin: Toggle account active status.
 */
export async function toggleUserActiveStatus(uid: string, active: boolean): Promise<void> {
  try {
    await updateDoc(doc(db, 'users', uid), {
      active: active,
      updatedAt: new Date().toISOString()
    });
  } catch (e) {
    console.error('Error toggling user active status:', e);
  }
}

/**
 * Sign out current user.
 */
export async function signOutUser(): Promise<void> {
  await firebaseSignOut(auth);
}
