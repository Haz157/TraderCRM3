package apps.farm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import apps.farm.utils.BiometricHelper

@Composable
fun AuthScreen(
    onPinEntered: (String) -> Unit,
    onBiometricSuccess: () -> Unit,
    useBiometric: Boolean,
    error: String? = null
) {
    var pinInput by remember { mutableStateOf("") }
    val maxPinLength = 4
    val context = LocalContext.current

    // Trigger biometric automatically if enabled and available
    LaunchedEffect(useBiometric) {
        if (useBiometric && BiometricHelper.isBiometricAvailable(context)) {
            BiometricHelper.showBiometricPrompt(
                activity = context as FragmentActivity,
                title = "الدخول للتطبيق",
                subtitle = "استخدم بصمة الإصبع للدخول",
                onSuccess = { onBiometricSuccess() },
                onError = { /* Handle error locally if needed */ }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "برجاء إدخال الرقم السري",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // PIN Indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(maxPinLength) { index ->
                val isFilled = index < pinInput.length
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (isFilled) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
        // Numeric Keypad
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("bio", "0", "del")
        )

        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    KeyButton(
                        key = key,
                        isBiometricEnabled = useBiometric,
                        onClick = {
                            when (key) {
                                "del" -> if (pinInput.isNotEmpty()) pinInput = pinInput.dropLast(1)
                                "bio" -> {
                                    if (useBiometric) {
                                        BiometricHelper.showBiometricPrompt(
                                            activity = context as FragmentActivity,
                                            title = "الدخول للتطبيق",
                                            onSuccess = { onBiometricSuccess() },
                                            onError = { /* Handle error */ }
                                        )
                                    }
                                }
                                else -> {
                                    if (pinInput.length < maxPinLength) {
                                        pinInput += key
                                        if (pinInput.length == maxPinLength) {
                                            onPinEntered(pinInput)
                                            pinInput = "" // Reset for next attempt if it fails
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun KeyButton(
    key: String,
    isBiometricEnabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .then(
                if (key == "bio" && !isBiometricEnabled) Modifier 
                else Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (key == "bio" && !isBiometricEnabled) return@Box

        IconButton(
            onClick = onClick,
            modifier = Modifier.fillMaxSize()
        ) {
            when (key) {
                "del" -> Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Delete")
                "bio" -> Icon(Icons.Default.Fingerprint, contentDescription = "Biometric", tint = MaterialTheme.colorScheme.primary)
                else -> Text(
                    text = key,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
