package apps.farm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apps.farm.ui.theme.PrimaryDark
import apps.farm.ui.theme.StatusBlocked


@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrialActivationScreen(
    onActivate: (String) -> Boolean = { false }
) {
    var key by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = StatusBlocked.copy(alpha = 0.1f),
            modifier = Modifier.size(100.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.padding(24.dp).size(48.dp),
                tint = StatusBlocked
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "انتهت الفترة التجريبية",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = StatusBlocked
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "لقد انتهت فترة الـ 3 أيام التجريبية المسموح بها. يرجى التواصل مع الإدارة للحصول على مفتاح التفعيل للاستمرار في استخدام التطبيق.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = key,
            onValueChange = { 
                key = it
                showError = false
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("مفتاح التفعيل") },
            placeholder = { Text("أدخل المفتاح هنا...") },
            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
            shape = RoundedCornerShape(16.dp),
            isError = showError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )

        if (showError) {
            Text(
                text = "مفتاح التفعيل غير صحيح",
                color = StatusBlocked,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (!onActivate(key)) {
                    showError = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryDark
            )
        ) {
            Text(
                text = "تفعيل التطبيق",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Contact Section
        val context = androidx.compose.ui.platform.LocalContext.current
        
        Text(
            text = "للتواصل مع الدعم الفني:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // WhatsApp Link
            IconButton(
                onClick = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://wa.me/201157235269"))
                    context.startActivity(intent)
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Whatsapp, // Using Phone as a fallback for WhatsApp
                    contentDescription = "WhatsApp",
                    tint = Color(0xFF25D366),
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Facebook Link
            IconButton(
                onClick = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://www.facebook.com/profile.php?id=61585149645925&mibextid=ZbWKwL"))
                    context.startActivity(intent)
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Facebook, // Using Public as a fallback for Facebook/Web
                    contentDescription = "Facebook",
                    tint = Color(0xFF1877F2),
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Branding
        Text(
            text = "Powered by HT Solutions",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}
