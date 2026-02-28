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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import apps.farm.R
import apps.farm.data.model.Cycle
import apps.farm.ui.theme.*
import apps.farm.viewmodel.CycleViewModel
import apps.farm.viewmodel.FarmViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmViewScreen(
    farmId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToCycleDetail: (String?, String) -> Unit,
    onNavigateToPdfViewer: (String) -> Unit,
    farmViewModel: FarmViewModel = hiltViewModel(),
    cycleViewModel: CycleViewModel = hiltViewModel()
) {
    var farmName by remember { mutableStateOf("") }
    var farmNote by remember { mutableStateOf("") }
    var isBlocked by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // Animation States
    var showHeader by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }
    var showCycles by remember { mutableStateOf(false) }

    val cycles by cycleViewModel.getCyclesByFarmId(farmId).collectAsState(initial = emptyList())

    LaunchedEffect(farmId) {
        farmViewModel.getFarmById(farmId)?.let { farm ->
            farmName = farm.farmName
            farmNote = farm.farmNote
            isBlocked = farm.blocked
        }
        
        // Staggered animation trigger
        delay(100)
        showHeader = true
        delay(100)
        showInfo = true
        delay(150)
        showCycles = true
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "تفاصيل المزرعة",
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
                    IconButton(onClick = { onNavigateToEdit(farmId) }) {
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

            // Farm Info Card
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
                        ViewInfoRow(icon = Icons.Default.Agriculture, label = "اسم المزرعة", value = farmName)
                        if (farmNote.isNotEmpty()) {
                            HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)
                            ViewInfoRow(icon = Icons.Default.EditNote, label = "ملاحظات", value = farmNote)
                        }
                    }
                }
            }

            // Cycles Section
            AnimatedVisibility(
                visible = showCycles,
                enter = slideInVertically(initialOffsetY = { 30 }) + fadeIn()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (cycles.isNotEmpty()) {
                        Text(
                            "الدورات (${cycles.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimary
                        )

                        cycles.forEachIndexed { index, cycle ->
                            val itemVisible = remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                delay(index * 100L)
                                itemVisible.value = true
                            }
                            
                            AnimatedVisibility(
                                visible = itemVisible.value,
                                enter = expandVertically() + fadeIn()
                            ) {
                                CycleViewCard(
                                    cycle = cycle,
                                    onEdit = { onNavigateToCycleDetail(cycle.id, farmId) },
                                    onGenerateReport = {
                                        cycleViewModel.generateDetailedCycleReport(context, cycle.id) { file ->
                                            file?.let { onNavigateToPdfViewer(it.absolutePath) }
                                        }
                                    }
                                )
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Text(
                                "لا توجد دورات",
                                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CycleViewCard(
    cycle: Cycle,
    onEdit: () -> Unit,
    onGenerateReport: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (cycle.isActive) MaterialTheme.colorScheme.surface else StatusBlockedContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    cycle.cycleName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onGenerateReport, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Description, contentDescription = "تقرير", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Text(
                "${dateFormat.format(Date(cycle.sd))} - ${dateFormat.format(Date(cycle.ed))}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun ViewInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = TextPrimary
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.padding(8.dp).size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(value, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = valueColor)
        }
    }
}
