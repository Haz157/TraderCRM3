package apps.farm.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import apps.farm.R
import apps.farm.data.model.Customer
import apps.farm.ui.theme.*
import apps.farm.viewmodel.CustomerViewModel
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customerId: String?,
    onNavigateBack: () -> Unit,
    onNavigateToPdfViewer: (String) -> Unit,
    customerViewModel: CustomerViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isBlocked by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var showMessage by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf(false) }
    
    // PDF related states
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var isGeneratingPdf by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val allCustomers by customerViewModel.allCustomers.collectAsState(initial = emptyList())

    LaunchedEffect(customerId) {
        if (customerId != null) {
            isEditing = true
            customerViewModel.getCustomerById(customerId)?.let { customer ->
                name = customer.name
                balance = if (customer.balance == 0.0) "" else customer.balance.toString()
                phone = customer.phone
                address = customer.address
                note = customer.note
                isBlocked = customer.blocked
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (isEditing) stringResource(R.string.title_edit_customer) else stringResource(R.string.title_add_customer),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.content_description_back),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Main Unified Card with all fields
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Customer Name Field
                    UnifiedFormField(
                        value = name,
                        onValueChange = { 
                            name = it
                            nameError = false
                        },
                        label = stringResource(R.string.label_customer_name),
                        icon = Icons.Default.Person,
                        placeholder = stringResource(R.string.label_customer_name),
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                    
                    if (nameError) {
                        Text(
                            text = stringResource(R.string.error_customer_name_exists),
                            color = StatusBlocked,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 56.dp, top = 2.dp)
                        )
                    }

                    // Divider
                    HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)

                    // Balance Field
                    UnifiedFormField(
                        value = balance,
                        onValueChange = { balance = it },
                        label = stringResource(R.string.label_opening_balance),
                        icon = Icons.Default.AccountBalance,
                        placeholder = "0.0",
                        isDecimal = true,
                        maxDecimals = 3,
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                    )

                    // Divider
                    HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)

                    // Phone Field
                    UnifiedFormField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = stringResource(R.string.label_phone_number),
                        icon = Icons.Default.Phone,
                        placeholder = stringResource(R.string.label_phone_number),
                        isPhone = true,
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                    )

                    // Divider
                    HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)

                    // Address Field
                    UnifiedFormField(
                        value = address,
                        onValueChange = { address = it },
                        label = stringResource(R.string.label_address),
                        icon = Icons.Default.LocationOn,
                        placeholder = stringResource(R.string.label_address),
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                    )

                    // Divider
                    HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)

                    // Notes Field
                    UnifiedFormField(
                        value = note,
                        onValueChange = { note = it },
                        label = stringResource(R.string.label_notes),
                        icon = Icons.Default.EditNote,
                        placeholder = stringResource(R.string.label_notes),
                        minLines = 3,
                        imeAction = ImeAction.Done,
                        onImeAction = { focusManager.clearFocus() }
                    )
                }
            }

            if (isEditing) {
                // Status Card
                StatusCard3(
                    isBlocked = isBlocked,
                    onToggle = { isBlocked = !isBlocked }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Print Statement Button
                Button(
                    onClick = { showDatePickerDialog = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    if (isGeneratingPdf) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.EditNote, contentDescription = null) // Using a placeholder icon as print is not directly available in standard material icons easily
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.label_print_statement), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            val errorCustomerNameRequired = stringResource(R.string.error_customer_name_required)

            //  Save Button
            FilledTonalButton(
                onClick = {
                    if (name.isBlank()) {
                        showMessage = errorCustomerNameRequired
                        return@FilledTonalButton
                    }

                    // Check if name is unique
                    val isNameUnique = if (isEditing) {
                        allCustomers.none { it.customer.id != customerId && it.customer.name.equals(name, ignoreCase = true) }
                    } else {
                        allCustomers.none { it.customer.name.equals(name, ignoreCase = true) }
                    }
                    
                    if (!isNameUnique) {
                        nameError = true
                        return@FilledTonalButton
                    }

                    val balanceValue = balance.toDoubleOrNull() ?: 0.0

                    val customer = Customer(
                        id = customerId ?: UUID.randomUUID().toString(),
                        name = name,
                        balance = balanceValue,
                        phone = phone,
                        address = address,
                        note = note,
                        blocked = isBlocked
                    )

                    scope.launch {
                        if (isEditing) {
                            customerViewModel.updateCustomer(customer) { success, message ->
                                if (success) {
                                    onNavigateBack()
                                } else {
                                    showMessage = message
                                }
                            }
                        } else {
                            customerViewModel.insertCustomer(customer) { success, message ->
                                if (success) {
                                    onNavigateBack()
                                } else {
                                    showMessage = message
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = if (isEditing) Icons.Default.CheckCircle else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isEditing) stringResource(R.string.button_save_changes) else stringResource(R.string.button_add_customer),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
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

    if (showDatePickerDialog) {
        DateRangePickerDialog(
            onDismiss = { showDatePickerDialog = false },
            onConfirm = { startDate, endDate ->
                showDatePickerDialog = false
                isGeneratingPdf = true
                customerId?.let { id ->
                    customerViewModel.generateCustomerStatement(
                        context = context,
                        customerId = id,
                        startDate = startDate,
                        endDate = endDate,
                        onPdfGenerated = { file ->
                            isGeneratingPdf = false
                            if (file != null) {
                                onNavigateToPdfViewer(file.absolutePath)
                            } else {
                                showMessage = "Error generating PDF"
                            }
                        }
                    )
                }
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusCard3(
    isBlocked: Boolean,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isBlocked) StatusBlockedContainer else StatusActiveContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isBlocked) StatusBlocked else StatusActive,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isBlocked) Icons.Default.Block else Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(20.dp),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.label_status, 
                            if (isBlocked) stringResource(R.string.status_blocked) 
                            else stringResource(R.string.status_active)
                        ),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (isBlocked) StatusBlocked else StatusActive
                    )
                }
            }
            Switch(
                checked = !isBlocked,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = StatusActive,
                    checkedTrackColor = StatusActiveContainer,
                    uncheckedThumbColor = StatusBlocked,
                    uncheckedTrackColor = StatusBlockedContainer
                )
            )
        }
    }
}
