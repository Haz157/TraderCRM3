package apps.farm.ui.navigation

sealed class Screen(val route: String) {
    data object Main : Screen("main")
    data object FarmDetail : Screen("farm_detail/{farmId}") {
        fun createRoute(farmId: String?) = "farm_detail/${farmId ?: "new"}"
    }
    data object CycleDetail : Screen("cycle_detail/{cycleId}/{farmId}") {
        fun createRoute(cycleId: String?, farmId: String) = "cycle_detail/${cycleId ?: "new"}/$farmId"
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
}
