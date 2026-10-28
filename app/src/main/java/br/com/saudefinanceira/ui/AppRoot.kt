package br.com.saudefinanceira.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.saudefinanceira.data.FinanceDao
import br.com.saudefinanceira.ui.screens.BillsScreen
import br.com.saudefinanceira.ui.screens.ComingSoonScreen
import br.com.saudefinanceira.ui.screens.HomeScreen
import br.com.saudefinanceira.ui.screens.InstallmentsScreen
import br.com.saudefinanceira.ui.screens.TipsScreen

enum class Dest(val route: String, val label: String, val icon: ImageVector) {
    Home("inicio", "Início", Icons.Filled.Home),
    Bills("contas", "Contas", Icons.AutoMirrored.Filled.List),
    Installments("parcelas", "Parcelas", Icons.Filled.ShoppingCart),
    Tips("dicas", "Dicas", Icons.Filled.Info),
    Settings("ajustes", "Ajustes", Icons.Filled.Settings),
}

private fun NavHostController.goTo(route: String) {
    navigate(route) {
        popUpTo(Dest.Home.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun AppRoot(dao: FinanceDao) {
    val vm: FinanceViewModel = viewModel(factory = FinanceViewModel.Factory(dao))
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                Dest.values().forEach { d ->
                    NavigationBarItem(
                        selected = current == d.route,
                        onClick = { nav.goTo(d.route) },
                        icon = { Icon(d.icon, contentDescription = d.label) },
                        label = { Text(d.label) },
                    )
                }
            }
        },
    ) { padding: PaddingValues ->
        NavHost(nav, startDestination = Dest.Home.route) {
            composable(Dest.Home.route) { HomeScreen(vm, padding, onOpenTips = { nav.goTo(Dest.Tips.route) }) }
            composable(Dest.Bills.route) { BillsScreen(vm, padding) }
            composable(Dest.Installments.route) { InstallmentsScreen(vm, padding) }
            composable(Dest.Tips.route) { TipsScreen(vm, padding) }
            composable(Dest.Settings.route) { ComingSoonScreen("Ajustes", padding) }
        }
    }
}
