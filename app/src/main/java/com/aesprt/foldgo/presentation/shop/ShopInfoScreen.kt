package com.aesprt.foldgo.presentation.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aesprt.foldgo.domain.model.Shop
import com.aesprt.foldgo.presentation.components.FoldGoLoading
import com.aesprt.foldgo.presentation.components.ModernBackground
import com.aesprt.foldgo.ui.theme.DeepOceanBlue
import com.aesprt.foldgo.ui.theme.FoldGoTheme
import com.aesprt.foldgo.ui.theme.MintGreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun ShopInfoScreen(
    onNavigateBack: () -> Unit,
    viewModel: ShopInfoViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isUpdateSuccess) {
        if (uiState.isUpdateSuccess) {
            viewModel.clearSuccess()
            onNavigateBack()
        }
    }

    ShopInfoContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onUpdateShop = viewModel::updateShop
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopInfoContent(
    uiState: ShopInfoUiState,
    onNavigateBack: () -> Unit,
    onUpdateShop: (Shop) -> Unit
) {
    var shopName by remember(uiState.shop) { mutableStateOf(uiState.shop?.name ?: "") }
    var address by remember(uiState.shop) { mutableStateOf(uiState.shop?.address ?: "") }
    var mobileNumber by remember(uiState.shop) { mutableStateOf(uiState.shop?.mobileNumber ?: "") }

    ModernBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Shop Information", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if (!uiState.isLoading && uiState.shop != null) {
                            IconButton(
                                onClick = {
                                    uiState.shop.let {
                                        onUpdateShop(
                                            it.copy(
                                                name = shopName,
                                                address = address,
                                                mobileNumber = mobileNumber
                                            )
                                        )
                                    }
                                },
                                enabled = !uiState.isSaving
                            ) {
                                if (uiState.isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Icon(Icons.Rounded.Save, contentDescription = "Save")
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
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
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    // Immersive Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        
                        // Shop Icon / Avatar
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                modifier = Modifier.size(110.dp),
                                shape = RoundedCornerShape(36.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 12.dp,
                                tonalElevation = 2.dp
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(DeepOceanBlue, MintGreen)
                                            ),
                                            alpha = 0.1f
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.Storefront,
                                        contentDescription = null,
                                        modifier = Modifier.size(52.dp),
                                        tint = DeepOceanBlue
                                    )
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Text(
                            text = "General Information",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(32.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp,
                            shadowElevation = 2.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                ShopInfoField(
                                    label = "Shop Name",
                                    value = shopName,
                                    onValueChange = { shopName = it },
                                    icon = Icons.Rounded.Badge,
                                    placeholder = "e.g. Fold&Go Center"
                                )

                                ShopInfoField(
                                    label = "Contact Number",
                                    value = mobileNumber,
                                    onValueChange = { mobileNumber = it },
                                    icon = Icons.Rounded.Phone,
                                    placeholder = "09xxxxxxxxx",
                                    keyboardType = KeyboardType.Phone
                                )

                                ShopInfoField(
                                    label = "Location Address",
                                    value = address,
                                    onValueChange = { address = it },
                                    icon = Icons.Rounded.LocationOn,
                                    placeholder = "Full business address",
                                    singleLine = false,
                                    minLines = 3
                                )
                            }
                        }
                        
                        // Help Card
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "This information will be displayed on customer receipts and SMS notifications.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    if (uiState.error != null) {
                        Surface(
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = uiState.error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ShopInfoScreenPreview() {
    FoldGoTheme {
        ShopInfoContent(
            uiState = ShopInfoUiState(
                shop = Shop(
                    shopId = "1",
                    name = "Fold&Go Center",
                    address = "123 Laundry St.",
                    mobileNumber = "09123456789",
                    ownerId = "owner1",
                    pin = "1234",
                    settings = emptyMap(),
                    createdAt = System.currentTimeMillis()
                )
            ),
            onNavigateBack = {},
            onUpdateShop = {}
        )
    }
}

@Composable
private fun ShopInfoField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent
            ),
            textStyle = MaterialTheme.typography.bodyLarge
        )
    }
}
