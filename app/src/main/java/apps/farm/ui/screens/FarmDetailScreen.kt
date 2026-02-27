package apps.farm.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import apps.farm.R
import apps.farm.data.model.Farm
import apps.farm.ui.theme.*
import apps.farm.viewmodel.CycleViewModel
import apps.farm.viewmodel.FarmViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmDetailScreen(
    farmId: String?,
    onNavigateBack: () -> Unit,
    onNavigateToCycleDetail: (String?, String) -> Unit,
    farmViewModel: FarmViewModel = hiltViewModel(),
    cycleViewModel: CycleViewModel = hiltViewModel()
) {
    var farmName by remember { mutableStateOf("") }
    var farmNote by remember { mutableStateOf("") }
    var isBlocked by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var showMessage by remember { mutableStateOf<String?>(null) }
    var farmNameError by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val allFarms by farmViewModel.allFarms.collectAsState(initial = emptyList())

    LaunchedEffect(farmId) {
        if (farmId != null) {
            isEditing = true
            farmViewModel.getFarmById(farmId)?.let { farm ->
                farmName = farm.farmName
                farmNote = farm.farmNote
                isBlocked = farm.blocked
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (isEditing) stringResource(R.string.title_edit_farm) else stringResource(R.string.title_add_farm),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.content_description_back),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Main Unified Card with all fields
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Farm Name Field
                    UnifiedFormField(
                        value = farmName,
                        onValueChange = { 
                            farmName = it
                            farmNameError = false
                        },
                        label = stringResource(R.string.label_farm_name),
                        icon = Icons.Default.Agriculture,
                        placeholder = stringResource(R.string.label_farm_name),
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                    
                    if (farmNameError) {
                        Text(
                            text = stringResource(R.string.error_farm_name_exists),
                            color = StatusBlocked,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 56.dp, top = 2.dp)
                        )
                    }

                    // Divider
                    HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)

                    // Notes Field
                    UnifiedFormField(
                        value = farmNote,
                        onValueChange = { farmNote = it },
                        label = stringResource(R.string.label_farm_notes),
                        icon = Icons.Default.EditNote,
                        placeholder = stringResource(R.string.label_farm_notes),
                        minLines = 3,
                        imeAction = ImeAction.Done,
                        onImeAction = { focusManager.clearFocus() }
                    )
                }
            }

            if (isEditing) {
                // Status Card
                StatusCard(
                    isBlocked = isBlocked,
                    onToggle = { isBlocked = !isBlocked }
                )

                // Cycles Section
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = stringResource(R.string.section_cycles),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary
                )

                CyclesSection(
                    farmId = farmId!!,
                    cycleViewModel = cycleViewModel,
                    onAddCycle = { onNavigateToCycleDetail(null, farmId) },
                    onEditCycle = { cycleId -> onNavigateToCycleDetail(cycleId, farmId) }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            val errorFarmNameRequired = stringResource(R.string.error_farm_name_required)

            //  Save Button
            FilledTonalButton(
                onClick = {
                    if (farmName.isBlank()) {
                        showMessage = errorFarmNameRequired
                        return@FilledTonalButton
                    }

                    // Check if farm name is unique
                    val isFarmNameUnique = if (isEditing) {
                        allFarms.none { it.id != farmId && it.farmName.equals(farmName, ignoreCase = true) }
                    } else {
                        allFarms.none { it.farmName.equals(farmName, ignoreCase = true) }
                    }
                    
                    if (!isFarmNameUnique) {
                        farmNameError = true
                        return@FilledTonalButton
                    }

                    val farm = Farm(
                        id = farmId ?: UUID.randomUUID().toString(),
                        farmName = farmName,
                        farmNote = farmNote,
                        blocked = isBlocked
                    )

                    scope.launch {
                        if (isEditing) {
                            farmViewModel.updateFarm(farm)
                        } else {
                            farmViewModel.insertFarm(farm)
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = if (isEditing) Icons.Default.CalendarToday else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isEditing) stringResource(R.string.button_save_changes) else stringResource(R.string.button_add_farm),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    showMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { showMessage = null },
            confirmButton = {
                TextButton(onClick = { showMessage = null }) {
                    Text(stringResource(R.string.dialog_button_ok))
                }
            },
            title = { Text(stringResource(R.string.dialog_title_alert)) },
            text = { Text(message) }
        )
    }
}




@Composable
fun CyclesSection(
    farmId: String,
    cycleViewModel: CycleViewModel,
    onAddCycle: () -> Unit,
    onEditCycle: (String) -> Unit
) {
    val cycles by cycleViewModel.getCyclesByFarmId(farmId).collectAsState(initial = emptyList())
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Add Cycle Button
        FilledTonalButton(
            onClick = onAddCycle,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = PrimaryContainer,
                contentColor = PrimaryDark
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                stringResource(R.string.button_add_cycle),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }

        if (cycles.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SurfaceLight,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                Text(
                    text = stringResource(R.string.message_no_cycles),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            cycles.forEach { cycle ->
                val animatedScale by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "cycle_scale"
                )

                Card(
                    onClick = { onEditCycle(cycle.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(animatedScale),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (cycle.isActive)
                            MaterialTheme.colorScheme.surface
                        else
                            SurfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (cycle.isActive) 2.dp else 1.dp,
                        pressedElevation = 4.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cycle.cycleName,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = if (cycle.isActive) TextPrimary else TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${dateFormat.format(Date(cycle.sd))} - ${dateFormat.format(Date(cycle.ed))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        if (!cycle.isActive) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = StatusWarning.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = stringResource(R.string.status_inactive),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StatusWarning,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = StatusActive.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = stringResource(R.string.status_active),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StatusActive,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
