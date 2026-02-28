package apps.farm.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import apps.farm.R
import apps.farm.ui.theme.*
import apps.farm.viewmodel.CustomerViewModel
import apps.farm.viewmodel.ReceiveViewModel
import apps.farm.viewmodel.SafeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveDetailScreen(
    receiveId: String?,
    onNavigateBack: () -> Unit,
    receiveViewModel: ReceiveViewModel = hiltViewModel(),
    customerViewModel: CustomerViewModel = hiltViewModel(),
    safeViewModel: SafeViewModel = hiltViewModel()
) {
    val customers by customerViewModel.allCustomers.collectAsState(initial = emptyList())
    val selectedCustomer by receiveViewModel.selectedCustomer.collectAsState()
    val receiveAmount by receiveViewModel.receiveAmount.collectAsState()
    val discountAmount by receiveViewModel.discountAmount.collectAsState()
    val note by receiveViewModel.note.collectAsState()
    val customerBalance by receiveViewModel.customerBalance.collectAsState()
    val safes by receiveViewModel.activeSafes.collectAsState(initial = emptyList())
    val selectedSafe by receiveViewModel.selectedSafe.collectAsState()
    
    var showCustomerDialog by remember { mutableStateOf(false) }
    var showSafeDialog by remember { mutableStateOf(false) }
    var showMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(receiveId) {
        if (receiveId != null) {
            receiveViewModel.loadReceive(receiveId)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (receiveId == null) stringResource(R.string.title_add_receive) else stringResource(R.string.title_edit_receive),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_description_back))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Customer Selection
            Card(
                onClick = { showCustomerDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(8.dp), tint = PrimaryDark)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(stringResource(R.string.label_customer), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text(
                            text = selectedCustomer?.name ?: stringResource(R.string.placeholder_select_customer),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (selectedCustomer != null) TextPrimary else TextDisabled
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                }
            }

            // Safe Selection
            Card(
                onClick = { showSafeDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SecondaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.padding(8.dp), tint = SecondaryDark)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(stringResource(R.string.label_safe), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text(
                            text = selectedSafe?.name ?: stringResource(R.string.placeholder_select_safe),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (selectedSafe != null) TextPrimary else TextDisabled
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                }
            }

            // Amounts Section
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(R.string.section_amounts), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    
                    UnifiedFormField(
                        value = if (receiveAmount == 0.0) "" else receiveAmount.toString(),
                        onValueChange = { receiveViewModel.setReceiveAmount(it.toDoubleOrNull() ?: 0.0) },
                        label = stringResource(R.string.label_receive_amount),
                        icon = Icons.Default.AttachMoney,
                        placeholder = "0.0",
                        isDecimal = true
                    )

                    UnifiedFormField(
                        value = if (discountAmount == 0.0) "" else discountAmount.toString(),
                        onValueChange = { receiveViewModel.setDiscountAmount(it.toDoubleOrNull() ?: 0.0) },
                        label = stringResource(R.string.label_discount_amount),
                        icon = Icons.Default.CardGiftcard,
                        placeholder = "0.0",
                        isDecimal = true
                    )
                }
            }

            // Notes Section
            UnifiedFormField(
                value = note,
                onValueChange = { receiveViewModel.setNote(it) },
                label = stringResource(R.string.label_notes),
                icon = Icons.AutoMirrored.Filled.Note,
                placeholder = stringResource(R.string.label_notes),
                minLines = 3
            )

            // Balance Summary
            if (receiveId == null && selectedCustomer != null) {
                val newBalance = customerBalance - receiveAmount - discountAmount
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryContainer.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        BalanceRow(label = stringResource(R.string.label_current_balance), value = customerBalance.toString())
                        BalanceRow(label = stringResource(R.string.label_new_balance), value = newBalance.toString(), isBold = true)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            Button(
                onClick = {
                    if (receiveId == null) {
                        receiveViewModel.createReceive(
                            onSuccess = { onNavigateBack() },
                            onError = { showMessage = it }
                        )
                    } else {
                        receiveViewModel.updateReceive(
                            receiveId = receiveId,
                            onSuccess = { onNavigateBack() },
                            onError = { showMessage = it }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = selectedCustomer != null && selectedSafe != null && receiveAmount > 0
            ) {
                Icon(if (receiveId == null) Icons.Default.Save else Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (receiveId == null) stringResource(R.string.button_add_receive) else "حفظ التعديلات",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }

    if (showSafeDialog) {
        SafeSelectionDialog(
            safes = safes,
            onDismiss = { showSafeDialog = false },
            onSelect = { receiveViewModel.selectSafe(it) }
        )
    }

    if (showCustomerDialog) {
        CustomerSelectionDialog(
            customers = customers,
            onDismiss = { showCustomerDialog = false },
            onSelect = { receiveViewModel.selectCustomer(it) }
        )
    }

    showMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { showMessage = null },
            confirmButton = {
                TextButton(onClick = { showMessage = null }) {
                    Text(stringResource(R.string.dialog_button_ok))
                }
            },
            title = { Text(stringResource(R.string.dialog_title_alert)) },
            text = { Text(message) }
        )
    }
}

@Composable
fun BalanceRow(label: String, value: String, isBold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = if (isBold) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium,
            color = if (value.startsWith("-")) BalanceNegative else BalancePositive
        )
    }
}
