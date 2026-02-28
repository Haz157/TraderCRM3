package apps.farm.ui.navigation

sealed class Screen(val route: String) {
    data object Main : Screen("main")
    data object FarmDetail : Screen("farm_detail/{farmId}") {
        fun createRoute(farmId: String?) = "farm_detail/${farmId ?: "new"}"
    }

    data object CycleDetail : Screen("cycle_detail/{cycleId}/{farmId}") {
        fun createRoute(cycleId: String?, farmId: String) =
            "cycle_detail/${cycleId ?: "new"}/$farmId"
    }

    data object CustomerDetail : Screen("customer_detail/{customerId}") {
        fun createRoute(customerId: String?) = "customer_detail/${customerId ?: "new"}"
    }

    data object SafeDetail : Screen("safe_detail/{safeId}") {
        fun createRoute(safeId: String?) = "safe_detail/${safeId ?: "new"}"
    }

    data object SaleInvoiceDetail : Screen("sale_invoice_detail/{invoiceId}") {
        fun createRoute(invoiceId: String?) = "sale_invoice_detail/${invoiceId ?: "new"}"
    }

    data object ReceiveDetail : Screen("receive_detail/{receiveId}") {
        fun createRoute(receiveId: String?) = "receive_detail/${receiveId ?: "new"}"
    }

    data object SecuritySettings : Screen("security_settings")
    data object Backup : Screen("backup")
    
    data object PdfViewer : Screen("pdf_viewer/{filePath}") {
        fun createRoute(filePath: String) = "pdf_viewer/${java.net.URLEncoder.encode(filePath, "UTF-8")}"
    }

    // View-only screens
    data object FarmView : Screen("farm_view/{farmId}") {
        fun createRoute(farmId: String) = "farm_view/$farmId"
    }
    data object CustomerView : Screen("customer_view/{customerId}") {
        fun createRoute(customerId: String) = "customer_view/$customerId"
    }
    data object SafeView : Screen("safe_view/{safeId}") {
        fun createRoute(safeId: String) = "safe_view/$safeId"
    }
    data object InvoiceView : Screen("invoice_view/{invoiceId}") {
        fun createRoute(invoiceId: String) = "invoice_view/$invoiceId"
    }
    data object ReceiveView : Screen("receive_view/{receiveId}") {
        fun createRoute(receiveId: String) = "receive_view/$receiveId"
    }
}
