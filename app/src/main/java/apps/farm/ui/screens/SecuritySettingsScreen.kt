package apps.farm.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import apps.farm.viewmodel.AuthViewModel

import kotlinx.coroutines.launch
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val isEnabled by viewModel.isAuthEnabled.collectAsState(initial = false)
    val useBiometric by viewModel.useBiometric.collectAsState(initial = false)
    val isAutoBackupEnabled by viewModel.isAutoBackupEnabled.collectAsState(initial = false)
    val backupEmail by viewModel.backupEmail.collectAsState(initial = "htsolutionscodenest@gmail.com")
    
    var showPinDialog by remember { mutableStateOf(false) }
    var showEmailDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الإعدادات الأمنية") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SecurityItem(
                title = "تفعيل قفل التطبيق",
                subtitle = "تطالب برقم سري عند فتح التطبيق",
                icon = Icons.Default.Security,
                trailing = {
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { 
                            if (it) showPinDialog = true 
                            else viewModel.setAuthEnabled(false)
                        }
                    )
                }
            )

            if (isEnabled) {
                SecurityItem(
                    title = "استخدام البصمة",
                    subtitle = "تفعيل تسجيل الدخول ببصمة الإصبع",
                    icon = Icons.Default.Fingerprint,
                    trailing = {
                        Switch(
                            checked = useBiometric,
                            onCheckedChange = { viewModel.setUseBiometric(it) }
                        )
                    }
                )

                SecurityItem(
                    title = "تغيير الرقم السري",
                    subtitle = "تحديث الرقم السري المكون من 4 أرقام",
                    icon = Icons.Default.Password,
                    onClick = { showPinDialog = true }
                )
            }

            HorizontalDivider()

            SecurityItem(
                title = "نسخ احتياطي تلقائي",
                subtitle = "رفع البيانات تلقائياً عند توفر الإنترنت",
                icon = Icons.Default.CloudUpload,
                trailing = {
                    Switch(
                        checked = isAutoBackupEnabled,
                        onCheckedChange = { viewModel.setAutoBackupEnabled(it) }
                    )
                }
            )

            SecurityItem(
                title = "إيميل النسخ الاحتياطي",
                subtitle = backupEmail,
                icon = Icons.Default.Email,
                onClick = { showEmailDialog = true }
            )

            HorizontalDivider()


        }
    }

    if (showEmailDialog) {
        EmailUpdateDialog(
            currentEmail = backupEmail,
            onDismiss = { showEmailDialog = false },
            onConfirm = { email ->
                viewModel.setBackupEmail(email)
                showEmailDialog = false
            }
        )
    }

    if (showPinDialog) {
        PinSetupDialog(
            onDismiss = { showPinDialog = false },
            onConfirm = { pin ->
                viewModel.setPin(pin)
                viewModel.setAuthEnabled(true)
                showPinDialog = false
            }
        )
    }
}

@Composable
fun SecurityItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            trailing?.invoke()
        }
    }
}

@Composable
fun PinSetupDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    val isError = pin.length in 1..<4

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إعداد الرقم السري") },
        text = {
            Column {
                Text("أدخل رقم سري مكون من 4 أرقام")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) pin = it },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    isError = isError,
                    supportingText = { if (isError) Text("يجب إدخال 4 أرقام") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(pin) },
                enabled = pin.length == 4
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun EmailUpdateDialog(
    currentEmail: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var email by remember { mutableStateOf(currentEmail) }
    val isError = !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تحديث إيميل النسخ الاحتياطي") },
        text = {
            Column {
                Text("سيتم إرسال النسخة الاحتياطية إلى هذا الإيميل")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("البريد الإلكتروني") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = isError,
                    supportingText = { if (isError) Text("برجاء إدخال بريد إلكتروني صحيح") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(email) },
                enabled = !isError && email.isNotEmpty()
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
