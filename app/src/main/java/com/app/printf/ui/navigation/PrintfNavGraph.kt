package com.app.printf.ui.navigation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.printf.R
import com.app.printf.ui.screens.CreateInvoiceScreen
import com.app.printf.ui.screens.CustomersScreen
import com.app.printf.ui.screens.HistoryScreen
import com.app.printf.ui.screens.PdfPreviewScreen
import com.app.printf.ui.screens.ProductsScreen
import com.app.printf.ui.screens.ProfileScreen
import com.app.printf.ui.screens.SelectProductScreen
import com.app.printf.ui.theme.BrandWhite
import com.app.printf.ui.viewmodel.CustomerViewModel
import com.app.printf.ui.viewmodel.HistoryViewModel
import com.app.printf.ui.viewmodel.InvoiceViewModel
import com.app.printf.ui.viewmodel.PdfPreviewViewModel
import com.app.printf.ui.viewmodel.ProductViewModel
import com.app.printf.ui.viewmodel.ProfileViewModel
import com.app.printf.ui.viewmodel.SelectProductViewModel
import com.app.printf.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

object Routes {
    const val PRODUCTS = "products"
    const val CUSTOMERS = "customers"
    const val INVOICE = "invoice"
    const val HISTORY = "history"
    const val PROFILE = "profile"
    const val SELECT_PRODUCT = "select_product"
    const val PDF_PREVIEW = "pdf_preview/{invoiceId}"

    fun pdfPreview(invoiceId: Long) = "pdf_preview/$invoiceId"
}

private val bottomNavRoutes = setOf(
    Routes.PRODUCTS,
    Routes.CUSTOMERS,
    Routes.INVOICE,
    Routes.HISTORY,
    Routes.PROFILE,
)

private data class BottomNavItem(
    val route: String,
    val labelRes: Int,
    val icon: @Composable () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrintfNavGraph(
    factory: ViewModelFactory,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val productViewModel: ProductViewModel = viewModel(factory = factory)
    val customerViewModel: CustomerViewModel = viewModel(factory = factory)
    val invoiceViewModel: InvoiceViewModel = viewModel(factory = factory)
    val historyViewModel: HistoryViewModel = viewModel(factory = factory)
    val profileViewModel: ProfileViewModel = viewModel(factory = factory)
    val editInvoiceScope = rememberCoroutineScope()
    val context = LocalContext.current
    var showExitDialog by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavRoutes
    val canNavigateBack = navController.previousBackStackEntry != null

    BackHandler {
        if (canNavigateBack) {
            navController.popBackStack()
        } else {
            showExitDialog = true
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(text = stringResource(R.string.exit_app_title)) },
            text = { Text(text = stringResource(R.string.exit_app_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        (context as? Activity)?.finish()
                    },
                ) {
                    Text(text = stringResource(R.string.exit))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
        )
    }

    val bottomItems = listOf(
        BottomNavItem(
            route = Routes.PRODUCTS,
            labelRes = R.string.nav_products,
            icon = { Icon(Icons.Default.Inventory2, contentDescription = null) },
        ),
        BottomNavItem(
            route = Routes.CUSTOMERS,
            labelRes = R.string.nav_customers,
            icon = { Icon(Icons.Default.People, contentDescription = null) },
        ),
        BottomNavItem(
            route = Routes.INVOICE,
            labelRes = R.string.nav_invoice,
            icon = { Icon(Icons.Default.Receipt, contentDescription = null) },
        ),
        BottomNavItem(
            route = Routes.HISTORY,
            labelRes = R.string.nav_history,
            icon = { Icon(Icons.Default.History, contentDescription = null) },
        ),
        BottomNavItem(
            route = Routes.PROFILE,
            labelRes = R.string.nav_profile,
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
        ),
    )

    val topBarTitle = when (currentRoute) {
        Routes.PRODUCTS -> stringResource(R.string.nav_products)
        Routes.CUSTOMERS -> stringResource(R.string.nav_customers)
        Routes.INVOICE -> stringResource(R.string.nav_invoice)
        Routes.HISTORY -> stringResource(R.string.nav_history)
        Routes.PROFILE -> stringResource(R.string.nav_profile)
        else -> stringResource(R.string.app_name)
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (showBottomBar) {
                TopAppBar(
                    title = {
                        androidx.compose.foundation.layout.Column {
                            Text(
                                text = topBarTitle,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = BrandWhite,
                            )
                            Text(
                                text = stringResource(R.string.company_tagline),
                                style = MaterialTheme.typography.labelMedium,
                                color = BrandWhite.copy(alpha = 0.85f),
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = BrandWhite,
                        actionIconContentColor = BrandWhite,
                    ),
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = BrandWhite,
                    tonalElevation = 6.dp,
                    windowInsets = NavigationBarDefaults.windowInsets,
                ) {
                    bottomItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = item.icon,
                            label = { Text(stringResource(item.labelRes)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.PRODUCTS,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.PRODUCTS) {
                ProductsScreen(
                    viewModel = productViewModel,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            composable(Routes.CUSTOMERS) {
                CustomersScreen(
                    viewModel = customerViewModel,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            composable(Routes.INVOICE) {
                CreateInvoiceScreen(
                    viewModel = invoiceViewModel,
                    onSelectProduct = { navController.navigate(Routes.SELECT_PRODUCT) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            composable(Routes.HISTORY) {
                HistoryScreen(
                    viewModel = historyViewModel,
                    onViewPdf = { invoiceId ->
                        navController.navigate(Routes.pdfPreview(invoiceId))
                    },
                    onEditInvoice = { invoiceId ->
                        editInvoiceScope.launch {
                            invoiceViewModel.loadInvoiceForEditAndAwait(invoiceId)
                            navController.navigate(Routes.INVOICE) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    viewModel = profileViewModel,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            composable(Routes.SELECT_PRODUCT) {
                val selectProductViewModel: SelectProductViewModel = viewModel(factory = factory)
                SelectProductScreen(
                    selectProductViewModel = selectProductViewModel,
                    invoiceViewModel = invoiceViewModel,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = Routes.PDF_PREVIEW,
                arguments = listOf(navArgument("invoiceId") { type = NavType.LongType }),
            ) { backStackEntry ->
                val invoiceId = backStackEntry.arguments?.getLong("invoiceId") ?: return@composable
                val pdfPreviewViewModel: PdfPreviewViewModel = viewModel(factory = factory)
                PdfPreviewScreen(
                    invoiceId = invoiceId,
                    viewModel = pdfPreviewViewModel,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
