package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.data.local.AppDatabase
import com.example.data.repository.HappyPawsRepository
import com.example.model.UserRole
import com.example.ui.components.HappyPawsLogo
import com.example.ui.components.PetOnboardingWizardDialog
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.HappyPawsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: HappyPawsViewModel

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(this, lifecycleScope)
        val repository = HappyPawsRepository(
            clientDao = db.clientDao(),
            petDao = db.petDao(),
            appointmentDao = db.appointmentDao(),
            consultationDao = db.consultationDao(),
            preventiveDao = db.preventiveDao(),
            inventoryBillingDao = db.inventoryBillingDao(),
            crmDao = db.crmDao(),
            syncOutboxDao = db.syncOutboxDao()
        )

        // Initialize Firestore Sync Engine with offline persistence
        val syncEngine = com.example.data.sync.FirestoreSyncEngine(this, db, lifecycleScope)
        syncEngine.startRealtimeSync()
        lifecycleScope.launch(Dispatchers.IO) {
            syncEngine.drainOutbox()
        }

        // Initialize Production Auth Repository
        val authRepository = com.example.data.auth.AuthRepository(this, db, lifecycleScope)

        // Seed data if empty
        lifecycleScope.launch(Dispatchers.IO) {
            val count = db.clientDao().getClientCount()
            if (count == 0) {
                AppDatabase.populateSeedData(db)
            }
        }

        viewModel = HappyPawsViewModel(repository, authRepository)

        setContent {
            HappyPawsTheme {
                val authState by viewModel.authState.collectAsStateWithLifecycle()
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                var currentStaffTab by rememberSaveable { mutableIntStateOf(0) }
                var currentOwnerTab by rememberSaveable { mutableIntStateOf(0) }
                var showOnboardingWizard by rememberSaveable { mutableStateOf(false) }
                var showAccountDialog by rememberSaveable { mutableStateOf(false) }

                when (val currAuth = authState) {
                    is com.example.data.auth.AuthState.Initializing -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(WarmIvory),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                HappyPawsLogo(size = 96.dp, showTagline = true)
                                Spacer(modifier = Modifier.height(24.dp))
                                CircularProgressIndicator(color = AmberTerracotta)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Restoring session...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MediumCharcoal
                                )
                            }
                        }
                    }

                    is com.example.data.auth.AuthState.Unauthenticated, is com.example.data.auth.AuthState.AuthError -> {
                        var authErrorMsg by remember { mutableStateOf<String?>(null) }
                        var isAuthLoading by remember { mutableStateOf(false) }

                        AuthScreen(
                            onSignIn = { email, pass ->
                                isAuthLoading = true
                                authErrorMsg = null
                                viewModel.signIn(email, pass) { success, err ->
                                    isAuthLoading = false
                                    if (!success) authErrorMsg = err
                                }
                            },
                            onRegisterPetOwner = { email, pass, name, phone ->
                                isAuthLoading = true
                                authErrorMsg = null
                                viewModel.registerPetOwner(email, pass, name, phone) { success, err ->
                                    isAuthLoading = false
                                    if (!success) authErrorMsg = err
                                }
                            },
                            onActivateStaffInvite = { email, pass, token, name ->
                                isAuthLoading = true
                                authErrorMsg = null
                                viewModel.activateStaffInvitation(email, pass, token, name) { success, err ->
                                    isAuthLoading = false
                                    if (!success) authErrorMsg = err
                                }
                            },
                            onForgotPassword = { email ->
                                viewModel.forgotPassword(email) { _, err ->
                                    if (err != null) authErrorMsg = err
                                }
                            },
                            onLinkClinicRecord = { phoneOrEmail, claimCode ->
                                isAuthLoading = true
                                authErrorMsg = null
                                viewModel.linkClinicRecord(phoneOrEmail, claimCode) { success, err ->
                                    isAuthLoading = false
                                    if (!success) authErrorMsg = err
                                }
                            },
                            onDevSwitchRole = { role ->
                                viewModel.devSwitchRole(role)
                            },
                            errorMessage = authErrorMsg,
                            isLoading = isAuthLoading
                        )
                    }

                    is com.example.data.auth.AuthState.AuthenticatedUnverified -> {
                        EmailVerificationScreen(
                            email = currAuth.email,
                            onResendVerification = { viewModel.sendVerificationEmail() },
                            onCheckVerificationStatus = {
                                viewModel.checkEmailVerification { isVerified ->
                                    if (!isVerified) {
                                        Toast.makeText(this@MainActivity, "Email not yet verified. Please click the link in your email.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onSignOut = { viewModel.signOut() }
                        )
                    }

                    is com.example.data.auth.AuthState.AuthenticatedProfileIncomplete -> {
                        var profileErrorMsg by remember { mutableStateOf<String?>(null) }
                        var isProfileLoading by remember { mutableStateOf(false) }

                        CompleteProfileScreen(
                            userEmail = currAuth.email,
                            userName = currAuth.displayName,
                            onLinkClinicRecord = { phoneOrEmail, claimCode ->
                                isProfileLoading = true
                                profileErrorMsg = null
                                viewModel.linkClinicRecord(phoneOrEmail, claimCode) { success, err ->
                                    isProfileLoading = false
                                    if (!success) profileErrorMsg = err
                                }
                            },
                            onSaveNewPet = { pet, phone ->
                                isProfileLoading = true
                                profileErrorMsg = null
                                viewModel.completeNewPetOwnerOnboarding(pet, phone) { success, err ->
                                    isProfileLoading = false
                                    if (!success) profileErrorMsg = err
                                }
                            },
                            onSignOut = { viewModel.signOut() },
                            errorMessage = profileErrorMsg,
                            isLoading = isProfileLoading
                        )
                    }

                    is com.example.data.auth.AuthState.AuthenticatedPetOwner, is com.example.data.auth.AuthState.AuthenticatedStaff -> {
                        // Determine active client for Pet Owner experience
                        val activeOwner = if (currAuth is com.example.data.auth.AuthState.AuthenticatedPetOwner) {
                            currAuth.client
                        } else {
                            state.clients.firstOrNull() ?: com.example.data.local.ClientEntity(
                                id = 1,
                                fullName = "Anthony Tolbert",
                                preferredName = "Anthony",
                                phone = "0881479329"
                            )
                        }

                        val activeUserName = when (currAuth) {
                            is com.example.data.auth.AuthState.AuthenticatedStaff -> currAuth.displayName
                            is com.example.data.auth.AuthState.AuthenticatedPetOwner -> currAuth.displayName
                            else -> "User"
                        }
                        val activeUserEmail = when (currAuth) {
                            is com.example.data.auth.AuthState.AuthenticatedStaff -> currAuth.email
                            is com.example.data.auth.AuthState.AuthenticatedPetOwner -> currAuth.email
                            else -> ""
                        }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    HappyPawsLogo(size = 36.dp, showTagline = false)
                                    Column {
                                        Text(
                                            text = state.settings?.clinicName ?: "Happy Paws Liberia",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = FontFamily.Serif,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = DeepCharcoal
                                        )
                                        Text(
                                            text = state.settings?.subTitle ?: "Rescue Center & Vet Clinic",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = AmberTerracotta
                                        )
                                    }
                                }
                            },
                            actions = {
                                // Role switcher indicator pill
                                Surface(
                                    onClick = {
                                        if (state.currentRole == UserRole.PET_OWNER) {
                                            viewModel.switchRole(UserRole.VETERINARIAN)
                                        } else {
                                            viewModel.switchRole(UserRole.PET_OWNER)
                                        }
                                    },
                                    color = if (state.currentRole == UserRole.PET_OWNER) SoftCream else SoftSage,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, if (state.currentRole == UserRole.PET_OWNER) AmberTerracotta.copy(alpha = 0.5f) else ForestSage.copy(alpha = 0.5f)),
                                    modifier = Modifier.defaultMinSize(minHeight = 44.dp).testTag("quick_role_toggle")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (state.currentRole == UserRole.PET_OWNER) Icons.Default.Pets else Icons.Default.MedicalServices,
                                            contentDescription = null,
                                            tint = if (state.currentRole == UserRole.PET_OWNER) AmberTerracotta else ForestSage,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = state.currentRole.displayName,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (state.currentRole == UserRole.PET_OWNER) AmberTerracotta else ForestSage
                                        )
                                    }
                                }

                                // Account / Auth Button
                                IconButton(
                                    onClick = { showAccountDialog = true },
                                    modifier = Modifier.testTag("auth_account_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "User Account",
                                        tint = DeepCharcoal
                                    )
                                }

                                // Notifications Icon with badge
                                IconButton(
                                    onClick = {
                                        if (state.currentRole == UserRole.PET_OWNER) {
                                            currentOwnerTab = 2 // Settings/Notifications
                                        } else {
                                            currentStaffTab = 4 // Settings
                                        }
                                    },
                                    modifier = Modifier.testTag("notifications_icon")
                                ) {
                                    BadgedBox(
                                        badge = {
                                            if (state.unreadNotifCount > 0) {
                                                Badge(containerColor = StatusRed) {
                                                    Text("${state.unreadNotifCount}")
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = "Notifications",
                                            tint = DeepCharcoal
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = WarmIvory
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = WarmIvory,
                            contentColor = DeepCharcoal,
                            tonalElevation = 8.dp,
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            if (state.currentRole == UserRole.PET_OWNER) {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Pets, contentDescription = "My Pets") },
                                    label = { Text("My Pets") },
                                    selected = currentOwnerTab == 0,
                                    onClick = { currentOwnerTab = 0 },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = AmberTerracotta,
                                        indicatorColor = SoftCream
                                    ),
                                    modifier = Modifier.testTag("nav_owner_pets")
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Appointments") },
                                    label = { Text("Visits") },
                                    selected = currentOwnerTab == 1,
                                    onClick = { currentOwnerTab = 1 },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = AmberTerracotta,
                                        indicatorColor = SoftCream
                                    ),
                                    modifier = Modifier.testTag("nav_owner_visits")
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                    label = { Text("Settings") },
                                    selected = currentOwnerTab == 2,
                                    onClick = { currentOwnerTab = 2 },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = AmberTerracotta,
                                        indicatorColor = SoftCream
                                    ),
                                    modifier = Modifier.testTag("nav_owner_settings")
                                )
                            } else {
                                // Staff Navigation
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Today") },
                                    label = { Text("Today") },
                                    selected = currentStaffTab == 0,
                                    onClick = { currentStaffTab = 0 },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = AmberTerracotta,
                                        indicatorColor = SoftCream
                                    ),
                                    modifier = Modifier.testTag("nav_staff_today")
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.MedicalServices, contentDescription = "Clinical") },
                                    label = { Text("Clinical") },
                                    selected = currentStaffTab == 1,
                                    onClick = { currentStaffTab = 1 },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = AmberTerracotta,
                                        indicatorColor = SoftCream
                                    ),
                                    modifier = Modifier.testTag("nav_staff_clinical")
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.People, contentDescription = "Clients") },
                                    label = { Text("Clients") },
                                    selected = currentStaffTab == 2,
                                    onClick = { currentStaffTab = 2 },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = AmberTerracotta,
                                        indicatorColor = SoftCream
                                    ),
                                    modifier = Modifier.testTag("nav_staff_clients")
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Inventory2, contentDescription = "Billing") },
                                    label = { Text("Billing") },
                                    selected = currentStaffTab == 3,
                                    onClick = { currentStaffTab = 3 },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = AmberTerracotta,
                                        indicatorColor = SoftCream
                                    ),
                                    modifier = Modifier.testTag("nav_staff_billing")
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                    label = { Text("Settings") },
                                    selected = currentStaffTab == 4,
                                    onClick = { currentStaffTab = 4 },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = AmberTerracotta,
                                        indicatorColor = SoftCream
                                    ),
                                    modifier = Modifier.testTag("nav_staff_settings")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        if (state.currentRole == UserRole.PET_OWNER) {
                            when (currentOwnerTab) {
                                0, 1 -> {
                                    PetOwnerDashboardScreen(
                                        currentOwner = activeOwner,
                                        pets = state.pets,
                                        appointments = state.appointments,
                                        vaccines = state.vaccinations,
                                        dewormings = state.dewormings,
                                        prescriptions = state.prescriptions,
                                        consultations = state.consultations,
                                        weights = state.weights,
                                        clinicSettings = state.settings,
                                        onAddPetClick = { showOnboardingWizard = true },
                                        onRequestAppointment = { petId, reason, isEmergency ->
                                            viewModel.requestAppointment(petId, reason, isEmergency)
                                        },
                                        onLogWeight = { petId, weightKg ->
                                            viewModel.recordWeight(
                                                com.example.data.local.WeightRecordEntity(
                                                    petId = petId,
                                                    weightKg = weightKg,
                                                    recordedDate = "2026-09-28"
                                                )
                                            )
                                        }
                                    )
                                }
                                2 -> {
                                    SettingsScreen(
                                        currentRole = state.currentRole,
                                        settings = state.settings,
                                        auditLogs = state.auditLogs,
                                        notifications = state.notifications,
                                        onRoleSelected = { viewModel.switchRole(it) },
                                        onSaveSettings = { viewModel.saveClinicSettings(it) },
                                        onMarkAllNotificationsRead = { viewModel.markAllNotificationsRead() }
                                    )
                                }
                            }
                        } else {
                            // Staff Views
                            when (currentStaffTab) {
                                0 -> {
                                    StaffTodayScreen(
                                        currentRole = state.currentRole,
                                        appointments = state.appointments,
                                        pets = state.pets,
                                        clients = state.clients,
                                        lowStockItems = state.lowStockInventory,
                                        notifications = state.notifications,
                                        vaccinations = state.vaccinations,
                                        dewormings = state.dewormings,
                                        consultations = state.consultations,
                                        onUpdateAppointmentStatus = { id, status ->
                                            viewModel.updateAppointmentStatus(id, status)
                                        },
                                        onStartConsultation = { petId, _ ->
                                            viewModel.selectPetForClinical(petId)
                                            currentStaffTab = 1
                                        },
                                        onQuickCheckIn = {
                                            currentStaffTab = 2
                                        },
                                        onRegisterNewClient = {
                                            currentStaffTab = 2
                                        },
                                        onRecordPayment = {
                                            currentStaffTab = 3
                                        },
                                        onCheckInPetToday = { petId, clientId ->
                                            viewModel.checkInPetToday(petId, clientId)
                                        }
                                    )
                                }
                                1 -> {
                                    StaffClinicalScreen(
                                        pets = state.pets,
                                        clients = state.clients,
                                        consultations = state.consultations,
                                        vaccines = state.vaccinations,
                                        dewormings = state.dewormings,
                                        parasiteRecords = state.parasiteRecords,
                                        prescriptions = state.prescriptions,
                                        weights = state.weights,
                                        clinicSettings = state.settings,
                                        preSelectedPetId = state.selectedPetForClinicalId,
                                        onSaveConsultation = { viewModel.saveConsultation(it) },
                                        onFinalizeConsultation = {},
                                        onAddVaccination = { viewModel.recordVaccination(it) },
                                        onAddDeworming = { viewModel.recordDeworming(it) },
                                        onAddParasiteRecord = { viewModel.recordParasite(it) },
                                        onAddPrescription = { viewModel.recordPrescription(it) },
                                        onAddWeight = { viewModel.recordWeight(it) }
                                    )
                                }
                                2 -> {
                                    StaffClientsPetsScreen(
                                        clients = state.clients,
                                        pets = state.pets,
                                        onAddClientAndPet = { client, pet ->
                                            viewModel.addClientAndPet(client, pet)
                                        },
                                        onSelectPetForClinical = { petId ->
                                            viewModel.selectPetForClinical(petId)
                                            currentStaffTab = 1
                                        }
                                    )
                                }
                                3 -> {
                                    StaffBillingInventoryScreen(
                                        inventory = state.inventory,
                                        invoices = state.invoices,
                                        payments = state.payments,
                                        clients = state.clients,
                                        pets = state.pets,
                                        clinicSettings = state.settings,
                                        onAdjustStock = { id, delta ->
                                            viewModel.adjustStock(id, delta)
                                        },
                                        onAddInventoryItem = { viewModel.addInventoryItem(it) },
                                        onCreateInvoice = { viewModel.createInvoice(it) },
                                        onRecordPayment = { viewModel.recordPayment(it) }
                                    )
                                }
                                4 -> {
                                    SettingsScreen(
                                        currentRole = state.currentRole,
                                        settings = state.settings,
                                        auditLogs = state.auditLogs,
                                        notifications = state.notifications,
                                        onRoleSelected = { viewModel.switchRole(it) },
                                        onSaveSettings = { viewModel.saveClinicSettings(it) },
                                        onMarkAllNotificationsRead = { viewModel.markAllNotificationsRead() }
                                    )
                                }
                            }
                        }

                        // Onboarding Wizard Modal Dialog
                        if (showOnboardingWizard) {
                            PetOnboardingWizardDialog(
                                ownerId = activeOwner.id,
                                ownerName = activeOwner.preferredName.ifEmpty { activeOwner.fullName },
                                onSavePet = { newPet, _ ->
                                    viewModel.addPetFromOnboarding(newPet)
                                },
                                onDismiss = { showOnboardingWizard = false }
                            )
                        }

                        // Account Details & Sign Out Modal Dialog
                        if (showAccountDialog) {
                            AlertDialog(
                                onDismissRequest = { showAccountDialog = false },
                                icon = {
                                    Icon(
                                        Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        tint = AmberTerracotta,
                                        modifier = Modifier.size(36.dp)
                                    )
                                },
                                title = {
                                    Text(
                                        text = activeUserName,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (activeUserEmail.isNotBlank()) {
                                            Text(
                                                text = activeUserEmail,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MediumCharcoal
                                            )
                                        }
                                        Text(
                                            text = "Active Role: ${state.currentRole.displayName}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = AmberTerracotta
                                        )
                                        if (currAuth is com.example.data.auth.AuthState.AuthenticatedPetOwner) {
                                            Text(
                                                text = "Clinic File ID: #${currAuth.client.id}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = SoftSlate
                                            )
                                        }
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            showAccountDialog = false
                                            viewModel.signOut()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                                        modifier = Modifier.testTag("account_sign_out_button")
                                    ) {
                                        Text("Sign Out")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showAccountDialog = false }) {
                                        Text("Close")
                                    }
                                }
                            )
                        }
                    }
                }
            } // End of when (val currAuth = authState)
        }
    }
}
}
}
