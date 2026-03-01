package apps.farm.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import apps.farm.R
import apps.farm.data.model.*
import apps.farm.ui.theme.*
import androidx.compose.ui.res.stringResource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun EmptyWeightDialog(
    title: String,
    onDismiss: () -> Unit,
    onAdd: (weight: Double, count: Int) -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var count by remember { mutableStateOf("") }
    var averageWeight by remember { mutableStateOf("") }

    // Sync weight when averageWeight or count changes
    LaunchedEffect(averageWeight, count) {
        val avg = averageWeight.toDoubleOrNull() ?: 0.0
        val cnt = count.toIntOrNull() ?: 0
        if (avg > 0 && cnt > 0) {
            val calculatedWeight = avg * cnt
            val currentWeight = weight.toDoubleOrNull() ?: 0.0
            // Only update if the difference is significant to avoid infinite loops or jitter
            if (abs(calculatedWeight - currentWeight) > 0.001) {
                weight = String.format(Locale.US, "%.3f", calculatedWeight)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp), // More rounded as in image
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // 1. Average Crate Weight
                UnifiedFormField(
                    value = averageWeight,
                    onValueChange = { 
                        averageWeight = it
                    },
                    label = stringResource(R.string.label_average_crate_weight),
                    icon = Icons.Default.Scale,
                    placeholder = "0.0",
                    isDecimal = true,
                    maxDecimals = 3
                )

                // 2. Crate Count
                UnifiedFormField(
                    value = count,
                    onValueChange = { 
                        count = it.filter { char -> char.isDigit() }
                    },
                    label = stringResource(R.string.label_crate_count),
                    icon = Icons.Default.Inventory2,
                    placeholder = "0",
                    isPhone = true
                )

                // 3. Total Weight (Calculated/Manual)
                UnifiedFormField(
                    value = weight,
                    onValueChange = { 
                        weight = it
                        // Update average if total weight is changed manually
                        val w = it.toDoubleOrNull() ?: 0.0
                        val c = count.toIntOrNull() ?: 0
                        if (c > 0) {
                            val calculatedAvg = w / c
                            val currentAvg = averageWeight.toDoubleOrNull() ?: 0.0
                            if (Math.abs(calculatedAvg - currentAvg) > 0.001) {
                                averageWeight = String.format(Locale.US, "%.3f", calculatedAvg)
                            }
                        }
                    },
                    label = stringResource(R.string.label_weight_kg),
                    icon = Icons.Default.Calculate,
                    placeholder = "0.0",
                    isDecimal = true,
                    maxDecimals = 3
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp,
                            brush = androidx.compose.ui.graphics.SolidColor(TextSecondary.copy(alpha = 0.5f))
                        )
                    ) {
                        Text(
                            stringResource(R.string.button_cancel),
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )
                    }
                    
                    Button(
                        onClick = {
                            val weightValue = weight.toDoubleOrNull() ?: 0.0
                            val countValue = count.toIntOrNull() ?: 0
                            if (weightValue > 0 && countValue > 0) {
                                onAdd(weightValue, countValue)
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryLight,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            stringResource(R.string.button_add),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GrossWeightDialog(
    title: String,
    onDismiss: () -> Unit,
    onAdd: (weight: Double, count: Int) -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var count by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                UnifiedFormField(
                    value = count,
                    onValueChange = { count = it.filter { char -> char.isDigit() } },
                    label = stringResource(R.string.label_crate_count),
                    icon = Icons.Default.Inventory2,
                    placeholder = "0",
                    isPhone = true
                )

                UnifiedFormField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = stringResource(R.string.label_weight_kg),
                    icon = Icons.Default.Calculate,
                    placeholder = "0.0",
                    isDecimal = true,
                    maxDecimals = 3
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp,
                            brush = androidx.compose.ui.graphics.SolidColor(TextSecondary.copy(alpha = 0.5f))
                        )
                    ) {
                        Text(
                            stringResource(R.string.button_cancel),
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )
                    }
                    
                    Button(
                        onClick = {
                            val weightValue = weight.toDoubleOrNull() ?: 0.0
                            val countValue = count.toIntOrNull() ?: 0
                            if (weightValue > 0 && countValue > 0) {
                                onAdd(weightValue, countValue)
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryLight,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            stringResource(R.string.button_add),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FarmSelectionDialog(
    farms: List<Farm>,
    onDismiss: () -> Unit,
    onSelect: (Farm) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.placeholder_select_farm),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary
                )
                
                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(farms) { farm ->
                        Card(
                            onClick = {
                                onSelect(farm)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = SurfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Agriculture,
                                    contentDescription = null,
                                    tint = PrimaryDark
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = farm.farmName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
                
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.button_cancel))
                }
            }
        }
    }
}

@Composable
fun CustomerSelectionDialog(
    customers: List<CustomerWithBalance>,
    onDismiss: () -> Unit,
    onSelect: (Customer) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.placeholder_select_customer),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary
                )
                
                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(customers) { customerWithBalance ->
                        val customer = customerWithBalance.customer
                        Card(
                            onClick = {
                                onSelect(customer)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = SurfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = PrimaryDark
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = customer.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary
                                    )
                                    if (customer.phone.isNotEmpty()) {
                                        Text(
                                            text = customer.phone,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.button_cancel))
                }
            }
        }
    }
}

