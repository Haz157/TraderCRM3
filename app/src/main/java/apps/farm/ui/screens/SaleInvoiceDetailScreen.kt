package apps.farm.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
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
import apps.farm.data.model.*
import apps.farm.ui.theme.*
import apps.farm.viewmodel.SaleInvoiceViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleInvoiceDetailScreen(
    invoiceId: String?,
    onNavigateBack: () -> Unit,
    invoiceViewModel: SaleInvoiceViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    
    // Form state
    val selectedFarm = invoiceViewModel.selectedFarm.collectAsState()
    val selectedCycle = invoiceViewModel.selectedCycle.collectAsState()
    val selectedCustomer = invoiceViewModel.selectedCustomer.collectAsState()
    val selectedSafe = invoiceViewModel.selectedSafe.collectAsState()
    val invoiceDate = invoiceViewModel.invoiceDate.collectAsState()
    val receiveAmount = invoiceViewModel.receiveAmount.collectAsState()
    val price = invoiceViewModel.price.collectAsState()
    val emptyWeights = invoiceViewModel.emptyWeights.collectAsState()
    val grossWeights = invoiceViewModel.grossWeights.collectAsState()
    val discountAmount = invoiceViewModel.discountAmount.collectAsState()
    val additionAmount = invoiceViewModel.additionAmount.collectAsState()
    
    // Selection lists from ViewModel
    val farmlist: List<Farm> by invoiceViewModel.activeFarms.collectAsState(initial = emptyList())
    val customerlist: List<CustomerWithBalance> by invoiceViewModel.activeCustomers.collectAsState(initial = emptyList())
    val safelist: List<Safe> by invoiceViewModel.activeSafes.collectAsState(initial = emptyList())
    val cyclelist: List<Cycle> by invoiceViewModel.cyclesByFarm.collectAsState()
    
    // UI state
    var showFarmDialog by remember { mutableStateOf(false) }
    var showCycleDialog by remember { mutableStateOf(false) }
    var showCustomerDialog by remember { mutableStateOf(false) }
    var showSafeDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showEmptyWeightDialog by remember { mutableStateOf(false) }
    var showGrossWeightDialog by remember { mutableStateOf(false) }
    var showMessage by remember { mutableStateOf<String?>(null) }
    
    // Load invoice if editing
    LaunchedEffect(invoiceId) {
        if (invoiceId != null) {
            invoiceViewModel.loadInvoice(invoiceId)
        }
    }
    
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    // Calculate totals
    val totalEmptyWeight = emptyWeights.value.sumOf { it.weight }
    val totalGrossWeight = grossWeights.value.sumOf { it.weight }
    val netWeight = totalGrossWeight - totalEmptyWeight
    val totalPriceValue = price.value * netWeight
    val finalInvoiceTotal = totalPriceValue + additionAmount.value - discountAmount.value
    val remainingValue = finalInvoiceTotal - receiveAmount.value

    fun formatNumber(value: Double): String {
         return String.format(Locale.US, "%,.2f", value)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (invoiceId == null) stringResource(R.string.title_add_invoice) else "تعديل فاتورة",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selection Cards
            SelectionCard(
                title = stringResource(R.string.label_farm),
                value = selectedFarm.value?.farmName ?: stringResource(R.string.placeholder_select_farm),
                icon = Icons.Default.Agriculture,
                onClick = { showFarmDialog = true }
            )
            
            SelectionCard(
                title = stringResource(R.string.label_cycle),
                value = selectedCycle.value?.cycleName ?: stringResource(R.string.placeholder_select_cycle),
                icon = Icons.Default.CalendarMonth,
                onClick = { showCycleDialog = true },
                enabled = selectedFarm.value != null
            )
            
            SelectionCard(
                title = stringResource(R.string.label_customer),
                value = selectedCustomer.value?.name ?: stringResource(R.string.placeholder_select_customer),
                icon = Icons.Default.Person,
                onClick = { showCustomerDialog = true },
                enabled = selectedCycle.value != null
            )
            
            SelectionCard(
                title = stringResource(R.string.label_safe),
                value = selectedSafe.value?.name ?: stringResource(R.string.placeholder_select_safe),
                icon = Icons.Default.AccountBalance,
                onClick = { showSafeDialog = true }
            )
            
            SelectionCard(
                title = stringResource(R.string.label_invoice_date),
                value = invoiceDate.value?.let { dateFormat.format(Date(it)) } ?: stringResource(R.string.placeholder_select_date),
                icon = Icons.Default.CalendarMonth,
                onClick = { showDatePicker = true },
                enabled = selectedCycle.value != null
            )
            
            // Amount Fields
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.section_amounts),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = TextPrimary
                    )
                    
                    UnifiedFormField(
                        value = if (receiveAmount.value == 0.0) "" else receiveAmount.value.toString(),
                        onValueChange = { invoiceViewModel.setReceiveAmount(it.toDoubleOrNull() ?: 0.0) },
                        label = stringResource(R.string.label_receive_amount),
                        icon = Icons.Default.AccountBalance,
                        placeholder = "0.0",
                        isDecimal = true,
                        maxDecimals = 3,
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                    
                    UnifiedFormField(
                        value = if (price.value == 0.0) "" else price.value.toString(),
                        onValueChange = { invoiceViewModel.setPrice(it.toDoubleOrNull() ?: 0.0) },
                        label = stringResource(R.string.label_price),
                        icon = Icons.Default.Receipt,
                        placeholder = "0.0",
                        isDecimal = true,
                        maxDecimals = 3,
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                    )

                    UnifiedFormField(
                        value = if (additionAmount.value == 0.0) "" else additionAmount.value.toString(),
                        onValueChange = { invoiceViewModel.setAdditionAmount(it.toDoubleOrNull() ?: 0.0) },
                        label = stringResource(R.string.label_addition_amount),
                        icon = Icons.Default.Add,
                        placeholder = "0.0",
                        isDecimal = true,
                        maxDecimals = 3,
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                    )

                    UnifiedFormField(
                        value = if (discountAmount.value == 0.0) "" else discountAmount.value.toString(),
                        onValueChange = { invoiceViewModel.setDiscountAmount(it.toDoubleOrNull() ?: 0.0) },
                        label = stringResource(R.string.label_discount_amount),
                        icon = Icons.Default.Close,
                        placeholder = "0.0",
                        isDecimal = true,
                        maxDecimals = 3,
                        imeAction = ImeAction.Done,
                        onImeAction = { focusManager.clearFocus() }
                    )
                }
            }
            
            // Weights Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.section_weights),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = TextPrimary
                        )
                        
                        Row {
                            FilledTonalButton(
                                onClick = { showEmptyWeightDialog = true },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = PrimaryContainer,
                                    contentColor = PrimaryDark
                                )
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.label_tab_empty), style = MaterialTheme.typography.labelSmall)
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            FilledTonalButton(
                                onClick = { showGrossWeightDialog = true },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = SecondaryContainer,
                                    contentColor = SecondaryDark
                                )
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.label_tab_gross), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    
                    // Empty Weights List
                    if (emptyWeights.value.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.label_empty_weight),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = TextSecondary
                        )
                        emptyWeights.value.forEach { weight ->
                            WeightItem(
                                weight = weight.weight,
                                count = weight.crateCount,
                                onDelete = { invoiceViewModel.removeEmptyWeight(weight) }
                            )
                        }
                    }
                    
                    // Gross Weights List
                    if (grossWeights.value.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.label_gross_weight),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = TextSecondary
                        )
                        grossWeights.value.forEach { weight ->
                            WeightItem(
                                weight = weight.weight,
                                count = weight.crateCount,
                                onDelete = { invoiceViewModel.removeGrossWeight(weight) }
                            )
                        }
                    }
                }
            }
            
            // Summary Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PrimaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.section_summary),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = PrimaryDark
                    )
                    
                    SummaryRow(
                        label = stringResource(R.string.label_total_empty_weight),
                        value = stringResource(R.string.format_weight_kg, formatNumber(totalEmptyWeight))
                    )
                    
                    SummaryRow(
                        label = stringResource(R.string.label_total_gross_weight),
                        value = stringResource(R.string.format_weight_kg, formatNumber(totalGrossWeight))
                    )
                    
                    SummaryRow(
                        label = stringResource(R.string.label_net_weight),
                        value = stringResource(R.string.format_weight_kg, formatNumber(netWeight))
                    )

                    SummaryRow(
                        label = stringResource(R.string.label_total_price),
                        value = formatNumber(totalPriceValue)
                    )

                    if (additionAmount.value > 0) {
                        SummaryRow(
                            label = stringResource(R.string.label_addition_amount),
                            value = formatNumber(additionAmount.value)
                        )
                    }

                    if (discountAmount.value > 0) {
                        SummaryRow(
                            label = stringResource(R.string.label_discount_amount),
                            value = formatNumber(discountAmount.value)
                        )
                    }
                    
                    HorizontalDivider(color = PrimaryDark.copy(alpha = 0.3f))
                    
                    SummaryRow(
                        label = stringResource(R.string.label_total_invoice),
                        value = formatNumber(finalInvoiceTotal),
                        isLarge = true
                    )

                    SummaryRow(
                        label = stringResource(R.string.label_receive_amount),
                        value = formatNumber(receiveAmount.value)
                    )

                    HorizontalDivider(color = PrimaryDark.copy(alpha = 0.3f))

                    SummaryRow(
                        label = stringResource(R.string.label_remaining),
                        value = formatNumber(remainingValue),
                        isLarge = true
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Save Button
            FilledTonalButton(
                onClick = {
                    if (invoiceId == null) {
                        invoiceViewModel.createInvoice(
                            onSuccess = { onNavigateBack() },
                            onError = { showMessage = it }
                        )
                    } else {
                        invoiceViewModel.updateInvoice(
                            onSuccess = { onNavigateBack() },
                            onError = { showMessage = it }
                        )
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
                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (invoiceId == null) stringResource(R.string.button_add_invoice) else "تعديل فاتورة",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Dialogs Implementation
    if (showFarmDialog) {
        FarmSelectionDialog(
            farms = farmlist,
            onDismiss = { showFarmDialog = false },
            onSelect = { invoiceViewModel.selectFarm(it) }
        )
    }

    if (showCycleDialog) {
        CycleSelectionDialog(
            cycles = cyclelist,
            onDismiss = { showCycleDialog = false },
            onSelect = { invoiceViewModel.selectCycle(it) }
        )
    }

    if (showCustomerDialog) {
        CustomerSelectionDialog(
            customers = customerlist,
            onDismiss = { showCustomerDialog = false },
            onSelect = { invoiceViewModel.selectCustomer(it) }
        )
    }

    if (showSafeDialog) {
        SafeSelectionDialog(
            safes = safelist,
            onDismiss = { showSafeDialog = false },
            onSelect = { invoiceViewModel.selectSafe(it) }
        )
    }

    if (showEmptyWeightDialog) {
        WeightDialog(
            title = stringResource(R.string.label_empty_weight),
            onDismiss = { showEmptyWeightDialog = false },
            onAdd = { weight, count -> invoiceViewModel.addEmptyWeight(weight, count) }
        )
    }

    if (showGrossWeightDialog) {
        WeightDialog(
            title = stringResource(R.string.label_gross_weight),
            onDismiss = { showGrossWeightDialog = false },
            onAdd = { weight, count -> invoiceViewModel.addGrossWeight(weight, count) }
        )
    }

    if (showDatePicker) {
        val context = androidx.compose.ui.platform.LocalContext.current
        val calendar = Calendar.getInstance()
        invoiceDate.value?.let { calendar.timeInMillis = it }
        
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                invoiceViewModel.setInvoiceDate(selectedCalendar.timeInMillis)
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            // Set min/max date based on cycle if needed
            selectedCycle.value?.let {
                datePicker.minDate = it.sd
                datePicker.maxDate = it.ed
            }
        }.show()
        
        // Immediate reset because DatePickerDialog is external
        showDatePicker = false
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
fun SelectionCard(
    title: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.surface else SurfaceLight,
            contentColor = if (enabled) MaterialTheme.colorScheme.onSurface else TextDisabled
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (enabled) 2.dp else 0.dp
        ),
        modifier = Modifier.fillMaxWidth()
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
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp),
                        tint = PrimaryDark
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = if (enabled) TextPrimary else TextDisabled
                    )
                }
            }
        }
    }
}

@Composable
fun WeightItem(
    weight: Double,
    count: Int,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.format_weight_count_crates, weight.toString(), count.toString()),
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary
        )
        
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = stringResource(R.string.content_description_delete),
                tint = StatusBlocked,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun SummaryRow(
    label: String,
    value: String,
    isLarge: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isLarge) 
                MaterialTheme.typography.bodyMedium 
            else 
                MaterialTheme.typography.bodySmall,
            color = PrimaryDark
        )
        Text(
            text = value,
            style = if (isLarge) 
                MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            else 
                MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = PrimaryDark
        )
    }
}
