package apps.farm.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import apps.farm.ui.theme.*
import apps.farm.viewmodel.SafeViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeViewScreen(
    safeId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    safeViewModel: SafeViewModel = hiltViewModel()
) {
    var safeName by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf(0.0) }
    var note by remember { mutableStateOf("") }
    var isBlocked by remember { mutableStateOf(false) }
    
    // Animation States
    var showHeader by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }

    LaunchedEffect(safeId) {
        safeViewModel.getSafeById(safeId)?.let { safe ->
            safeName = safe.name
            balance = safe.balance
            note = safe.note
            isBlocked = safe.blocked
        }
        
        // Staggered animation trigger
        delay(100)
        showHeader = true
        delay(150)
        showInfo = true
    }

    fun formatNum(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            String.format(Locale.ENGLISH, "%,.0f", value)
        } else {
            String.format(Locale.ENGLISH, "%,.2f", value)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "تفاصيل الخزنة",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", modifier = Modifier.padding(8.dp))
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(safeId) }) {
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                            Icon(Icons.Default.Edit, contentDescription = "تعديل", modifier = Modifier.padding(8.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Status Badge
            AnimatedVisibility(
                visible = showHeader,
                enter = slideInVertically(initialOffsetY = { -20 }) + fadeIn()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isBlocked) StatusBlocked.copy(alpha = 0.1f) else StatusActive.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = if (isBlocked) "محظورة" else "نشطة",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = if (isBlocked) StatusBlocked else StatusActive,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Safe Info Card
            AnimatedVisibility(
                visible = showInfo,
                enter = slideInVertically(initialOffsetY = { 20 }) + fadeIn()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        ViewInfoRow(icon = Icons.Default.AccountBalance, label = "اسم الخزنة", value = safeName)
                        HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)
                        ViewInfoRow(
                            icon = Icons.Default.MonetizationOn,
                            label = "الرصيد",
                            value = formatNum(balance),
                            valueColor = if (balance >= 0) BalancePositive else BalanceNegative
                        )
                        if (note.isNotEmpty()) {
                            HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)
                            ViewInfoRow(icon = Icons.Default.EditNote, label = "ملاحظات", value = note)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
