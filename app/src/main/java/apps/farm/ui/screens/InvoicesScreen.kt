package apps.farm.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
    onNavigateToInvoiceView: (String) -> Unit,
    onNavigateToInvoiceDetail: (String?) -> Unit
) {
    val invoices by invoiceViewModel.allInvoices.collectAsState(initial = emptyList())
    var invoiceToDelete by remember { mutableStateOf<SaleInvoice?>(null) }
    var showMessage by remember { mutableStateOf<String?>(null) }

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
                    onClick = { onNavigateToInvoiceView(invoice.id) },
                    onEdit = { onNavigateToInvoiceDetail(invoice.id) },
                    onDelete = { invoiceToDelete = invoice }
                )
            }
        }
    }

    invoiceToDelete?.let { invoice ->
        AlertDialog(
            onDismissRequest = { invoiceToDelete = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        invoiceViewModel.deleteInvoice(invoice) { success, message ->
                            if (success) {
                                invoiceToDelete = null
                            } else {
                                showMessage = message
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = StatusBlocked)
                ) {
                    Text(stringResource(R.string.button_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { invoiceToDelete = null }) {
                    Text(stringResource(R.string.button_cancel))
                }
            },
            title = { Text(stringResource(R.string.dialog_title_confirm_delete)) },
            text = { Text("هل أنت متأكد من حذف هذه الفاتورة؟ سيتم عكس تأثير مبالغها من أرصدة العميل والخزنة.") }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceCard(
    invoice: SaleInvoice,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMedium,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "scale"
    )

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .scale(animatedScale),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
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
                    // Constant Icon
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
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
                            text = stringResource(
                                R.string.label_invoice_number,
                                invoice.invoiceNo.toString()
                            ),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = TextPrimary
                        )
                        Text(
                            text = dateFormat.format(Date(invoice.invoiceDate)),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                // Actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Edit Button
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "تعديل",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    // Delete Button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = StatusBlocked
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.content_description_delete),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Invoice Details
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                fun formatDecimal(value: Double): String {
                    return if (value == value.toLong().toDouble()) {
                        String.format(Locale.ENGLISH, "%.0f", value)
                    } else {
                        String.format(Locale.ENGLISH, "%,.2f", value)
                    }
                }

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
                        text = stringResource(
                            R.string.format_weight_kg,
                            formatDecimal(invoice.netWeight)
                        ),
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
                        text = formatDecimal(invoice.price),
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
                        text = formatDecimal(invoice.totalInvoice),
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

        }
    }
}
