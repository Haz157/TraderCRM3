package apps.farm.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import apps.farm.ui.screens.CustomerDetailScreen
import apps.farm.ui.screens.CycleDetailScreen
import apps.farm.ui.screens.FarmDetailScreen
import apps.farm.ui.screens.MainScreen
import apps.farm.ui.screens.ReceiveDetailScreen
import apps.farm.ui.screens.SafeDetailScreen
import apps.farm.ui.screens.SaleInvoiceDetailScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToFarmDetail = { farmId ->
                    navController.navigate(Screen.FarmDetail.createRoute(farmId))
                },
                onNavigateToCycleDetail = { cycleId, farmId ->
                    if (farmId != null) {
                        navController.navigate(Screen.CycleDetail.createRoute(cycleId, farmId))
                    }
                },
                onNavigateToCustomerDetail = { customerId ->
                    navController.navigate(Screen.CustomerDetail.createRoute(customerId))
                },
                onNavigateToSafeDetail = { safeId ->
                    navController.navigate(Screen.SafeDetail.createRoute(safeId))
                },
                onNavigateToInvoiceDetail = { invoiceId ->
                    navController.navigate(Screen.SaleInvoiceDetail.createRoute(invoiceId))
                },
                onNavigateToReceiveDetail = { receiveId ->
                    navController.navigate(Screen.ReceiveDetail.createRoute(receiveId))
                }
            )
        }

        composable(
            route = Screen.FarmDetail.route,
            arguments = listOf(navArgument("farmId") { type = NavType.StringType })
        ) { backStackEntry ->
            val farmId = backStackEntry.arguments?.getString("farmId")
            val actualFarmId = if (farmId == "new") null else farmId

            FarmDetailScreen(
                farmId = actualFarmId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCycleDetail = { cycleId, farmIdParam ->
                    navController.navigate(Screen.CycleDetail.createRoute(cycleId, farmIdParam))
                }
            )
        }

        composable(
            route = Screen.CycleDetail.route,
            arguments = listOf(
                navArgument("cycleId") { type = NavType.StringType },
                navArgument("farmId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val cycleId = backStackEntry.arguments?.getString("cycleId")
            val farmId = backStackEntry.arguments?.getString("farmId") ?: ""
            val actualCycleId = if (cycleId == "new") null else cycleId

            CycleDetailScreen(
                cycleId = actualCycleId,
                farmId = farmId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CustomerDetail.route,
            arguments = listOf(navArgument("customerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId")
            val actualCustomerId = if (customerId == "new") null else customerId

            CustomerDetailScreen(
                customerId = actualCustomerId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.SafeDetail.route,
            arguments = listOf(navArgument("safeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val safeId = backStackEntry.arguments?.getString("safeId")
            val actualSafeId = if (safeId == "new") null else safeId

            SafeDetailScreen(
                safeId = actualSafeId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.SaleInvoiceDetail.route,
            arguments = listOf(navArgument("invoiceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getString("invoiceId")
            val actualInvoiceId = if (invoiceId == "new") null else invoiceId

            SaleInvoiceDetailScreen(
                invoiceId = actualInvoiceId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ReceiveDetail.route,
            arguments = listOf(navArgument("receiveId") { type = NavType.StringType })
        ) { backStackEntry ->
            val receiveId = backStackEntry.arguments?.getString("receiveId")
            val actualReceiveId = if (receiveId == "new") null else receiveId

            ReceiveDetailScreen(
                receiveId = actualReceiveId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
