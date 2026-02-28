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
import apps.farm.ui.theme.icons.Farm
import apps.farm.viewmodel.SaleInvoiceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceViewScreen(
    invoiceId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    invoiceViewModel: SaleInvoiceViewModel = hiltViewModel()
) {
    var farmName by remember { mutableStateOf("") }
    var cycleName by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var safeName by remember { mutableStateOf("") }
    var invoiceNo by remember { mutableStateOf(0) }
    var invoiceDate by remember { mutableStateOf(0L) }
    var netWeight by remember { mutableStateOf(0.0) }
    var price by remember { mutableStateOf(0.0) }
    var totalPrice by remember { mutableStateOf(0.0) }
    var addition by remember { mutableStateOf(0.0) }
    var discount by remember { mutableStateOf(0.0) }
    var receiveAmount by remember { mutableStateOf(0.0) }
    var totalInvoice by remember { mutableStateOf(0.0) }
    var totalEmptyWeight by remember { mutableStateOf(0.0) }
    var totalGrossWeight by remember { mutableStateOf(0.0) }
    var isLoaded by remember { mutableStateOf(false) }
    
    // Animation States
    var showMainInfo by remember { mutableStateOf(false) }
    var showWeights by remember { mutableStateOf(false) }
    var showFinance by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    LaunchedEffect(invoiceId) {
        invoiceViewModel.loadInvoice(invoiceId)
    }

    val selectedFarm by invoiceViewModel.selectedFarm.collectAsState()
    val selectedCycle by invoiceViewModel.selectedCycle.collectAsState()
    val selectedCustomer by invoiceViewModel.selectedCustomer.collectAsState()
    val selectedSafe by invoiceViewModel.selectedSafe.collectAsState()
    val invDate by invoiceViewModel.invoiceDate.collectAsState()
    val invPrice by invoiceViewModel.price.collectAsState()
    val invReceive by invoiceViewModel.receiveAmount.collectAsState()
    val invDiscount by invoiceViewModel.discountAmount.collectAsState()
    val invAddition by invoiceViewModel.additionAmount.collectAsState()

    // Find the invoice in the list
    val allInvoices by invoiceViewModel.allInvoices.collectAsState()
    val invoice = allInvoices.find { it.id == invoiceId }

    LaunchedEffect(invoice, selectedFarm, selectedCycle, selectedCustomer, selectedSafe) {
        if (invoice != null) {
            farmName = selectedFarm?.farmName ?: ""
            cycleName = selectedCycle?.cycleName ?: ""
            customerName = selectedCustomer?.name ?: ""
            safeName = selectedSafe?.name ?: ""
            invoiceNo = invoice.invoiceNo
            invoiceDate = invoice.invoiceDate
            netWeight = invoice.netWeight
            price = invoice.price
            totalPrice = invoice.totalPrice
            addition = invoice.addition
            discount = invoice.discount
            receiveAmount = invoice.receiveAmount
            totalInvoice = invoice.totalInvoice
            totalEmptyWeight = invoice.totalEmptyWeight
            totalEmptyWeight = invoice.totalEmptyWeight
            totalGrossWeight = invoice.totalGrossWeight
            isLoaded = true
            
            // Staggered animation trigger
            delay(100)
            showMainInfo = true
            delay(100)
            showWeights = true
            delay(150)
            showFinance = true
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
                        if (invoiceNo > 0) "فاتورة بيع رقم $invoiceNo" else "تفاصيل الفاتورة",
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
                    IconButton(onClick = { onNavigateToEdit(invoiceId) }) {
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
                            ViewInfoRow(icon = Icons.Farm, label = "المزرعة", value = farmName)
                            HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)
                            ViewInfoRow(icon = Icons.Default.Loop, label = "الدورة", value = cycleName)
                            HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)
                            ViewInfoRow(icon = Icons.Default.Person, label = "التاجر", value = customerName)
                            HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)
                            ViewInfoRow(icon = Icons.Default.AccountBalance, label = "الخزنة", value = safeName)
                            HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)
                            ViewInfoRow(icon = Icons.Default.CalendarToday, label = "التاريخ", value = if (invoiceDate > 0) dateFormat.format(Date(invoiceDate)) else "")
                        }
                    }
                }

                // Weights Card
                AnimatedVisibility(
                    visible = showWeights,
                    enter = slideInVertically(initialOffsetY = { 30 }) + fadeIn()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("الأوزان", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                            HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)
                            ViewInfoRow(icon = Icons.Default.FitnessCenter, label = "إجمالي وزن الفارغ", value = formatNum(totalEmptyWeight))
                            ViewInfoRow(icon = Icons.Default.FitnessCenter, label = "إجمالي الوزن القائم", value = formatNum(totalGrossWeight))
                            ViewInfoRow(icon = Icons.Default.Scale, label = "صافي الوزن", value = formatNum(netWeight))
                        }
                    }
                }

                // Financial Card
                AnimatedVisibility(
                    visible = showFinance,
                    enter = slideInVertically(initialOffsetY = { 40 }) + fadeIn()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("الحساب", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                            HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)
                            ViewInfoRow(icon = Icons.Default.PriceCheck, label = "السعر", value = formatNum(price))
                            ViewInfoRow(icon = Icons.Default.MonetizationOn, label = "إجمالي السعر", value = formatNum(totalPrice))
                            if (addition > 0) {
                                ViewInfoRow(icon = Icons.Default.AddCircle, label = "إضافة", value = formatNum(addition))
                            }
                            if (discount > 0) {
                                ViewInfoRow(icon = Icons.Default.RemoveCircle, label = "خصم", value = formatNum(discount))
                            }
                            HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)
                            ViewInfoRow(icon = Icons.Default.Receipt, label = "إجمالي الفاتورة", value = formatNum(totalInvoice), valueColor = MaterialTheme.colorScheme.primary)
                            ViewInfoRow(icon = Icons.Default.Payments, label = "المبلغ المحصل", value = formatNum(receiveAmount), valueColor = BalancePositive)
                            val remaining = totalInvoice - receiveAmount
                            if (remaining > 0) {
                                ViewInfoRow(icon = Icons.Default.Warning, label = "المتبقي", value = formatNum(remaining), valueColor = BalanceNegative)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
