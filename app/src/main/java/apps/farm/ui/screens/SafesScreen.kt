package apps.farm.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material.icons.filled.Edit
import apps.farm.R
import apps.farm.data.model.Safe
import apps.farm.ui.theme.*
import apps.farm.viewmodel.SafeViewModel

@Composable
fun SafesScreen(
    safeViewModel: SafeViewModel = hiltViewModel(),
    onNavigateToSafeView: (String) -> Unit,
    onNavigateToSafeDetail: (String?) -> Unit
) {
    val safes by safeViewModel.allSafes.collectAsState(initial = emptyList())
    val context = androidx.compose.ui.platform.LocalContext.current

    var safeToDelete by remember { mutableStateOf<Safe?>(null) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    if (safeToDelete != null) {
        AlertDialog(
            onDismissRequest = { safeToDelete = null },
            title = { Text(stringResource(R.string.dialog_delete_safe_title)) },
            text = { Text(stringResource(R.string.dialog_delete_safe_message, safeToDelete?.name ?: "")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        safeToDelete?.let { safe ->
                            safeViewModel.deleteSafe(safe) { success, message ->
                                // Optional message handling
                            }
                        }
                        safeToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.button_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { safeToDelete = null }) {
                    Text(stringResource(R.string.button_cancel))
                }
            }
        )
    }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text(stringResource(R.string.dialog_delete_all_safes_title)) },
            text = { Text(stringResource(R.string.dialog_delete_all_safes_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        safeViewModel.deleteAllSafes { success, message ->
                            // Optional message handling
                        }
                        showDeleteAllDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.button_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text(stringResource(R.string.button_cancel))
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (safes.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { showDeleteAllDialog = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.action_delete_all))
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.DeleteSweep, contentDescription = null)
                }
            }
        }

        if (safes.isEmpty()) {
            EmptyStateMessage(
                icon = Icons.Default.AccountBalance,
                message = stringResource(R.string.message_no_safes)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = safes,
                    key = { it.id }
                ) { safe ->
                    SafeCard(
                        safe = safe,
                        onClick = { onNavigateToSafeView(safe.id) },
                        onEdit = { onNavigateToSafeDetail(safe.id) },
                        onToggleBlock = { safeViewModel.toggleBlockStatus(safe.id, safe.blocked) },
                        onDelete = { safeToDelete = safe }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeCard(
    safe: Safe,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onToggleBlock: () -> Unit,
    onDelete: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .scale(animatedScale),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (safe.blocked) StatusBlockedContainer else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (safe.blocked) 2.dp else 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Status Indicator
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (safe.blocked) StatusBlocked else StatusActive,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (safe.blocked) Icons.Default.Block else Icons.Default.AccountBalance,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(24.dp),
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = safe.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = if (safe.blocked) StatusBlocked else TextPrimary
                        )
                        if (safe.note.isNotEmpty()) {
                            Text(
                                text = safe.note,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                    }
                    FilledIconToggleButton(
                        checked = !safe.blocked,
                        onCheckedChange = { onToggleBlock() },
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledIconToggleButtonColors(
                            checkedContainerColor = StatusActiveContainer,
                            checkedContentColor = StatusActive,
                            containerColor = StatusBlockedContainer,
                            contentColor = StatusBlocked
                        )
                    ) {
                        Icon(
                            imageVector = if (safe.blocked) Icons.Default.Block else Icons.Default.CheckCircle,
                            contentDescription = if (safe.blocked) stringResource(R.string.status_blocked) else stringResource(R.string.status_active),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Balance Display
            Spacer(modifier = Modifier.height(12.dp))

            val balanceColor = when {
                safe.balance < 0 -> BalanceNegative
                safe.balance > 0 -> BalancePositive
                else -> BalanceNeutral
            }

            // Balance Chip
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = balanceColor.copy(alpha = 0.1f),
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(
                    text = stringResource(R.string.label_balance, safe.balance),
                    color = balanceColor,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            if (safe.blocked) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = StatusBlocked.copy(alpha = 0.1f),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(
                        text = stringResource(R.string.status_blocked),
                        color = StatusBlocked,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
