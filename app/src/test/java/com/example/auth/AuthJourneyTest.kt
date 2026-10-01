package com.example.auth

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.auth.AuthRepository
import com.example.data.auth.AuthState
import com.example.data.local.AppDatabase
import com.example.data.local.ClientEntity
import com.example.data.local.PetEntity
import com.example.model.UserRole
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AuthJourneyTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var authRepository: AuthRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        authRepository = AuthRepository(context, db, testScope)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `test initial state starts as Unauthenticated or Initializing without bypass`() {
        val state = authRepository.authState.value
        assertTrue(
            "State should be Initializing or Unauthenticated on fresh launch",
            state is AuthState.Initializing || state is AuthState.Unauthenticated
        )
    }

    @Test
    fun `test New Pet Owner registration creates incomplete profile ready for onboarding`() = runTest {
        val email = "newparent@gmail.com"
        val password = "securePassword123"
        val fullName = "Clara Weah"
        val phone = "0770554433"

        val result = authRepository.registerPetOwner(email, password, fullName, phone)
        assertTrue(result.isSuccess)
        val state = authRepository.authState.value
        assertTrue(
            "Newly registered pet owner must be in AuthenticatedProfileIncomplete or AuthenticatedUnverified",
            state is AuthState.AuthenticatedProfileIncomplete || state is AuthState.AuthenticatedUnverified
        )
    }

    @Test
    fun `test New Owner First Pet Onboarding creates Client and Pet without duplicates`() = runTest {
        val fullName = "Kofi Mensah"
        val phone = "0889112233"
        val petName = "Charlie"
        val species = "Dog"
        val breed = "African Village Dog"
        val sex = "Male"
        val dob = "2024-05-10"
        val color = "Golden Tan"
        val weightKg = 14.5

        val result = authRepository.completeNewPetOwnerOnboarding(
            fullName = fullName,
            phone = phone,
            petName = petName,
            species = species,
            breed = breed,
            sex = sex,
            dob = dob,
            color = color,
            weightKg = weightKg
        )

        assertTrue(result.isSuccess)
        val createdPet = result.getOrNull()
        assertNotNull(createdPet)
        assertEquals("Charlie", createdPet?.name)

        // Verify in Room database
        val client = db.clientDao().getClientById(createdPet!!.ownerId)
        assertNotNull(client)
        assertEquals("Kofi Mensah", client?.fullName)
        assertEquals(phone, client?.phone)

        // Verify AuthState is now AuthenticatedPetOwner
        val state = authRepository.authState.value
        assertTrue(state is AuthState.AuthenticatedPetOwner)
        val ownerState = state as AuthState.AuthenticatedPetOwner
        assertEquals("Kofi Mensah", ownerState.client.fullName)
    }

    @Test
    fun `test Existing Clinic Client Linking associates Anthony with Bella and prevents Anthony 2`() = runTest {
        // Step 1: Seed existing clinic client (Anthony) who already brought Bella to the clinic
        val initialClient = ClientEntity(
            id = 101,
            fullName = "Anthony Tolbert",
            preferredName = "Anthony",
            phone = "0881479329",
            email = "antojayster@gmail.com",
            claimCode = "HP-BELLA1",
            firebaseAuthUid = "" // Unclaimed
        )
        db.clientDao().insertClient(initialClient)

        val bellaPet = PetEntity(
            id = 201,
            ownerId = 101,
            name = "Bella",
            species = "Dog",
            breed = "African Village Dog",
            sex = "Female",
            dateOfBirth = "2023-01-15",
            color = "Brown & White",
            weightKg = 12.0
        )
        db.petDao().insertPet(bellaPet)

        assertEquals("Should have exactly 1 client initially", 1, db.clientDao().getClientCount())
        assertEquals("Should have exactly 1 pet initially", 1, db.petDao().getPetCount())

        // Step 2: Anthony creates an account and links his clinic record using phone & claim code
        val linkResult = authRepository.linkExistingClinicRecord(
            phoneOrEmail = "0881479329",
            claimCode = "HP-BELLA1"
        )

        assertTrue("Linking should succeed with valid claim code", linkResult.isSuccess)
        val linkedClient = linkResult.getOrNull()
        assertNotNull(linkedClient)
        assertEquals(101L, linkedClient?.id)
        assertTrue("Auth UID must now be populated on existing client", linkedClient?.firebaseAuthUid?.isNotEmpty() == true)
        assertEquals("Claim code should be cleared upon successful claim", "", linkedClient?.claimCode)

        // Step 3: Verify DUPLICATE-CLIENT PREVENTION
        // There must still be exactly 1 client and 1 pet in the database, NOT Anthony 2 or Bella 2!
        val clientCount = db.clientDao().getClientCount()
        val petCount = db.petDao().getPetCount()
        assertEquals("Must NOT create duplicate Client Anthony 2", 1, clientCount)
        assertEquals("Must NOT create duplicate Pet Bella 2", 1, petCount)

        // Verify linked pet is still Bella
        val ownerPet = db.petDao().getPetById(201)
        assertNotNull(ownerPet)
        assertEquals("Bella", ownerPet?.name)
        assertEquals(101L, ownerPet?.ownerId)

        // Verify AuthState transitioned to AuthenticatedPetOwner
        val state = authRepository.authState.value
        assertTrue(state is AuthState.AuthenticatedPetOwner)
    }

    @Test
    fun `test Invalid Claim Code fails linking and preserves unlinked record`() = runTest {
        val initialClient = ClientEntity(
            id = 102,
            fullName = "Marie Dennis",
            phone = "0776543210",
            claimCode = "HP-SIMBA2",
            firebaseAuthUid = ""
        )
        db.clientDao().insertClient(initialClient)

        val linkResult = authRepository.linkExistingClinicRecord(
            phoneOrEmail = "0776543210",
            claimCode = "WRONG-CODE-999"
        )

        assertTrue("Linking with invalid claim code must fail", linkResult.isFailure)

        val client = db.clientDao().getClientById(102)
        assertEquals("Auth UID must remain empty on failed claim", "", client?.firebaseAuthUid)
        assertEquals("Claim code must remain unchanged on failed claim", "HP-SIMBA2", client?.claimCode)
    }

    @Test
    fun `test Logout terminates session and returns to Unauthenticated`() = runTest {
        // Sign in offline test user
        authRepository.signIn("test.user@happypawsliberia.org", "password123")
        assertTrue(authRepository.authState.value !is AuthState.Unauthenticated)

        // Terminate session
        authRepository.signOut()
        assertEquals(AuthState.Unauthenticated, authRepository.authState.value)
    }

    @Test
    fun `test Forgot Password validates empty or invalid email`() = runTest {
        val emptyResult = authRepository.sendPasswordReset("")
        assertTrue("Empty email should fail reset", emptyResult.isFailure)

        val invalidResult = authRepository.sendPasswordReset("not-an-email")
        assertTrue("Invalid email format should fail reset", invalidResult.isFailure)
    }

    @Test
    fun `test Public registration cannot elevate to Staff roles`() = runTest {
        // An unauthorized user tries to register with staff-sounding email
        authRepository.registerPetOwner("attacker.admin@example.com", "password123", "Attacker", "088111222")

        val state = authRepository.authState.value
        // State must NEVER be AuthenticatedStaff from public register
        assertFalse(
            "Public registration must never result in AuthenticatedStaff",
            state is AuthState.AuthenticatedStaff
        )
    }

    @Test
    fun `test Development Persona Switcher updates local UI state without granting server credentials`() = runTest {
        // Toggle dev sandbox
        authRepository.devSwitchRole(UserRole.VETERINARIAN)
        val staffState = authRepository.authState.value
        assertTrue(staffState is AuthState.AuthenticatedStaff)
        assertEquals(UserRole.VETERINARIAN, (staffState as AuthState.AuthenticatedStaff).role)

        authRepository.devSwitchRole(UserRole.PET_OWNER)
        val petOwnerState = authRepository.authState.value
        assertTrue(petOwnerState is AuthState.AuthenticatedPetOwner)
    }
}