@Composable
fun SafeSelectionDialog(
    safes: List<Safe>,
    onDismiss: () -> Unit,
    onSelect: (Safe) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.placeholder_select_safe),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary
                )
                
                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(safes) { safe ->
                        Card(
                            onClick = {
                                onSelect(safe)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = SurfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = PrimaryDark
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = safe.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = stringResource(R.string.label_balance, safe.balance.toString()),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
                
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.button_cancel))
                }
            }
        }
    }
}

@Composable
fun CycleSelectionDialog(
    cycles: List<Cycle>,
    onDismiss: () -> Unit,
    onSelect: (Cycle) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.placeholder_select_cycle),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary
                )
                
                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cycles) { cycle ->
                        Card(
                            onClick = {
                                onSelect(cycle)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = SurfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = PrimaryDark
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = cycle.cycleName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary
                                    )
                                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                    Text(
                                        text = "${dateFormat.format(Date(cycle.sd))} - ${dateFormat.format(Date(cycle.ed))}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
                
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.button_cancel))
                }
            }
        }
    }
}

@Composable
fun DateRangePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Long?, Long?) -> Unit
) {
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "فترة التقرير",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary
                )
                
                // Start Date Selection
                Card(
                    onClick = {
                        val calendar = java.util.Calendar.getInstance()
                        android.app.DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val cal = java.util.Calendar.getInstance()
                                cal.set(year, month, dayOfMonth, 0, 0, 0)
                                cal.set(java.util.Calendar.MILLISECOND, 0)
                                startDate = cal.timeInMillis
                            },
                            calendar.get(java.util.Calendar.YEAR),
                            calendar.get(java.util.Calendar.MONTH),
                            calendar.get(java.util.Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = PrimaryDark)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("تاريخ البداية", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(
                                text = startDate?.let { sdf.format(Date(it)) } ?: "اختر التاريخ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (startDate != null) TextPrimary else TextDisabled
                            )
                        }
                    }
                }

                // End Date Selection
                Card(
                    onClick = {
                        val calendar = java.util.Calendar.getInstance()
                        android.app.DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val cal = java.util.Calendar.getInstance()
                                cal.set(year, month, dayOfMonth, 23, 59, 59)
                                cal.set(java.util.Calendar.MILLISECOND, 999)
                                endDate = cal.timeInMillis
                            },
                            calendar.get(java.util.Calendar.YEAR),
                            calendar.get(java.util.Calendar.MONTH),
                            calendar.get(java.util.Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = PrimaryDark)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("تاريخ النهاية", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(
                                text = endDate?.let { sdf.format(Date(it)) } ?: "اختر التاريخ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (endDate != null) TextPrimary else TextDisabled
                            )
                        }
                    }
                }

                Text(
                    text = "اترك التواريخ فارغة للحصول على تقرير كامل",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.button_cancel))
                    }
                    
                    FilledTonalButton(
                        onClick = { onConfirm(startDate, endDate) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("تم")
                    }
                }
            }
        }
    }
}


