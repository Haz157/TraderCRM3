package apps.farm.data.model

import com.google.gson.annotations.SerializedName

/**
 * A container for all database tables to be exported as a JSON-based backup file (.dmp).
 */
data class DataBackupModel(
    @SerializedName("farms") val farms: List<Farm> = emptyList(),
    @SerializedName("cycles") val cycles: List<Cycle> = emptyList(),
    @SerializedName("customers") val customers: List<Customer> = emptyList(),
    @SerializedName("safes") val safes: List<Safe> = emptyList(),
    @SerializedName("invoices") val invoices: List<SaleInvoice> = emptyList(),
    @SerializedName("receives") val receives: List<Receive> = emptyList(),
    @SerializedName("empty_weights") val emptyWeights: List<EmptyWeight> = emptyList(),
    @SerializedName("gross_weights") val grossWeights: List<GrossWeight> = emptyList(),
    @SerializedName("export_date") val exportDate: Long = System.currentTimeMillis(),
    @SerializedName("app_version") val appVersion: String = "1.0"
)
