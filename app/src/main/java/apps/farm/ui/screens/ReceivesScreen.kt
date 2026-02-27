package apps.farm.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
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
import apps.farm.R
import apps.farm.data.model.Receive
import apps.farm.ui.theme.*
import apps.farm.viewmodel.ReceiveViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReceivesScreen(
    receiveViewModel: ReceiveViewModel = hiltViewModel(),
    onNavigateToReceiveDetail: (String?) -> Unit
) {
    val receives by receiveViewModel.allReceives.collectAsState(initial = emptyList())

    if (receives.isEmpty()) {
        EmptyStateMessage(
            icon = Icons.Default.AccountBalanceWallet,
            message = stringResource(R.string.message_no_receives)
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = receives,
                key = { it.id }
            ) { receive ->
                ReceiveCard(
                    receive = receive,
                    onEdit = { onNavigateToReceiveDetail(receive.id) },
                    onToggleBlock = { receiveViewModel.toggleBlockStatus(receive.id, receive.isBlocked) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveCard(
    receive: Receive,
    onEdit: () -> Unit,
    onToggleBlock: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Card(
        onClick = onEdit,
        modifier = Modifier
            .fillMaxWidth()
            .scale(animatedScale),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (receive.isBlocked) StatusBlockedContainer else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (receive.isBlocked) 2.dp else 4.dp,
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
                        color = if (receive.isBlocked) StatusBlocked else StatusActive,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (receive.isBlocked) Icons.Default.Block else Icons.Default.AccountBalanceWallet,
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
                            text = stringResource(R.string.label_receive_amount_with_value, receive.receive.toString()),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = if (receive.isBlocked) StatusBlocked else TextPrimary
                        )
                        Text(
                            text = dateFormat.format(Date(receive.createdDate)),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }

                // Toggle Button
                FilledIconToggleButton(
                    checked = !receive.isBlocked,
                    onCheckedChange = { onToggleBlock() },
                    modifier = Modifier.size(44.dp),
                    colors = IconButtonDefaults.filledIconToggleButtonColors(
                        checkedContainerColor = StatusActiveContainer,
                        checkedContentColor = StatusActive,
                        containerColor = StatusBlockedContainer,
                        contentColor = StatusBlocked
                    )
                ) {
                    Icon(
                        imageVector = if (receive.isBlocked) Icons.Default.Block else Icons.Default.CheckCircle,
                        contentDescription = if (receive.isBlocked) stringResource(R.string.status_blocked) else stringResource(R.string.status_active),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (receive.note.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = receive.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = PrimaryDark,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            if (receive.isBlocked) {
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
