package apps.farm.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import apps.farm.R
import apps.farm.data.model.Customer
import apps.farm.data.model.Farm
import apps.farm.ui.theme.BalanceNegative
import apps.farm.ui.theme.BalanceNeutral
import apps.farm.ui.theme.BalancePositive
import apps.farm.ui.theme.PrimaryContainer
import apps.farm.ui.theme.PrimaryDark
import apps.farm.ui.theme.StatusActive
import apps.farm.ui.theme.StatusActiveContainer
import apps.farm.ui.theme.StatusBlocked
import apps.farm.ui.theme.StatusBlockedContainer
import apps.farm.ui.theme.TextPrimary
import apps.farm.ui.theme.TextSecondary
import apps.farm.ui.theme.icons.Farm
import apps.farm.viewmodel.CustomerViewModel
import apps.farm.viewmodel.FarmViewModel

data class TabItem(
    val title: String,
    val route: String,
    val icon: @Composable () -> Unit
)

sealed class TabScreen(val route: String) {
    data object Farms : TabScreen("farms")
    data object Customers : TabScreen("customers")
    data object Safes : TabScreen("safes")
    data object Invoices : TabScreen("invoices")
    data object Receives : TabScreen("receives")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToFarmDetail: (String?) -> Unit,
    onNavigateToCycleDetail: (String?, String?) -> Unit,
    onNavigateToCustomerDetail: (String?) -> Unit,
    onNavigateToSafeDetail: (String?) -> Unit,
    onNavigateToInvoiceDetail: (String?) -> Unit,
    onNavigateToReceiveDetail: (String?) -> Unit,
    farmViewModel: FarmViewModel = hiltViewModel(),
    customerViewModel: CustomerViewModel = hiltViewModel()
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    },
//                    navigationIcon = {
//                        IconButton(
//                            onClick = { /* TODO: Add menu action */ }
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Menu,
//                                contentDescription = stringResource(R.string.content_description_menu),
//                                tint = MaterialTheme.colorScheme.onPrimary
//                            )
//                        }
//                    },
//                    actions = {
//                        IconButton(
//                            onClick = { /* TODO: Add search action */ }
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Search,
//                                contentDescription = stringResource(R.string.content_description_search),
//                                tint = MaterialTheme.colorScheme.onPrimary
//                            )
//                        }
//                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        },
        bottomBar = {
            Surface(
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 0.dp
                ) {
                val items = listOf(
                    TabItem(stringResource(R.string.tab_farms), TabScreen.Farms.route) { Icon(Icons.Default.Agriculture, contentDescription = null) },
                    TabItem(stringResource(R.string.tab_customers), TabScreen.Customers.route) { Icon(Icons.Default.Person, contentDescription = null) },
                    TabItem(stringResource(R.string.tab_safes), TabScreen.Safes.route) { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                    TabItem(stringResource(R.string.tab_invoices), TabScreen.Invoices.route) { Icon(Icons.Default.Receipt, contentDescription = null) },
                    TabItem(stringResource(R.string.tab_receives), TabScreen.Receives.route) { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) }
                )
                
                items.forEach { item ->
                    val selected = currentRoute == item.route
                    NavigationBarItem(
                        icon = item.icon,
                        label = { Text(item.title) },
                        selected = selected,
                        onClick = {
                            if (currentRoute != item.route) {
                                bottomNavController.navigate(item.route) {
                                    popUpTo(bottomNavController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    },
        floatingActionButton = {
            AnimatedVisibility(
                visible = true,
                enter = scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)),
                exit = scaleOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        when (currentRoute) {
                            TabScreen.Farms.route -> onNavigateToFarmDetail(null)
                            TabScreen.Customers.route -> onNavigateToCustomerDetail(null)
                            TabScreen.Safes.route -> onNavigateToSafeDetail(null)
                            TabScreen.Invoices.route -> onNavigateToInvoiceDetail(null)
                            TabScreen.Receives.route -> onNavigateToReceiveDetail(null)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.content_description_add),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            NavHost(
                navController = bottomNavController,
                startDestination = TabScreen.Farms.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(TabScreen.Farms.route) {
                    FarmsTab(farmViewModel, onNavigateToFarmDetail)
                }
                composable(TabScreen.Customers.route) {
                    CustomersTab(customerViewModel, onNavigateToCustomerDetail)
                }
                composable(TabScreen.Safes.route) {
                    SafesScreen(onNavigateToSafeDetail = onNavigateToSafeDetail)
                }
                composable(TabScreen.Invoices.route) {
                    InvoicesScreen(onNavigateToInvoiceDetail = onNavigateToInvoiceDetail)
                }
                composable(TabScreen.Receives.route) {
                    ReceivesScreen(onNavigateToReceiveDetail = onNavigateToReceiveDetail)
                }
            }
        }
    }
}

@Composable
fun FarmsTab(
    farmViewModel: FarmViewModel,
    onNavigateToFarmDetail: (String?) -> Unit
) {
    val farms by farmViewModel.allFarms.collectAsState(initial = emptyList())

    if (farms.isEmpty()) {
        EmptyStateMessage(
            icon = Icons.Farm,
            message = stringResource(R.string.message_no_cycles)
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = farms,
                key = { it.id }
            ) { farm ->
                FarmCard(
                    farm = farm,
                    onEdit = { onNavigateToFarmDetail(farm.id) },
                    onToggleBlock = { farmViewModel.toggleBlockStatus(farm.id, !farm.blocked) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmCard(
    farm: Farm,
    onEdit: () -> Unit,
    onToggleBlock: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Card(
        onClick = onEdit,
        modifier = Modifier
            .fillMaxWidth()
            .scale(animatedScale),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (farm.blocked) StatusBlockedContainer else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (farm.blocked) 2.dp else 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Status Indicator
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (farm.blocked) StatusBlocked else StatusActive,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (farm.blocked) Icons.Default.Block else Icons.Farm,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(24.dp),
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = farm.farmName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = if (farm.blocked) StatusBlocked else TextPrimary
                        )
                        if (farm.farmNote.isNotEmpty()) {
                            Text(
                                text = farm.farmNote,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }

                //  Toggle Button
                FilledIconToggleButton(
                    checked = !farm.blocked,
                    onCheckedChange = { onToggleBlock() },
                    modifier = Modifier.size(44.dp),
                    colors = IconButtonDefaults.filledIconToggleButtonColors(
                        checkedContainerColor = StatusActiveContainer,
                        checkedContentColor = StatusActive,
                        containerColor = StatusBlockedContainer,
                        contentColor = StatusBlocked
                    )
                ) {
                    Icon(
                        imageVector = if (farm.blocked) Icons.Default.Block else Icons.Default.CheckCircle,
                        contentDescription = if (farm.blocked) stringResource(R.string.status_blocked) else stringResource(R.string.status_active),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (farm.blocked) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = StatusBlocked.copy(alpha = 0.1f),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(
                        text = stringResource(R.string.status_blocked),
                        color = StatusBlocked,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CustomersTab(
    customerViewModel: CustomerViewModel,
    onNavigateToCustomerDetail: (String?) -> Unit
) {
    val customers by customerViewModel.allCustomers.collectAsState(initial = emptyList())

    if (customers.isEmpty()) {
        EmptyStateMessage(
            icon = Icons.Default.Person,
            message = stringResource(R.string.message_no_cycles)
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = customers,
                key = { it.id }
            ) { customer ->
                CustomerCard(
                    customer = customer,
                    onEdit = { onNavigateToCustomerDetail(customer.id) },
                    onToggleBlock = { customerViewModel.toggleBlockStatus(customer.id, !customer.blocked) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerCard(
    customer: Customer,
    onEdit: () -> Unit,
    onToggleBlock: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    val balanceColor = when {
        customer.balance < 0 -> BalanceNegative
        customer.balance > 0 -> BalancePositive
        else -> BalanceNeutral
    }

    Card(
        onClick = onEdit,
        modifier = Modifier
            .fillMaxWidth()
            .scale(animatedScale),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (customer.blocked) StatusBlockedContainer else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (customer.blocked) 2.dp else 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Avatar/Status Indicator
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (customer.blocked) StatusBlocked else PrimaryContainer,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(10.dp)
                                .size(24.dp),
                            tint = if (customer.blocked) Color.White else PrimaryDark
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = customer.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = if (customer.blocked) StatusBlocked else TextPrimary
                        )
                        if (customer.phone.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.label_phone, customer.phone),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

                //  Toggle Button
                FilledIconToggleButton(
                    checked = !customer.blocked,
                    onCheckedChange = { onToggleBlock() },
                    modifier = Modifier.size(44.dp),
                    colors = IconButtonDefaults.filledIconToggleButtonColors(
                        checkedContainerColor = StatusActiveContainer,
                        checkedContentColor = StatusActive,
                        containerColor = StatusBlockedContainer,
                        contentColor = StatusBlocked
                    )
                ) {
                    Icon(
                        imageVector = if (customer.blocked) Icons.Default.Block else Icons.Default.CheckCircle,
                        contentDescription = if (customer.blocked) stringResource(R.string.status_blocked) else stringResource(R.string.status_active),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Balance Chip
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = balanceColor.copy(alpha = 0.1f),
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(
                    text = stringResource(R.string.label_balance, customer.balance),
                    color = balanceColor,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            if (customer.blocked) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = StatusBlocked.copy(alpha = 0.1f),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(
                        text = stringResource(R.string.status_blocked),
                        color = StatusBlocked,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateMessage(
    icon: ImageVector,
    message: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PrimaryContainer,
            modifier = Modifier.size(80.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(20.dp)
                    .size(40.dp),
                tint = PrimaryDark
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary
        )
    }
}
