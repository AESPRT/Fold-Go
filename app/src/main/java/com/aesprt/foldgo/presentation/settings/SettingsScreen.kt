package com.aesprt.foldgo.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aesprt.foldgo.presentation.components.FoldGoLoading
import com.aesprt.foldgo.presentation.components.ModernBackground
import com.aesprt.foldgo.presentation.components.FoldGoLogo
import com.aesprt.foldgo.ui.theme.FoldGoTheme
import com.aesprt.foldgo.ui.theme.DeepOceanBlue
import com.aesprt.foldgo.ui.theme.MintGreen
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    onNavigateToShopInfo: (() -> Unit)? = null,
    onNavigateToServices: (() -> Unit)? = null,
    onNavigateToEquipmentSetup: (() -> Unit)? = null,
    onNavigateToSMS: (() -> Unit)? = null,
    onNavigateToNotifications: (() -> Unit)? = null,
    onNavigateToAppearance: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsContent(
        uiState = uiState,
        onLogout = onLogout,
        onEndShift = { onComplete ->
            viewModel.endShift(onComplete)
        },
        onNavigateToShopInfo = onNavigateToShopInfo,
        onNavigateToServices = onNavigateToServices,
        onNavigateToEquipmentSetup = onNavigateToEquipmentSetup,
        onNavigateToSMS = onNavigateToSMS,
        onNavigateToNotifications = onNavigateToNotifications,
        onNavigateToAppearance = onNavigateToAppearance,
        contentPadding = contentPadding
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onLogout: () -> Unit,
    onEndShift: (() -> Unit) -> Unit,
    onNavigateToShopInfo: (() -> Unit)? = null,
    onNavigateToServices: (() -> Unit)? = null,
    onNavigateToEquipmentSetup: (() -> Unit)? = null,
    onNavigateToSMS: (() -> Unit)? = null,
    onNavigateToNotifications: (() -> Unit)? = null,
    onNavigateToAppearance: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues()
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val onFeatureNotAvailable: (String) -> Unit = { featureName ->
        scope.launch {
            snackbarHostState.showSnackbar("$featureName is coming soon!")
        }
    }

    ModernBackground {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        FoldGoLogo(
                            iconSize = 32.dp,
                            title = "Settings",
                            supportingText = "Manage your account and preferences",
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        navigationIconContentColor = Color.Unspecified,
                        titleContentColor = Color.Unspecified,
                        actionIconContentColor = Color.Unspecified
                    )
                )
            },
            bottomBar = {
                // Reserve space for the floating bottom bar
                Spacer(modifier = Modifier.height(contentPadding.calculateBottomPadding()))
            }
        ) { padding ->
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    FoldGoLoading()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = padding.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr), 
                                 vertical = padding.calculateTopPadding())
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    Spacer(modifier = Modifier.height(6.dp))

                    // Profile Header - Redesigned for a more premium look
                    ProfileHeader(
                        staffName = uiState.staffName,
                        shopName = uiState.shop?.name ?: "Folding Shop"
                    )

                    // Shop Section - Cleaned up with subtle grouping
                    SettingsSection(title = "Management") {
                        SettingsItem(
                            icon = Icons.Rounded.Store,
                            iconColor = DeepOceanBlue,
                            title = "Shop Information",
                            subtitle = uiState.shop?.address ?: "Location and contact details",
                            onClick = { 
                                onNavigateToShopInfo?.invoke() ?: onFeatureNotAvailable("Shop Information")
                            }
                        )
                        SettingsItem(
                            icon = Icons.Rounded.LocalLaundryService,
                            iconColor = MintGreen,
                            title = "Laundry Services",
                            subtitle = "Prices, categories, and items",
                            onClick = {
                                onNavigateToServices?.invoke() ?: onFeatureNotAvailable("Laundry Services")
                            }
                        )
                        SettingsItem(
                            icon = Icons.Rounded.AddCircleOutline,
                            iconColor = Color(0xFF4CAF50),
                            title = "Equipment Setup",
                            subtitle = "Add new washing and drying machines",
                            onClick = {
                                onNavigateToEquipmentSetup?.invoke() ?: onFeatureNotAvailable("Equipment Setup")
                            }
                        )
                        SettingsItem(
                            icon = Icons.AutoMirrored.Rounded.Message,
                            iconColor = Color(0xFF673AB7),
                            title = "SMS Notifications",
                            subtitle = "Credits and customer alerts",
                            onClick = {
                                onNavigateToSMS?.invoke() ?: onFeatureNotAvailable("SMS Notifications")
                            }
                        )
                    }

                    // Preferences
                    SettingsSection(title = "App Experience") {
                        SettingsItem(
                            icon = Icons.Rounded.Notifications,
                            iconColor = Color(0xFFFF9800),
                            title = "Notifications",
                            subtitle = "Real-time alerts and system sounds",
                            onClick = {
                                onNavigateToNotifications?.invoke() ?: onFeatureNotAvailable("Notifications")
                            }
                        )
                        SettingsItem(
                            icon = Icons.Rounded.Palette,
                            iconColor = Color(0xFFE91E63),
                            title = "Appearance",
                            subtitle = "Personalize your workspace",
                            onClick = {
                                onNavigateToAppearance?.invoke() ?: onFeatureNotAvailable("Appearance")
                            }
                        )
                    }

                    // Shift Management - Critical action with distinct styling
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Account & Security",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .clickable { showLogoutDialog = true }
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(44.dp),
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.AutoMirrored.Rounded.Logout,
                                            null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "End Current Shift",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        "Securely log out of the current session",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                    )
                                }
                                Icon(
                                    Icons.Rounded.ChevronRight,
                                    null,
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Branding Footer
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FoldGoLogo(
                            iconSize = 32.dp,
                            showText = true,
                            textColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { 
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Rounded.Logout, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
                    }
                }
            },
            title = { Text("End Your Shift?") },
            text = { Text("You will be logged out and your current session will end. Ready to wrap up?") },
            confirmButton = {
                Button(
                    onClick = {
                        onEndShift {
                            onLogout()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("End Shift", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }
}


@Composable
fun ProfileHeader(staffName: String, shopName: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 8.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Subtle gradient accent
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(DeepOceanBlue.copy(alpha = 0.1f), Color.Transparent)
                        )
                    )
            )
            
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = DeepOceanBlue,
                    border = androidx.compose.foundation.BorderStroke(4.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            staffName.take(1).uppercase(),
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(20.dp))
                
                Column {
                    Surface(
                        color = MintGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "ACTIVE SESSION",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MintGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Text(
                        staffName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Storefront,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            shopName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(start = 8.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            color = iconColor.copy(alpha = 0.12f),
            shape = RoundedCornerShape(14.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon, 
                    null, 
                    tint = iconColor, 
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title, 
                style = MaterialTheme.typography.bodyLarge, 
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle, 
                style = MaterialTheme.typography.bodySmall, 
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            Icons.Rounded.ChevronRight,
            null,
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    FoldGoTheme {
        SettingsContent(
            uiState = SettingsUiState(
                staffName = "Adriel",
                shop = null
            ),
            onLogout = {},
            onEndShift = {}
        )
    }
}
