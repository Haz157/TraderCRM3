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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import apps.farm.ui.theme.*
import apps.farm.viewmodel.ReceiveViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveViewScreen(
    receiveId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    receiveViewModel: ReceiveViewModel = hiltViewModel()
) {
    var customerName by remember { mutableStateOf("") }
    var safeName by remember { mutableStateOf("") }
    var receiveAmount by remember { mutableStateOf(0.0) }
    var discountAmount by remember { mutableStateOf(0.0) }
    var receiveNo by remember { mutableStateOf(0) }
    var note by remember { mutableStateOf("") }
    var createdDate by remember { mutableStateOf(0L) }
    var isLoaded by remember { mutableStateOf(false) }
    
    // Animation States
    var showMainInfo by remember { mutableStateOf(false) }
    var showFinance by remember { mutableStateOf(false) }
    var showNote by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val allReceives by receiveViewModel.allReceives.collectAsState(initial = emptyList())

    LaunchedEffect(allReceives) {
        val receive = allReceives.find { it.id == receiveId }
        if (receive != null) {
            receiveAmount = receive.receive
            discountAmount = receive.discount
            receiveNo = receive.receiveNo
            note = receive.note
            createdDate = receive.createdDate
            isLoaded = true

            // Resolve names
            receiveViewModel.getCustomerName(receive.customerId)?.let { customerName = it }
            receiveViewModel.getSafeName(receive.safeId)?.let { safeName = it }
            
            // Staggered animation trigger
            delay(100)
            showMainInfo = true
            delay(100)
            showFinance = true
            delay(150)
            showNote = true
        }
    }

    fun formatNum(value: Double): String {
        if (value == 0.0) return "0"
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
                        if (receiveNo > 0) "تحصيل رقم $receiveNo" else "تفاصيل التحصيل",
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
                    IconButton(onClick = { onNavigateToEdit(receiveId) }) {
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                            Icon(Icons.Default.Edit, contentDescription = "تعديل", modifier = Modifier.padding(8.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        if (!isLoaded) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Main Info Card
                AnimatedVisibility(
                    visible = showMainInfo,
                    enter = slideInVertically(initialOffsetY = { 20 }) + fadeIn()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            ViewInfoRow(icon = Icons.Default.Person, label = "العميل", value = customerName)
                            HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)
                            ViewInfoRow(icon = Icons.Default.AccountBalance, label = "الخزنة", value = safeName)
                            HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)
                            ViewInfoRow(icon = Icons.Default.CalendarToday, label = "التاريخ", value = if (createdDate > 0) dateFormat.format(Date(createdDate)) else "")
                        }
                    }
                }

                // Financial Card
                AnimatedVisibility(
                    visible = showFinance,
                    enter = slideInVertically(initialOffsetY = { 30 }) + fadeIn()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("المبالغ", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                            HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)
                            ViewInfoRow(icon = Icons.Default.Payments, label = "المبلغ المحصل", value = formatNum(receiveAmount), valueColor = BalancePositive)
                            if (discountAmount > 0) {
                                ViewInfoRow(icon = Icons.Default.RemoveCircle, label = "خصم", value = formatNum(discountAmount))
                            }
                        }
                    }
                }

                // Note Card
                if (note.isNotEmpty()) {
                    AnimatedVisibility(
                        visible = showNote,
                        enter = slideInVertically(initialOffsetY = { 40 }) + fadeIn()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                ViewInfoRow(icon = Icons.Default.EditNote, label = "ملاحظات", value = note)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
