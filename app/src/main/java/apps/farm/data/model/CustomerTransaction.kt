package apps.farm.data.model

data class CustomerTransaction(
    val date: Long,
    val typeName: String, // E.g., "فاتورة بيع", "تحصيل"
    val operationId: String?, // Invoice/Receive No
    val cycleName: String?,
    val farmName: String?,
    val netWeight: Double?, // Only populated for invoices
    val price: Double?, // Only populated for invoices
    val debit: Double, // Amount added to balance (negative for balance, e.g. invoice total)
    val credit: Double, // Amount subtracted from balance (positive for balance, e.g. receive amount)
    val cumulativeBalance: Double,
    val invoiceReceive: Double = 0.0, // Receive amount tied to this invoice
    val invoiceRemaining: Double = 0.0 // Remaining = totalInvoice - invoiceReceive
)
