package apps.farm.ui.screens

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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import apps.farm.data.model.Safe
import apps.farm.ui.theme.*
import apps.farm.viewmodel.SafeViewModel
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeDetailScreen(
    safeId: String?,
    onNavigateBack: () -> Unit,
    safeViewModel: SafeViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isBlocked by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var showMessage by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val allSafes by safeViewModel.allSafes.collectAsState(initial = emptyList())

    LaunchedEffect(safeId) {
        if (safeId != null) {
            isEditing = true
            val safe = safeViewModel.getSafeById(safeId)
            if (safe != null) {
                name = safe.name
                balance = safe.balance.toString()
                note = safe.note
                isBlocked = safe.blocked
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (isEditing) stringResource(R.string.title_edit_safe) else stringResource(R.string.title_add_safe),
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
                    // Safe Name Field
                    UnifiedFormField(
                        value = name,
                        onValueChange = { 
                            name = it
                            nameError = false
                        },
                        label = stringResource(R.string.label_safe_name),
                        icon = Icons.Default.AccountBalance,
                        placeholder = stringResource(R.string.label_safe_name),
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                    
                    if (nameError) {
                        Text(
                            text = stringResource(R.string.error_safe_name_exists),
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
                        placeholder = stringResource(R.string.label_opening_balance),
                        isDecimal = true,
                        maxDecimals = 3,
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
                StatusCard(
                    isBlocked = isBlocked,
                    onToggle = { isBlocked = !isBlocked }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            val errorSafeNameRequired = stringResource(R.string.error_safe_name_required)

            // Save Button
            FilledTonalButton(
                onClick = {
                    if (name.isBlank()) {
                        showMessage = errorSafeNameRequired
                        return@FilledTonalButton
                    }

                    // Check if safe name is unique
                    val isSafeNameUnique = if (isEditing) {
                        allSafes.none { it.id != safeId && it.name.equals(name, ignoreCase = true) }
                    } else {
                        allSafes.none { it.name.equals(name, ignoreCase = true) }
                    }
                    
                    if (!isSafeNameUnique) {
                        nameError = true
                        return@FilledTonalButton
                    }

                    val balanceValue = balance.toDoubleOrNull() ?: 0.0

                    val safe = Safe(
                        id = safeId ?: UUID.randomUUID().toString(),
                        name = name,
                        balance = balanceValue,
                        note = note,
                        blocked = isBlocked
                    )

                    scope.launch {
                        if (isEditing) {
                            safeViewModel.updateSafe(safe) { success, message ->
                                showMessage = message
                                if (success) {
                                    onNavigateBack()
                                }
                            }
                        } else {
                            safeViewModel.insertSafe(safe) { success, message ->
                                showMessage = message
                                if (success) {
                                    onNavigateBack()
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
                    imageVector = if (isEditing) Icons.Default.EditNote else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isEditing) stringResource(R.string.button_save_changes) else stringResource(R.string.button_add_safe),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusCard(
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

            FilledIconToggleButton(
                checked = !isBlocked,
                onCheckedChange = { onToggle() },
                modifier = Modifier.size(44.dp),
                colors = IconButtonDefaults.filledIconToggleButtonColors(
                    checkedContainerColor = StatusActiveContainer,
                    checkedContentColor = StatusActive,
                    containerColor = StatusBlockedContainer,
                    contentColor = StatusBlocked
                )
            ) {
                Icon(
                    imageVector = if (isBlocked) Icons.Default.Block else Icons.Default.CheckCircle,
                    contentDescription = if (isBlocked) stringResource(R.string.status_blocked) else stringResource(R.string.status_active),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
