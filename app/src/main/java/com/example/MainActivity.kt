package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.AuthState
import com.example.data.local.PetEntity
import com.example.model.UserRole
import com.example.ui.components.CameraScannerDialog
import com.example.ui.components.PetDossierDialog
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.HappyPawsViewModel

sealed class StaffTab(val title: String, val icon: ImageVector) {
    object Today : StaffTab("Today", Icons.Default.Dashboard)
    object Clinical : StaffTab("Clinical", Icons.Default.MedicalServices)
    object Clients : StaffTab("Clients", Icons.Default.People)
    object Billing : StaffTab("Billing", Icons.Default.Receipt)
    object Settings : StaffTab("Settings", Icons.Default.Settings)
}

sealed class PetOwnerTab(val title: String, val icon: ImageVector) {
    object MyPets : PetOwnerTab("My Pets", Icons.Default.Pets)
    object Visits : PetOwnerTab("Visits", Icons.Default.CalendarMonth)
    object Settings : PetOwnerTab("Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: HappyPawsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HappyPawsTheme {
                val authState by viewModel.authState.collectAsState()

                when (val state = authState) {
                    is AuthState.Unauthenticated -> {
                        AuthScreen(
                            authRepo = viewModel.authRepo,
                            onLoginSuccess = { /* Automatically handled via authState */ }
                        )
                    }
                    is AuthState.Authenticated -> {
                        MainAppScaffold(
                            viewModel = viewModel,
                            currentUser = state
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    viewModel: HappyPawsViewModel,
    currentUser: AuthState.Authenticated
) {
    val role = currentUser.role
    val pets by viewModel.allPets.collectAsState()
    val ownerPet = pets.firstOrNull { it.id == 1L } ?: pets.firstOrNull() ?: com.example.data.local.PetEntity(
        id = 1,
        ownerClientId = 1,
        name = "Bella",
        species = "Canine (Dog)",
        breed = "African Boerboel Mix",
        sex = "Spayed Female",
        dob = "12 Jan 2021",
        weightKg = 24.5,
        color = "Brindle",
        microchipId = "985141002931882",
        rabiesTag = "HP-LR-2024-0884"
    )

    var currentStaffTab by remember { mutableStateOf<StaffTab>(StaffTab.Today) }
    var currentPetOwnerTab by remember { mutableStateOf<PetOwnerTab>(PetOwnerTab.MyPets) }
    var showGlobalScanner by remember { mutableStateOf(false) }
    var scannedPetForDossier by remember { mutableStateOf<PetEntity?>(null) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Happy Paws",
                            fontWeight = FontWeight.Bold,
                            color = DeepCharcoal,
                            fontSize = 18.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (role == UserRole.SUPER_ADMIN) TerracottaLight else SageLight,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text(
                                text = role.displayName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (role == UserRole.SUPER_ADMIN) AmberTerracottaDark else ForestSage,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showGlobalScanner = true }) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = "Scan Tag / QR",
                            tint = AmberTerracotta
                        )
                    }
                    IconButton(onClick = { viewModel.signOut() }) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = "Sign Out",
                            tint = DeepCharcoal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            // Enhanced Bottom Navigation Bar with dark visible inactive text/icons
            NavigationBar(
                containerColor = Color.White,
                contentColor = DeepCharcoal,
                tonalElevation = 6.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                if (role == UserRole.PET_OWNER) {
                    val tabs = listOf(PetOwnerTab.MyPets, PetOwnerTab.Visits, PetOwnerTab.Settings)
                    tabs.forEach { tab ->
                        val selected = currentPetOwnerTab == tab
                        NavigationBarItem(
                            selected = selected,
                            onClick = { currentPetOwnerTab = tab },
                            icon = {
                                Icon(
                                    tab.icon,
                                    contentDescription = tab.title,
                                    tint = if (selected) AmberTerracotta else DeepCharcoal
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) AmberTerracotta else DeepCharcoal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AmberTerracotta,
                                unselectedIconColor = DeepCharcoal,
                                selectedTextColor = AmberTerracotta,
                                unselectedTextColor = DeepCharcoal,
                                indicatorColor = TerracottaLight
                            )
                        )
                    }
                } else {
                    // Exact 5 Staff Sections: Today | Clinical | Clients | Billing | Settings
                    val tabs = listOf(StaffTab.Today, StaffTab.Clinical, StaffTab.Clients, StaffTab.Billing, StaffTab.Settings)
                    tabs.forEach { tab ->
                        val selected = currentStaffTab == tab
                        NavigationBarItem(
                            selected = selected,
                            onClick = { currentStaffTab = tab },
                            icon = {
                                Icon(
                                    tab.icon,
                                    contentDescription = tab.title,
                                    tint = if (selected) AmberTerracotta else DeepCharcoal,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) AmberTerracotta else DeepCharcoal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AmberTerracotta,
                                unselectedIconColor = DeepCharcoal,
                                selectedTextColor = AmberTerracotta,
                                unselectedTextColor = DeepCharcoal,
                                indicatorColor = TerracottaLight
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (role == UserRole.PET_OWNER) {
                when (currentPetOwnerTab) {
                    is PetOwnerTab.MyPets -> PetOwnerDashboardScreen(viewModel = viewModel, ownerPet = ownerPet, currentRole = role)
                    is PetOwnerTab.Visits -> PetOwnerDashboardScreen(viewModel = viewModel, ownerPet = ownerPet, currentRole = role)
                    is PetOwnerTab.Settings -> SettingsScreen(viewModel = viewModel, currentRole = role, onSignOut = { viewModel.signOut() })
                }
            } else {
                when (currentStaffTab) {
                    is StaffTab.Today -> StaffTodayScreen(viewModel = viewModel, currentRole = role)
                    is StaffTab.Clinical -> StaffClinicalScreen(viewModel = viewModel)
                    is StaffTab.Clients -> StaffClientsPetsScreen(viewModel = viewModel)
                    is StaffTab.Billing -> StaffBillingInventoryScreen(viewModel = viewModel)
                    is StaffTab.Settings -> SettingsScreen(viewModel = viewModel, currentRole = role, onSignOut = { viewModel.signOut() })
                }
            }
        }
    }

    if (showGlobalScanner) {
        CameraScannerDialog(
            pets = pets,
            currentRole = role,
            onDismiss = { showGlobalScanner = false },
            onPetVerified = { pet ->
                showGlobalScanner = false
                scannedPetForDossier = pet
            }
        )
    }

    scannedPetForDossier?.let { pet ->
        PetDossierDialog(
            pet = pet,
            currentRole = role,
            onAdmitToQueue = {
                viewModel.checkInPetToday(pet.id, "Owner of ${pet.name}", pet.name, pet.species)
                scannedPetForDossier = null
                Toast.makeText(context, "${pet.name} admitted to Today's Clinic Queue.", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { scannedPetForDossier = null }
        )
    }
}
