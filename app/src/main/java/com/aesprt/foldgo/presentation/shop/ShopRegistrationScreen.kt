package com.aesprt.foldgo.presentation.shop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aesprt.foldgo.presentation.components.FoldGoLogo
import com.aesprt.foldgo.presentation.components.ModernBackground
import com.aesprt.foldgo.ui.theme.FoldGoTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun ShopRegistrationScreen(
    onRegistrationSuccess: () -> Unit,
    viewModel: ShopRegistrationViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onRegistrationSuccess()
        }
    }

    ShopRegistrationContent(
        uiState = uiState,
        onShopNameChange = viewModel::onShopNameChange,
        onAddressChange = viewModel::onAddressChange,
        onMobileNumberChange = viewModel::onMobileNumberChange,
        onOwnerNameChange = viewModel::onOwnerNameChange,
        onPinChange = viewModel::onPinChange,
        onRegisterShop = viewModel::registerShop
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopRegistrationContent(
    uiState: ShopRegistrationUiState,
    onShopNameChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onMobileNumberChange: (String) -> Unit,
    onOwnerNameChange: (String) -> Unit,
    onPinChange: (String) -> Unit,
    onRegisterShop: () -> Unit
) {
    val scrollState = rememberScrollState()

    ModernBackground {
        Scaffold(
            containerColor = Color.Transparent
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    FoldGoLogo()

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Register Your Shop",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Set up your laundry business in seconds.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    OutlinedTextField(
                        value = uiState.shopName,
                        onValueChange = onShopNameChange,
                        label = { Text("Shop Name", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Rounded.Store, null) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = uiState.address,
                        onValueChange = onAddressChange,
                        label = {
                            Text(
                                "Shop Address",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = uiState.mobileNumber,
                        onValueChange = onMobileNumberChange,
                        label = { Text("Shop Mobile Number", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Rounded.Phone, null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        placeholder = { Text("09xxxxxxxxx", style = MaterialTheme.typography.bodySmall) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = uiState.ownerName,
                        onValueChange = onOwnerNameChange,
                        label = { Text("Owner Name", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = uiState.pin,
                        onValueChange = onPinChange,
                        label = {
                            Text(
                                "Set 4-Digit Shop PIN",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword
                        ),
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = onRegisterShop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Finish Setup", style = MaterialTheme.typography.titleMedium)
                        }
                    }

                    if (uiState.error != null) {
                        Text(
                            text = uiState.error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ShopRegistrationContentPreview() {
    FoldGoTheme {
        ShopRegistrationContent(
            uiState = ShopRegistrationUiState(
                shopName = "Quick Wash",
                address = "123 Street",
                mobileNumber = "+639123456789",
                ownerName = "John Doe",
                pin = "1234"
            ),
            onShopNameChange = {},
            onAddressChange = {},
            onMobileNumberChange = {},
            onOwnerNameChange = {},
            onPinChange = {},
            onRegisterShop = {}
        )
    }
}
