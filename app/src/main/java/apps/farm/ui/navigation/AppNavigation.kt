package apps.farm.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import apps.farm.ui.screens.CustomerDetailScreen
import apps.farm.ui.screens.CycleDetailScreen
import apps.farm.ui.screens.FarmDetailScreen
import apps.farm.ui.screens.MainScreen
import apps.farm.ui.screens.ReceiveDetailScreen
import apps.farm.ui.screens.SafeDetailScreen
import apps.farm.ui.screens.SecuritySettingsScreen
import apps.farm.ui.screens.SaleInvoiceDetailScreen
import apps.farm.ui.screens.PdfViewerScreen
import apps.farm.ui.screens.BackupScreen
import apps.farm.ui.screens.CustomerViewScreen
import apps.farm.ui.screens.FarmViewScreen
import apps.farm.ui.screens.InvoiceViewScreen
import apps.farm.ui.screens.ReceiveViewScreen
import apps.farm.ui.screens.SafeViewScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(400)
            ) + fadeIn(animationSpec = tween(400))
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(400)
            ) + fadeOut(animationSpec = tween(400))
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(400)
            ) + fadeIn(animationSpec = tween(400))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(400)
            ) + fadeOut(animationSpec = tween(400))
        }
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToFarmDetail = { farmId ->
                    navController.navigate(Screen.FarmDetail.createRoute(farmId))
                },
                onNavigateToFarmView = { farmId ->
                    navController.navigate(Screen.FarmView.createRoute(farmId))
                },
                onNavigateToCustomerDetail = { customerId ->
                    navController.navigate(Screen.CustomerDetail.createRoute(customerId))
                },
                onNavigateToCustomerView = { customerId ->
                    navController.navigate(Screen.CustomerView.createRoute(customerId))
                },
                onNavigateToSafeDetail = { safeId ->
                    navController.navigate(Screen.SafeDetail.createRoute(safeId))
                },
                onNavigateToSafeView = { safeId ->
                    navController.navigate(Screen.SafeView.createRoute(safeId))
                },
                onNavigateToInvoiceDetail = { invoiceId ->
                    navController.navigate(Screen.SaleInvoiceDetail.createRoute(invoiceId))
                },
                onNavigateToInvoiceView = { invoiceId ->
                    navController.navigate(Screen.InvoiceView.createRoute(invoiceId))
                },
                onNavigateToReceiveDetail = { receiveId ->
                    navController.navigate(Screen.ReceiveDetail.createRoute(receiveId))
                },
                onNavigateToReceiveView = { receiveId ->
                    navController.navigate(Screen.ReceiveView.createRoute(receiveId))
                },
                onNavigateToSecuritySettings = {
                    navController.navigate(Screen.SecuritySettings.route)
                },
                onNavigateToBackup = {
                    navController.navigate(Screen.Backup.route)
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
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPdfViewer = { filePath ->
                    navController.navigate(Screen.PdfViewer.createRoute(filePath))
                }
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
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPdfViewer = { filePath ->
                    navController.navigate(Screen.PdfViewer.createRoute(filePath))
                }
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

        composable(
            route = Screen.FarmView.route,
            arguments = listOf(navArgument("farmId") { type = NavType.StringType })
        ) { backStackEntry ->
            val farmId = backStackEntry.arguments?.getString("farmId") ?: return@composable
            FarmViewScreen(
                farmId = farmId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id -> navController.navigate(Screen.FarmDetail.createRoute(id)) },
                onNavigateToCycleDetail = { cycleId, fId -> 
                    val cId = cycleId ?: "new"
                    navController.navigate(Screen.CycleDetail.createRoute(cId, fId)) 
                },
                onNavigateToPdfViewer = { pdfPath ->
                    val encodedPath = java.net.URLEncoder.encode(pdfPath, "UTF-8")
                    navController.navigate(Screen.PdfViewer.createRoute(encodedPath))
                }
            )
        }

        composable(
            route = Screen.CustomerView.route,
            arguments = listOf(navArgument("customerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId") ?: return@composable
            CustomerViewScreen(
                customerId = customerId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id -> navController.navigate(Screen.CustomerDetail.createRoute(id)) },
                onNavigateToPdfViewer = { pdfPath ->
                    val encodedPath = java.net.URLEncoder.encode(pdfPath, "UTF-8")
                    navController.navigate(Screen.PdfViewer.createRoute(encodedPath))
                }
            )
        }

        composable(
            route = Screen.SafeView.route,
            arguments = listOf(navArgument("safeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val safeId = backStackEntry.arguments?.getString("safeId") ?: return@composable
            SafeViewScreen(
                safeId = safeId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id -> navController.navigate(Screen.SafeDetail.createRoute(id)) }
            )
        }

        composable(
            route = Screen.InvoiceView.route,
            arguments = listOf(navArgument("invoiceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getString("invoiceId") ?: return@composable
            InvoiceViewScreen(
                invoiceId = invoiceId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id -> navController.navigate(Screen.SaleInvoiceDetail.createRoute(id)) }
            )
        }

        composable(
            route = Screen.ReceiveView.route,
            arguments = listOf(navArgument("receiveId") { type = NavType.StringType })
        ) { backStackEntry ->
            val receiveId = backStackEntry.arguments?.getString("receiveId") ?: return@composable
            ReceiveViewScreen(
                receiveId = receiveId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id -> navController.navigate(Screen.ReceiveDetail.createRoute(id)) }
            )
        }

        composable(Screen.SecuritySettings.route) {
            SecuritySettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Backup.route) {
            BackupScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.PdfViewer.route,
            arguments = listOf(navArgument("filePath") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedPath = backStackEntry.arguments?.getString("filePath") ?: ""
            val filePath = java.net.URLDecoder.decode(encodedPath, "UTF-8")
            
            PdfViewerScreen(
                filePath = filePath,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
