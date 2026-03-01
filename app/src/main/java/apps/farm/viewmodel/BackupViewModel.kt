package apps.farm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import apps.farm.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class BackupCategory {
    FARMS, CUSTOMERS, SAFES, INVOICES, RECEIVES
}

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val farmRepository: FarmRepository,
    private val customerRepository: CustomerRepository,
    private val safeRepository: SafeRepository,
    private val invoiceRepository: SaleInvoiceRepository,
    private val receiveRepository: ReceiveRepository,
    private val cycleRepository: CycleRepository
) : ViewModel() {

    private val _selectedCategories = MutableStateFlow<Set<BackupCategory>>(BackupCategory.values().toSet())
    val selectedCategories: StateFlow<Set<BackupCategory>> = _selectedCategories.asStateFlow()

    private val _isAllSelected = MutableStateFlow(true)
    val isAllSelected: StateFlow<Boolean> = _isAllSelected.asStateFlow()

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    fun toggleCategory(category: BackupCategory) {
        val current = _selectedCategories.value.toMutableSet()
        if (current.contains(category)) {
            current.remove(category)
        } else {
            current.add(category)
        }
        _selectedCategories.value = current
        _isAllSelected.value = current.size == BackupCategory.values().size
    }

    fun toggleSelectAll() {
        if (_isAllSelected.value) {
            _selectedCategories.value = emptySet()
            _isAllSelected.value = false
        } else {
            _selectedCategories.value = BackupCategory.values().toSet()
            _isAllSelected.value = true
        }
    }

    fun startBackup(
        onFullBackup: () -> Unit,
        onFullJsonBackup: () -> Unit,
        onSelectiveExport: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (_selectedCategories.value.isEmpty()) {
            onError("error_no_category_selected")
            return
        }

        if (_isAllSelected.value) {
            // Standard binary backup is still available, but we prefer JSON for reliability
            // For now, let's offer both or switch to JSON.
            // Let's implement full JSON backup as the default robust option.
            onFullJsonBackup()
        } else {
            viewModelScope.launch {
                _isExporting.value = true
                try {
                    val exportData = StringBuilder()
                    exportData.append("FARM MANAGEMENT SELECTIVE EXPORT\n")
                    exportData.append("Generated on: ${java.util.Date()}\n\n")

                    if (_selectedCategories.value.contains(BackupCategory.FARMS)) {
                        exportData.append("--- FARMS DATA ---\n")
                    }
                    if (_selectedCategories.value.contains(BackupCategory.CUSTOMERS)) {
                        exportData.append("--- CUSTOMERS DATA ---\n")
                    }

                    onSelectiveExport(exportData.toString())
                } catch (e: Exception) {
                    onError(e.message ?: "Unknown error")
                } finally {
                    _isExporting.value = false
                }
            }
        }
    }

    fun performFullJsonBackup(context: android.content.Context, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isExporting.value = true
            try {
                val data = apps.farm.data.model.DataBackupModel(
                    farms = farmRepository.getAllFarmsSync(),
                    cycles = cycleRepository.getAllCyclesSync(),
                    customers = customerRepository.getAllCustomersSync(),
                    safes = safeRepository.getAllSafesSync(),
                    invoices = invoiceRepository.getAllInvoicesSync(),
                    receives = receiveRepository.getAllReceivesSync(),
                    emptyWeights = invoiceRepository.getAllEmptyWeights(),
                    grossWeights = invoiceRepository.getAllGrossWeights()
                )
                
                apps.farm.utils.DatabaseBackupUtils.exportToJson(context, data)
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error")
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun performJsonRestore(
        context: android.content.Context,
        uri: android.net.Uri,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                apps.farm.utils.DatabaseBackupUtils.importFromJson(
                    context,
                    uri,
                    onDataParsed = { data ->
                        viewModelScope.launch {
                            try {
                                // Important: Run in a single atomic transaction
                                invoiceRepository.fullDataRestore(
                                    farms = data.farms,
                                    cycles = data.cycles,
                                    customers = data.customers,
                                    safes = data.safes,
                                    invoices = data.invoices,
                                    receives = data.receives,
                                    emptyWeights = data.emptyWeights,
                                    grossWeights = data.grossWeights
                                )
                                onSuccess()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                onError(e.message ?: "Restoration failed during DB insertion")
                            }
                        }
                    },
                    onError = { error ->
                        onError(error)
                    }
                )
            } catch (e: Exception) {
                onError(e.message ?: "Unknown restoration error")
            }
        }
    }
}
