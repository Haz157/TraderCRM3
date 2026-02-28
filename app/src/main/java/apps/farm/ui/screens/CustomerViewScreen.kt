package apps.farm.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import apps.farm.ui.theme.BalanceNegative
import apps.farm.ui.theme.BalanceNeutral
import apps.farm.ui.theme.BalancePositive
import apps.farm.ui.theme.StatusActive
import apps.farm.ui.theme.StatusBlocked
import apps.farm.ui.theme.SurfaceVariant
import apps.farm.viewmodel.CustomerViewModel
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerViewScreen(
    customerId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToPdfViewer: (String) -> Unit,
    customerViewModel: CustomerViewModel = hiltViewModel()
) {
    var customerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var openingBalance by remember { mutableDoubleStateOf(0.0) }
    var currentBalance by remember { mutableDoubleStateOf(0.0) }
    var isBlocked by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Animation States
    var showHeader by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }
    var showBalance by remember { mutableStateOf(false) }

    val allCustomers by customerViewModel.allCustomers.collectAsState(initial = emptyList())

    LaunchedEffect(allCustomers) {
        allCustomers.find { it.customer.id == customerId }?.let { cwb ->
            customerName = cwb.customer.name
            phone = cwb.customer.phone
            address = cwb.customer.address
            note = cwb.customer.note
            openingBalance = cwb.customer.balance
            currentBalance = cwb.currentBalance
            isBlocked = cwb.customer.blocked
        }
        
        // Staggered animation trigger
        delay(100)
        showHeader = true
        delay(100)
        showInfo = true
        delay(150)
        showBalance = true
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
                        "تفاصيل العميل",
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
                    // Print Statement
                    IconButton(onClick = {
                        customerViewModel.generateCustomerStatement(context, customerId, null, null) { file ->
                            file?.let { onNavigateToPdfViewer(it.absolutePath) }
                        }
                    }) {
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                            Icon(Icons.Default.Description, contentDescription = "كشف حساب", modifier = Modifier.padding(8.dp), tint = MaterialTheme.colorScheme.secondary)
                        }
                    }
                    // Edit
                    IconButton(onClick = { onNavigateToEdit(customerId) }) {
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
                        text = if (isBlocked) "محظور" else "نشط",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = if (isBlocked) StatusBlocked else StatusActive,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Customer Info Card
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
                        ViewInfoRow(icon = Icons.Default.Person, label = "اسم العميل", value = customerName)
                        if (phone.isNotEmpty()) {
                            HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)
                            ViewInfoRow(icon = Icons.Default.Phone, label = "رقم الهاتف", value = phone)
                        }
                        if (address.isNotEmpty()) {
                            HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)
                            ViewInfoRow(icon = Icons.Default.LocationOn, label = "العنوان", value = address)
                        }
                        if (note.isNotEmpty()) {
                            HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)
                            ViewInfoRow(icon = Icons.Default.EditNote, label = "ملاحظات", value = note)
                        }
                    }
                }
            }

            // Balance Card
            AnimatedVisibility(
                visible = showBalance,
                enter = slideInVertically(initialOffsetY = { 30 }) + fadeIn()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ViewInfoRow(
                            icon = Icons.Default.AccountBalanceWallet,
                            label = "الرصيد الافتتاحي",
                            value = formatNum(openingBalance)
                        )
                        HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)
                        val balanceColor = when {
                            currentBalance > 0 -> BalancePositive
                            currentBalance < 0 -> BalanceNegative
                            else -> BalanceNeutral
                        }
                        ViewInfoRow(
                            icon = Icons.Default.MonetizationOn,
                            label = "الرصيد الحالي (المديونية)",
                            value = formatNum(currentBalance),
                            valueColor = balanceColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
