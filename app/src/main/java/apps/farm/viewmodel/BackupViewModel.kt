package apps.farm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import apps.farm.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
    private val receiveRepository: ReceiveRepository
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

    fun startBackup(onFullBackup: () -> Unit, onSelectiveExport: (String) -> Unit, onError: (String) -> Unit) {
        if (_selectedCategories.value.isEmpty()) {
            onError("error_no_category_selected")
            return
        }

        if (_isAllSelected.value) {
            onFullBackup()
        } else {
            viewModelScope.launch {
                _isExporting.value = true
                try {
                    val exportData = StringBuilder()
                    exportData.append("FARM MANAGEMENT SELECTIVE EXPORT\n")
                    exportData.append("Generated on: ${java.util.Date()}\n\n")

                    if (_selectedCategories.value.contains(BackupCategory.FARMS)) {
                        // In a real app we'd fetch and JSON stringify, for now we just describe the content
                        exportData.append("--- FARMS DATA ---\n")
                    }
                    if (_selectedCategories.value.contains(BackupCategory.CUSTOMERS)) {
                        exportData.append("--- CUSTOMERS DATA ---\n")
                    }
                    // ... and so on

                    onSelectiveExport(exportData.toString())
                } catch (e: Exception) {
                    onError(e.message ?: "Unknown error")
                } finally {
                    _isExporting.value = false
                }
            }
        }
    }
}
