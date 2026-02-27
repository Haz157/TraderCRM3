package apps.farm.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Receipt
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
import apps.farm.data.model.SaleInvoice
import apps.farm.ui.theme.*
import apps.farm.viewmodel.SaleInvoiceViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InvoicesScreen(
    invoiceViewModel: SaleInvoiceViewModel = hiltViewModel(),
    onNavigateToInvoiceDetail: (String?) -> Unit
) {
    val invoices by invoiceViewModel.allInvoices.collectAsState(initial = emptyList())

    if (invoices.isEmpty()) {
        EmptyStateMessage(
            icon = Icons.Default.Receipt,
            message = stringResource(R.string.message_no_invoices)
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = invoices,
                key = { it.id }
            ) { invoice ->
                InvoiceCard(
                    invoice = invoice,
                    onEdit = { onNavigateToInvoiceDetail(invoice.id) },
                    onToggleBlock = { invoiceViewModel.toggleBlockStatus(invoice.id, invoice.isBlocked) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceCard(
    invoice: SaleInvoice,
    onEdit: () -> Unit,
    onToggleBlock: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Card(
        onClick = onEdit,
        modifier = Modifier
            .fillMaxWidth()
            .scale(animatedScale),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (invoice.isBlocked) StatusBlockedContainer else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (invoice.isBlocked) 2.dp else 4.dp,
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
                        color = if (invoice.isBlocked) StatusBlocked else StatusActive,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (invoice.isBlocked) Icons.Default.Block else Icons.Default.Receipt,
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
                            text = stringResource(R.string.label_invoice_number, invoice.id.take(8)),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = if (invoice.isBlocked) StatusBlocked else TextPrimary
                        )
                        Text(
                            text = dateFormat.format(Date(invoice.invoiceDate)),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                // Toggle Button
                FilledIconToggleButton(
                    checked = !invoice.isBlocked,
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
                        imageVector = if (invoice.isBlocked) Icons.Default.Block else Icons.Default.CheckCircle,
                        contentDescription = if (invoice.isBlocked) stringResource(R.string.status_blocked) else stringResource(R.string.status_active),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Invoice Details
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Net Weight
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.label_net_weight),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = stringResource(R.string.format_weight_kg, invoice.netWeight.toString()),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.label_price),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = invoice.price.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Total Invoice
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.label_total_invoice),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = invoice.totalInvoice.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (invoice.isBlocked) {
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
