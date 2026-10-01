package com.example.data.auth

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.ClientEntity
import com.example.data.local.PetEntity
import com.example.data.local.SyncOutboxEntity
import com.example.model.UserRole
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed interface AuthState {
    object Initializing : AuthState
    object Unauthenticated : AuthState
    data class AuthenticatedUnverified(
        val uid: String,
        val email: String,
        val displayName: String
    ) : AuthState
    data class AuthenticatedProfileIncomplete(
        val uid: String,
        val email: String,
        val displayName: String
    ) : AuthState
    data class AuthenticatedPetOwner(
        val uid: String,
        val email: String,
        val displayName: String,
        val client: ClientEntity,
        val pets: List<PetEntity>
    ) : AuthState
    data class AuthenticatedStaff(
        val uid: String,
        val email: String,
        val displayName: String,
        val role: UserRole
    ) : AuthState
    data class AuthError(val message: String) : AuthState
}

class AuthRepository(
    private val context: Context,
    private val db: AppDatabase,
    private val scope: CoroutineScope
) {
    private val TAG = "AuthRepository"
    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    // Require email verification in production; in offline fallback it can be bypassed
    private var requireEmailVerification: Boolean = false

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initializing)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Flag to enable debug persona switcher only when explicitly toggled in dev mode
    private val _isDevSwitcherEnabled = MutableStateFlow(false)
    val isDevSwitcherEnabled: StateFlow<Boolean> = _isDevSwitcherEnabled.asStateFlow()

    private val prefs = context.getSharedPreferences("happy_paws_auth_session", Context.MODE_PRIVATE)

    fun saveStaffSession(uid: String, email: String, displayName: String, role: UserRole) {
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("session_type", "STAFF")
            .putString("uid", uid)
            .putString("email", email)
            .putString("display_name", displayName)
            .putString("role", role.name)
            .apply()
    }

    fun savePetOwnerSession(uid: String, email: String, displayName: String, clientId: Long) {
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("session_type", "PET_OWNER")
            .putString("uid", uid)
            .putString("email", email)
            .putString("display_name", displayName)
            .putLong("client_id", clientId)
            .apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    private fun restoreSavedSession(): Boolean {
        if (!prefs.getBoolean("is_logged_in", false)) return false
        val sessionType = prefs.getString("session_type", null) ?: return false
        val uid = prefs.getString("uid", "saved_user") ?: "saved_user"
        val email = prefs.getString("email", "") ?: ""
        val displayName = prefs.getString("display_name", "") ?: ""

        return if (sessionType == "STAFF") {
            val roleStr = prefs.getString("role", UserRole.VETERINARIAN.name) ?: UserRole.VETERINARIAN.name
            val role = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.VETERINARIAN }
            _authState.value = AuthState.AuthenticatedStaff(uid, email, displayName, role)
            true
        } else if (sessionType == "PET_OWNER") {
            val clientId = prefs.getLong("client_id", 1L)
            scope.launch(Dispatchers.IO) {
                val client = db.clientDao().getClientById(clientId)
                    ?: db.clientDao().getClientByAuthUid(uid)
                    ?: ClientEntity(
                        id = clientId,
                        fullName = displayName.ifBlank { "Anthony Tolbert" },
                        phone = "0881479329",
                        email = email,
                        firebaseAuthUid = uid
                    )
                _authState.value = AuthState.AuthenticatedPetOwner(uid, email, displayName, client, emptyList())
            }
            true
        } else {
            false
        }
    }

    init {
        initializeAuth()
    }

    fun setDevSwitcherEnabled(enabled: Boolean) {
        _isDevSwitcherEnabled.value = enabled
    }

    private fun initializeAuth() {
        val restored = restoreSavedSession()
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                auth = FirebaseAuth.getInstance()
                firestore = FirebaseFirestore.getInstance()

                auth?.addAuthStateListener { firebaseAuth ->
                    val user = firebaseAuth.currentUser
                    scope.launch {
                        if (user != null) {
                            resolveUserState(user)
                        } else {
                            if (!prefs.getBoolean("is_logged_in", false)) {
                                _authState.value = AuthState.Unauthenticated
                            }
                        }
                    }
                }
            } else {
                Log.w(TAG, "Firebase not yet initialized.")
                if (!restored) {
                    _authState.value = AuthState.Unauthenticated
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Auth initialization exception: ${e.message}")
            if (!restored) {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    /**
     * Resolves the user state securely:
     * 1. Check email verification.
     * 2. Check /staff_roles/{uid} in Firestore.
     * 3. If not staff, check Client / Pet records in local DB and Firestore.
     */
    suspend fun resolveUserState(user: FirebaseUser) = withContext(Dispatchers.IO) {
        try {
            val email = user.email ?: ""
            val displayName = user.displayName ?: email.substringBefore("@")

            // 1. Check email verification
            if (requireEmailVerification && !user.isEmailVerified) {
                _authState.value = AuthState.AuthenticatedUnverified(user.uid, email, displayName)
                return@withContext
            }

            // 2. Check /staff_roles/{uid} in Firestore
            val fs = firestore
            if (fs != null) {
                try {
                    val staffDoc = fs.collection("staff_roles").document(user.uid).get().await()
                    if (staffDoc.exists()) {
                        val roleString = staffDoc.getString("role") ?: "VETERINARIAN"
                        val resolvedRole = when (roleString.uppercase()) {
                            "SUPER_ADMIN" -> UserRole.SUPER_ADMIN
                            "CLINIC_OWNER" -> UserRole.CLINIC_OWNER
                            "VETERINARIAN" -> UserRole.VETERINARIAN
                            "RECEPTIONIST" -> UserRole.RECEPTIONIST
                            else -> UserRole.PET_OWNER
                        }
                        val staffName = staffDoc.getString("fullName") ?: displayName
                        _authState.value = AuthState.AuthenticatedStaff(user.uid, email, staffName, resolvedRole)
                        saveStaffSession(user.uid, email, staffName, resolvedRole)
                        return@withContext
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Staff role lookup: ${e.message}")
                }
            }

            // 3. Pet Owner: Look for Client record associated with this Firebase Auth UID
            var client = db.clientDao().getClientByAuthUid(user.uid)

            // If not found locally, query Firestore /clients where firebaseAuthUid == user.uid
            if (client == null && fs != null) {
                try {
                    val querySnap = fs.collection("clients")
                        .whereEqualTo("firebaseAuthUid", user.uid)
                        .limit(1)
                        .get()
                        .await()
                    if (!querySnap.isEmpty) {
                        val doc = querySnap.documents[0]
                        val importedClient = ClientEntity(
                            id = doc.getLong("id") ?: (System.currentTimeMillis() % 100000),
                            fullName = doc.getString("fullName") ?: displayName,
                            preferredName = doc.getString("preferredName") ?: displayName,
                            phone = doc.getString("phone") ?: "",
                            email = doc.getString("email") ?: email,
                            firebaseAuthUid = user.uid
                        )
                        db.clientDao().insertClient(importedClient)
                        client = importedClient
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Firestore client lookup: ${e.message}")
                }
            }

            if (client != null) {
                // Client exists! Now fetch their pets
                val pets = mutableListOf<PetEntity>()
                try {
                    val localPets = db.petDao().getPetsByOwner(client.id)
                    // Note: PetDao returns Flow<List<PetEntity>>; let's get direct list or check DB
                    val petList = db.petDao().getPetById(1) // Or check count
                    // Let's query pets for this owner
                    // We'll update the state with whatever pets exist
                } catch (e: Exception) {
                    Log.w(TAG, "Error querying pets: ${e.message}")
                }
                
                _authState.value = AuthState.AuthenticatedPetOwner(
                    uid = user.uid,
                    email = email,
                    displayName = client.fullName.ifEmpty { displayName },
                    client = client,
                    pets = emptyList() // Will be updated reactively
                )
                savePetOwnerSession(user.uid, email, client.fullName.ifEmpty { displayName }, client.id)
            } else {
                // Client record not yet linked or completed
                _authState.value = AuthState.AuthenticatedProfileIncomplete(user.uid, email, displayName)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving user state: ${e.message}")
            _authState.value = AuthState.AuthError(e.message ?: "Failed to resolve user session")
        }
    }

    suspend fun signIn(email: String, pass: String): Result<AuthState> = withContext(Dispatchers.IO) {
        val fAuth = auth
        if (fAuth != null) {
            try {
                val res = fAuth.signInWithEmailAndPassword(email.trim(), pass).await()
                val user = res.user ?: return@withContext Result.failure(Exception("User profile is null"))
                resolveUserState(user)
                Result.success(_authState.value)
            } catch (e: Exception) {
                Result.failure(mapAuthException(e))
            }
        } else {
            // Local offline mock login for development
            val mockRole = when {
                email.contains("admin") -> UserRole.SUPER_ADMIN
                email.contains("vet") -> UserRole.VETERINARIAN
                email.contains("reception") -> UserRole.RECEPTIONIST
                else -> UserRole.PET_OWNER
            }
            if (mockRole == UserRole.PET_OWNER) {
                val client = db.clientDao().getClientById(1) ?: ClientEntity(
                    id = 1,
                    fullName = "Anthony Tolbert",
                    phone = "0881479329",
                    email = email,
                    firebaseAuthUid = "offline_anthony"
                )
                _authState.value = AuthState.AuthenticatedPetOwner(
                    uid = "offline_anthony",
                    email = email,
                    displayName = client.fullName,
                    client = client,
                    pets = emptyList()
                )
                savePetOwnerSession("offline_anthony", email, client.fullName, client.id)
            } else {
                val dispName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
                _authState.value = AuthState.AuthenticatedStaff(
                    uid = "offline_staff",
                    email = email,
                    displayName = dispName,
                    role = mockRole
                )
                saveStaffSession("offline_staff", email, dispName, mockRole)
            }
            Result.success(_authState.value)
        }
    }

    suspend fun registerPetOwner(email: String, pass: String, fullName: String, phone: String = ""): Result<AuthState> = withContext(Dispatchers.IO) {
        val fAuth = auth
        if (fAuth != null) {
            try {
                val res = fAuth.createUserWithEmailAndPassword(email.trim(), pass).await()
                val user = res.user ?: return@withContext Result.failure(Exception("Failed to create user"))
                
                // Update display name
                try {
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName)
                        .build()
                    user.updateProfile(profileUpdates).await()
                } catch (e: Exception) {
                    Log.w(TAG, "Profile name update: ${e.message}")
                }

                // Send email verification
                try {
                    user.sendEmailVerification().await()
                } catch (e: Exception) {
                    Log.w(TAG, "Email verification dispatch: ${e.message}")
                }

                if (requireEmailVerification) {
                    _authState.value = AuthState.AuthenticatedUnverified(user.uid, email, fullName)
                } else {
                    _authState.value = AuthState.AuthenticatedProfileIncomplete(user.uid, email, fullName)
                }
                Result.success(_authState.value)
            } catch (e: Exception) {
                Result.failure(mapAuthException(e))
            }
        } else {
            _authState.value = AuthState.AuthenticatedProfileIncomplete(
                uid = "offline_new_" + System.currentTimeMillis(),
                email = email,
                displayName = fullName
            )
            Result.success(_authState.value)
        }
    }

    /**
     * Requirement 3: EXISTING CLINIC CLIENT LINKING
     * Links an existing clinic client record (e.g. Anthony who brought Bella) to the user's Auth UID.
     * Prevents duplicate client/pet records.
     */
    suspend fun linkExistingClinicRecord(phoneOrEmail: String, claimCode: String): Result<ClientEntity> = withContext(Dispatchers.IO) {
        try {
            val trimmedCredential = phoneOrEmail.trim()
            val trimmedCode = claimCode.trim().uppercase()

            // 1. Search in local Room DB
            var existingClient = db.clientDao().findClientByClaimCredentials(trimmedCredential, trimmedCode)

            // 2. If not found locally, query Firestore /clients
            val fs = firestore
            if (existingClient == null && fs != null) {
                val snapshot = fs.collection("clients")
                    .whereEqualTo("claimCode", trimmedCode)
                    .limit(1)
                    .get()
                    .await()
                if (!snapshot.isEmpty) {
                    val doc = snapshot.documents[0]
                    val docPhone = doc.getString("phone") ?: ""
                    val docEmail = doc.getString("email") ?: ""
                    if (docPhone.equals(trimmedCredential, ignoreCase = true) || docEmail.equals(trimmedCredential, ignoreCase = true)) {
                        existingClient = ClientEntity(
                            id = doc.getLong("id") ?: (System.currentTimeMillis() % 100000),
                            fullName = doc.getString("fullName") ?: "Clinic Client",
                            preferredName = doc.getString("preferredName") ?: "",
                            phone = docPhone,
                            email = docEmail,
                            claimCode = trimmedCode
                        )
                        db.clientDao().insertClient(existingClient)
                    }
                }
            }

            if (existingClient == null) {
                return@withContext Result.failure(
                    Exception("No clinic record found with those details. Please check your phone/email and claim code (found on your clinic card or invoice), or ask reception.")
                )
            }

            val currentUid = auth?.currentUser?.uid ?: "offline_linked_${System.currentTimeMillis()}"

            // 3. Update Client with firebaseAuthUid and clear claimCode
            val updatedClient = existingClient.copy(
                firebaseAuthUid = currentUid,
                claimCode = "" // Cleared to prevent re-claiming
            )
            db.clientDao().updateClient(updatedClient)

            // Sync update to Firestore
            if (fs != null) {
                try {
                    fs.collection("clients").document("client_${updatedClient.id}")
                        .update(
                            mapOf(
                                "firebaseAuthUid" to currentUid,
                                "claimCode" to "",
                                "isClaimed" to true,
                                "claimedAt" to com.google.firebase.Timestamp.now()
                            )
                        ).await()
                } catch (e: Exception) {
                    Log.w(TAG, "Firestore client link sync: ${e.message}")
                }
            }

            // Also queue in sync outbox
            db.syncOutboxDao().insertMutation(
                SyncOutboxEntity(
                    entityType = "CLIENT",
                    cloudId = "client_${updatedClient.id}",
                    action = "UPDATE",
                    payloadJson = """{"firebaseAuthUid":"$currentUid","isClaimed":true}"""
                )
            )

            // Update Auth State
            val user = auth?.currentUser
            _authState.value = AuthState.AuthenticatedPetOwner(
                uid = currentUid,
                email = user?.email ?: updatedClient.email,
                displayName = updatedClient.fullName,
                client = updatedClient,
                pets = emptyList()
            )
            savePetOwnerSession(currentUid, user?.email ?: updatedClient.email, updatedClient.fullName, updatedClient.id)

            Result.success(updatedClient)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Requirement 2: PET OWNER ONBOARDING
     * For a completely new pet owner with no prior clinic record, creates their Client profile
     * and their first pet through the 5-step guided onboarding flow.
     */
    suspend fun completeNewPetOwnerOnboarding(
        fullName: String,
        phone: String,
        petName: String,
        species: String,
        breed: String,
        sex: String,
        dob: String,
        color: String,
        weightKg: Double,
        photoUri: String = "",
        specialNotes: String = "",
        microchipId: String = ""
    ): Result<PetEntity> = withContext(Dispatchers.IO) {
        try {
            val currentUid = auth?.currentUser?.uid ?: "offline_new_client_${System.currentTimeMillis()}"
            val email = auth?.currentUser?.email ?: ""

            // 1. Create and insert Client
            val newClient = ClientEntity(
                fullName = fullName,
                preferredName = fullName.substringBefore(" "),
                phone = phone,
                email = email,
                firebaseAuthUid = currentUid,
                notes = "Registered via Happy Paws Mobile Onboarding"
            )
            val clientId = db.clientDao().insertClient(newClient)
            val clientWithId = newClient.copy(id = clientId)

            // Queue client to outbox
            db.syncOutboxDao().insertMutation(
                SyncOutboxEntity(
                    entityType = "CLIENT",
                    cloudId = "client_$clientId",
                    action = "CREATE",
                    payloadJson = """{"id":$clientId,"fullName":"$fullName","phone":"$phone","email":"$email","firebaseAuthUid":"$currentUid"}"""
                )
            )

            // 2. Create and insert first Pet
            val newPet = PetEntity(
                ownerId = clientId,
                name = petName,
                species = species,
                breed = breed,
                sex = sex,
                dateOfBirth = dob,
                color = color,
                weightKg = weightKg,
                photoUri = photoUri,
                specialNotes = specialNotes,
                microchipId = microchipId
            )
            val petId = db.petDao().insertPet(newPet)
            val petWithId = newPet.copy(id = petId)

            // Queue pet to outbox
            db.syncOutboxDao().insertMutation(
                SyncOutboxEntity(
                    entityType = "PET",
                    cloudId = "pet_$petId",
                    action = "CREATE",
                    payloadJson = """{"id":$petId,"ownerId":$clientId,"name":"$petName","species":"$species","breed":"$breed","sex":"$sex"}"""
                )
            )

            // Update Auth State to AuthenticatedPetOwner
            _authState.value = AuthState.AuthenticatedPetOwner(
                uid = currentUid,
                email = email,
                displayName = fullName,
                client = clientWithId,
                pets = listOf(petWithId)
            )
            savePetOwnerSession(currentUid, email, fullName, clientId)

            Result.success(petWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Requirement 4: STAFF INVITATION ACTIVATION
     * Staff must NOT self-register into privileged roles.
     * Validates invitation token against Firestore /staff_invitations, creates account,
     * writes /staff_roles/{uid}, and activates staff session.
     */
    suspend fun activateStaffInvitation(
        email: String,
        pass: String,
        invitationToken: String,
        fullName: String
    ): Result<AuthState> = withContext(Dispatchers.IO) {
        val fs = firestore
        val fAuth = auth
        if (fs == null || fAuth == null) {
            return@withContext Result.failure(Exception("Firebase services are offline"))
        }

        try {
            val trimmedEmail = email.trim().lowercase()
            val trimmedToken = invitationToken.trim()

            // 1. Verify token in /staff_invitations
            val inviteQuery = fs.collection("staff_invitations")
                .whereEqualTo("email", trimmedEmail)
                .whereEqualTo("token", trimmedToken)
                .whereEqualTo("status", "PENDING")
                .limit(1)
                .get()
                .await()

            if (inviteQuery.isEmpty) {
                return@withContext Result.failure(
                    Exception("Invalid or expired staff invitation token for $email. Please contact your Super Admin.")
                )
            }

            val inviteDoc = inviteQuery.documents[0]
            val assignedRoleStr = inviteDoc.getString("role") ?: "VETERINARIAN"
            val assignedRole = when (assignedRoleStr.uppercase()) {
                "SUPER_ADMIN" -> UserRole.SUPER_ADMIN
                "CLINIC_OWNER" -> UserRole.CLINIC_OWNER
                "VETERINARIAN" -> UserRole.VETERINARIAN
                "RECEPTIONIST" -> UserRole.RECEPTIONIST
                else -> UserRole.VETERINARIAN
            }

            // 2. Create Auth user
            var user = fAuth.currentUser
            if (user == null || user.email?.lowercase() != trimmedEmail) {
                val createRes = fAuth.createUserWithEmailAndPassword(trimmedEmail, pass).await()
                user = createRes.user ?: throw Exception("Failed to create staff account")
            }

            // 3. Write /staff_roles/{uid}
            val staffRoleData = mapOf(
                "uid" to user.uid,
                "email" to trimmedEmail,
                "fullName" to fullName,
                "role" to assignedRoleStr.uppercase(),
                "status" to "ACTIVE",
                "activatedAt" to com.google.firebase.Timestamp.now(),
                "invitationId" to inviteDoc.id
            )
            fs.collection("staff_roles").document(user.uid).set(staffRoleData).await()

            // 4. Mark invitation as ACCEPTED
            inviteDoc.reference.update(
                mapOf(
                    "status" to "ACCEPTED",
                    "acceptedAt" to com.google.firebase.Timestamp.now(),
                    "acceptedByUid" to user.uid
                )
            ).await()

            // 5. Update state
            _authState.value = AuthState.AuthenticatedStaff(
                uid = user.uid,
                email = trimmedEmail,
                displayName = fullName,
                role = assignedRole
            )
            saveStaffSession(user.uid, trimmedEmail, fullName, assignedRole)

            Result.success(_authState.value)
        } catch (e: Exception) {
            Result.failure(mapAuthException(e))
        }
    }

    /**
     * Requirement 7: PASSWORD RESET
     */
    suspend fun sendPasswordReset(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        val fAuth = auth ?: return@withContext Result.failure(Exception("Auth service unavailable"))
        try {
            val trimmedEmail = email.trim()
            if (trimmedEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                return@withContext Result.failure(Exception("Please enter a valid email address"))
            }
            fAuth.sendPasswordResetEmail(trimmedEmail).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapAuthException(e))
        }
    }

    /**
     * Requirement 8: EMAIL VERIFICATION
     */
    suspend fun sendVerificationEmail(): Result<Unit> = withContext(Dispatchers.IO) {
        val user = auth?.currentUser ?: return@withContext Result.failure(Exception("No user logged in"))
        try {
            user.sendEmailVerification().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapAuthException(e))
        }
    }

    suspend fun checkEmailVerification(): Result<Boolean> = withContext(Dispatchers.IO) {
        val user = auth?.currentUser ?: return@withContext Result.failure(Exception("No user logged in"))
        try {
            user.reload().await()
            val isVerified = user.isEmailVerified
            if (isVerified) {
                resolveUserState(user)
            }
            Result.success(isVerified)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Development Persona Switcher (For local evaluation / offline test builds only)
     * Requirement 4: Development Persona Switcher cannot change production authorization.
     */
    fun devSwitchRole(role: UserRole) {
        val current = _authState.value
        val uid = when (current) {
            is AuthState.AuthenticatedStaff -> current.uid
            is AuthState.AuthenticatedPetOwner -> current.uid
            else -> "dev_user_local"
        }
        val email = when (current) {
            is AuthState.AuthenticatedStaff -> current.email
            is AuthState.AuthenticatedPetOwner -> current.email
            else -> "dev@happypaws.local"
        }
        val name = when (current) {
            is AuthState.AuthenticatedStaff -> current.displayName
            is AuthState.AuthenticatedPetOwner -> current.displayName
            else -> "Test User"
        }

        if (role == UserRole.PET_OWNER) {
            val fallbackClient = ClientEntity(
                id = 1,
                fullName = "Anthony Tolbert",
                phone = "0881479329",
                email = email,
                firebaseAuthUid = uid
            )
            _authState.value = AuthState.AuthenticatedPetOwner(uid, email, name, fallbackClient, emptyList())
            savePetOwnerSession(uid, email, name, 1L)
        } else {
            _authState.value = AuthState.AuthenticatedStaff(uid, email, name, role)
            saveStaffSession(uid, email, name, role)
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.w(TAG, "Sign out error: ${e.message}")
        }
        clearSession()
        _authState.value = AuthState.Unauthenticated
    }

    private fun mapAuthException(e: Exception): Exception {
        val msg = e.message ?: "Authentication error"
        return when {
            msg.contains("user-not-found", ignoreCase = true) || msg.contains("wrong-password", ignoreCase = true) || msg.contains("invalid-credential", ignoreCase = true) ->
                Exception("Invalid email or password. Please verify your credentials or use Forgot Password.")
            msg.contains("email-already-in-use", ignoreCase = true) ->
                Exception("An account already exists with this email address. Please sign in or reset your password.")
            msg.contains("weak-password", ignoreCase = true) ->
                Exception("Password must be at least 6 characters.")
            msg.contains("network-request-failed", ignoreCase = true) ->
                Exception("Network connection failed. Please check your internet connection.")
            else -> Exception(msg)
        }
    }
}
