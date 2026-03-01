package apps.farm.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import apps.farm.R
import apps.farm.ui.theme.*

@Composable
fun UnifiedFormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    placeholder: String,
    minLines: Int = 1,
    isDecimal: Boolean = false,
    maxDecimals: Int = 3,
    imeAction: ImeAction = ImeAction.Default,
    onImeAction: () -> Unit = {},
    isPhone: Boolean = false,
    readOnly: Boolean = false
) {
    var hasError by remember { mutableStateOf(false) }
    
    // Validate input based on type
    val filteredValue = when {
        isPhone -> {
            // Only allow digits for phone numbers
            value.filter { char -> char.isDigit() }
        }
        isDecimal -> {
            if (value.isEmpty()) ""
            else {
                val decimalPattern = Regex("^-?\\d*\\.?(\\d{0,$maxDecimals})?$")
                if (value.matches(decimalPattern)) {
                    hasError = false
                    value
                } else {
                    hasError = true
                    // Remove invalid characters
                    val cleanPattern = Regex("[^\\d.-]")
                    val cleaned = value.replace(cleanPattern, "")
                    val parts = cleaned.split(".")
                    if (parts.size > 1) {
                        "${parts[0]}.${parts[1].take(maxDecimals)}"
                    } else {
                        cleaned
                    }
                }
            }
        }
        else -> value
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon container
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (hasError) StatusBlockedContainer else PrimaryContainer,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(10.dp)
                    .size(24.dp),
                tint = if (hasError) StatusBlocked else PrimaryDark
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Text field
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = if (hasError) StatusBlocked else TextSecondary
            )
            OutlinedTextField(
                value = filteredValue,
                onValueChange = { 
                    if (!readOnly) {
                        if (isDecimal) {
                            // Only allow valid decimal input
                            if (it.isEmpty() || it.matches(Regex("^-?\\d*\\.?(\\d{0,$maxDecimals})?$"))) {
                                onValueChange(it)
                            }
                        } else {
                            onValueChange(it)
                        }
                    }
                },
                readOnly = readOnly,
                placeholder = { Text(placeholder, color = TextDisabled) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onImeAction() },
                minLines = minLines,
                maxLines = if (minLines > 1) 5 else 1,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (hasError) StatusBlocked else PrimaryLight,
                    unfocusedBorderColor = if (hasError) StatusBlocked else SurfaceVariant,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                textStyle = MaterialTheme.typography.bodyLarge,
                isError = hasError,
                keyboardOptions = KeyboardOptions(
                    imeAction = imeAction,
                    keyboardType = when {
                        isPhone -> KeyboardType.Phone
                        isDecimal -> KeyboardType.Decimal
                        else -> KeyboardType.Text
                    }
                ),
                keyboardActions = KeyboardActions(
                    onAny = { onImeAction() }
                )
            )
            
            if (hasError && isDecimal) {
                Text(
                    text = stringResource(R.string.error_invalid_decimal, maxDecimals),
                    color = StatusBlocked,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            }
        }
    }
}
