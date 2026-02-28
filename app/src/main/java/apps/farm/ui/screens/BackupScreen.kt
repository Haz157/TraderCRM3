package apps.farm.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import apps.farm.R
import apps.farm.ui.theme.*
import apps.farm.utils.DatabaseBackupUtils
import apps.farm.viewmodel.BackupCategory
import apps.farm.viewmodel.BackupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onNavigateBack: () -> Unit,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val selectedCategories by viewModel.selectedCategories.collectAsState()
    val isAllSelected by viewModel.isAllSelected.collectAsState()
    val isExporting by viewModel.isExporting.collectAsState()
    val context = LocalContext.current
    var showMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.title_backup),
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
            // Select All Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.label_select_all),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Switch(
                    checked = isAllSelected,
                    onCheckedChange = { viewModel.toggleSelectAll() }
                )
            }

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            // Categories
            BackupCategoryItem(
                title = stringResource(R.string.label_farms_data),
                icon = Icons.Default.Agriculture,
                selected = selectedCategories.contains(BackupCategory.FARMS),
                onClick = { viewModel.toggleCategory(BackupCategory.FARMS) }
            )

            BackupCategoryItem(
                title = stringResource(R.string.label_customers_data),
                icon = Icons.Default.Person,
                selected = selectedCategories.contains(BackupCategory.CUSTOMERS),
                onClick = { viewModel.toggleCategory(BackupCategory.CUSTOMERS) }
            )

            BackupCategoryItem(
                title = stringResource(R.string.label_safes_data),
                icon = Icons.Default.AccountBalance,
                selected = selectedCategories.contains(BackupCategory.SAFES),
                onClick = { viewModel.toggleCategory(BackupCategory.SAFES) }
            )

            BackupCategoryItem(
                title = stringResource(R.string.label_invoices_data),
                icon = Icons.Default.Receipt,
                selected = selectedCategories.contains(BackupCategory.INVOICES),
                onClick = { viewModel.toggleCategory(BackupCategory.INVOICES) }
            )

            BackupCategoryItem(
                title = stringResource(R.string.label_receives_data),
                icon = Icons.Default.AccountBalanceWallet,
                selected = selectedCategories.contains(BackupCategory.RECEIVES),
                onClick = { viewModel.toggleCategory(BackupCategory.RECEIVES) }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Start Backup Button
            Button(
                onClick = {
                    viewModel.startBackup(
                        onFullBackup = {
                            DatabaseBackupUtils.backupDatabase(context)
                        },
                        onSelectiveExport = { content ->
                            DatabaseBackupUtils.shareSelectiveExport(context, content)
                        },
                        onError = { error ->
                            showMessage = if (error == "error_no_category_selected") {
                                context.getString(R.string.error_no_category_selected)
                            } else {
                                error
                            }
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isExporting
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.button_start_backup))
                }
            }

            // Restore Database Button
            var showRestoreConfirmDialog by remember { mutableStateOf(false) }
            val restoreLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                androidx.activity.result.contract.ActivityResultContracts.GetContent()
            ) { uri ->
                uri?.let {
                    DatabaseBackupUtils.restoreDatabase(
                        context = context,
                        backupUri = it,
                        onSuccess = {
                            DatabaseBackupUtils.restartApp(context)
                        },
                        onError = { error ->
                            showMessage = context.getString(R.string.message_restore_error, error)
                        }
                    )
                }
            }

            OutlinedButton(
                onClick = { showRestoreConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.SettingsBackupRestore, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.button_restore_db))
            }

            if (showRestoreConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showRestoreConfirmDialog = false },
                    title = { Text(stringResource(R.string.dialog_title_confirm_restore)) },
                    text = { Text(stringResource(R.string.dialog_message_confirm_restore)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showRestoreConfirmDialog = false
                                restoreLauncher.launch("*/*") // Filter for all files, user picks the .db
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(stringResource(R.string.dialog_button_ok))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRestoreConfirmDialog = false }) {
                            Text(stringResource(R.string.dialog_button_cancel))
                        }
                    }
                )
            }
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
fun BackupCategoryItem(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) PrimaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = if (selected) PrimaryDark else TextSecondary)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selected) TextPrimary else TextSecondary
                )
            }
            Checkbox(
                checked = selected,
                onCheckedChange = { onClick() }
            )
        }
    }
}
