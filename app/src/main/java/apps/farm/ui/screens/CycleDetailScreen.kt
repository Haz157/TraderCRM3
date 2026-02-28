package apps.farm.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import apps.farm.R
import apps.farm.data.model.Cycle
import apps.farm.ui.theme.*
import apps.farm.viewmodel.CycleViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycleDetailScreen(
    cycleId: String?,
    farmId: String,
    onNavigateBack: () -> Unit,
    onNavigateToPdfViewer: (String) -> Unit,
    cycleViewModel: CycleViewModel = hiltViewModel()
) {
    var cycleName by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var isActive by remember { mutableStateOf(true) }
    var isEditing by remember { mutableStateOf(false) }
    var showMessage by remember { mutableStateOf<String?>(null) }
    var isGeneratingPdf by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(cycleId) {
        if (cycleId != null) {
            isEditing = true
            cycleViewModel.getCycleById(cycleId)?.let { cycle ->
                cycleName = cycle.cycleName
                startDate = Date(cycle.sd)
                endDate = Date(cycle.ed)
                isActive = cycle.isActive
            }
        }
    }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun showDatePicker(isStart: Boolean) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                if (isStart) {
                    startDate = selectedCalendar.time
                } else {
                    endDate = selectedCalendar.time
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (isEditing) stringResource(R.string.title_edit_cycle) else stringResource(R.string.title_add_cycle),
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
                    // Cycle Name Field
                    UnifiedFormField(
                        value = cycleName,
                        onValueChange = { cycleName = it },
                        label = stringResource(R.string.label_cycle_name),
                        icon = Icons.Default.Agriculture,
                        placeholder = stringResource(R.string.label_cycle_name),
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                    )

                    // Divider
                    HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)

                    // Start Date Field
                    UnifiedDateField(
                        value = startDate?.let { dateFormat.format(it) } ?: "",
                        onClick = { showDatePicker(true) },
                        label = stringResource(R.string.label_start_date),
                        icon = Icons.Default.DateRange,
                        placeholder = stringResource(R.string.label_start_date)
                    )

                    // Divider
                    HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)

                    // End Date Field
                    UnifiedDateField(
                        value = endDate?.let { dateFormat.format(it) } ?: "",
                        onClick = { showDatePicker(false) },
                        label = stringResource(R.string.label_end_date),
                        icon = Icons.Default.CalendarMonth,
                        placeholder = stringResource(R.string.label_end_date)
                    )
                }
            }

            if (isEditing) {
                // Status Card
                StatusCard2(
                    isActive = isActive,
                    onToggle = { isActive = !isActive }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Print Cycle Report Button
                Button(
                    onClick = {
                        isGeneratingPdf = true
                        cycleId?.let { id ->
                            cycleViewModel.generateDetailedCycleReport(context, id) { file ->
                                isGeneratingPdf = false
                                if (file != null) {
                                    onNavigateToPdfViewer(file.absolutePath)
                                } else {
                                    showMessage = "Error generating PDF"
                                }
                            }
                        }
                    },
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
                        Icon(Icons.Default.EditNote, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Print Cycle Report", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            val errorCycleNameRequired = stringResource(R.string.error_cycle_name_required)
            val errorDatesRequired = stringResource(R.string.error_dates_required)
            val errorStartDateAfterEnd = stringResource(R.string.error_start_date_after_end)

            //  Save Button
            FilledTonalButton(
                onClick = {
                    if (cycleName.isBlank()) {
                        showMessage = errorCycleNameRequired
                        return@FilledTonalButton
                    }
                    if (startDate == null || endDate == null) {
                        showMessage = errorDatesRequired
                        return@FilledTonalButton
                    }
                    if (startDate!!.after(endDate)) {
                        showMessage = errorStartDateAfterEnd
                        return@FilledTonalButton
                    }

                    val cycle = Cycle(
                        id = cycleId ?: UUID.randomUUID().toString(),
                        farmId = farmId,
                        cycleName = cycleName,
                        sd = startDate!!.time,
                        ed = endDate!!.time,
                        isActive = isActive
                    )

                    if (isEditing) {
                        cycleViewModel.updateCycle(cycle) { success, message ->
                            showMessage = message
                            if (success) {
                                onNavigateBack()
                            }
                        }
                    } else {
                        cycleViewModel.insertCycle(cycle) { success, message ->
                            showMessage = message
                            if (success) {
                                onNavigateBack()
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
                    imageVector = if (isEditing) Icons.Default.CalendarToday else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isEditing) stringResource(R.string.button_save_changes) else stringResource(R.string.button_add_cycle),
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
}


@Composable
fun UnifiedDateField(
    value: String,
    onClick: () -> Unit,
    label: String,
    icon: ImageVector,
    placeholder: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon container
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = PrimaryContainer,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(10.dp)
                    .size(24.dp),
                tint = PrimaryDark
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Date field with picker
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = TextSecondary
            )
            OutlinedTextField(
                value = value,
                onValueChange = { },
                placeholder = { Text(placeholder, color = TextDisabled) },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryLight,
                    unfocusedBorderColor = SurfaceVariant,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                trailingIcon = {
                    IconButton(onClick = onClick) {
                        Icon(Icons.Default.DateRange, contentDescription = stringResource(R.string.content_description_select_date))
                    }
                },
                textStyle = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusCard2(
    isActive: Boolean,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) StatusActiveContainer else StatusBlockedContainer
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
                    color = if (isActive) StatusActive else StatusBlocked,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.Block,
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
                            if (isActive) stringResource(R.string.status_active) 
                            else stringResource(R.string.status_inactive)
                        ),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (isActive) StatusActive else StatusBlocked
                    )
                }
            }
            Switch(
                checked = isActive,
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
