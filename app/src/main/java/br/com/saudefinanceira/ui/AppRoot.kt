package br.com.saudefinanceira.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.saudefinanceira.data.AppDatabase
import br.com.saudefinanceira.ui.screens.BillsScreen
import br.com.saudefinanceira.ui.screens.HomeScreen
import br.com.saudefinanceira.ui.screens.InstallmentsScreen
import br.com.saudefinanceira.ui.screens.MoreScreen
import br.com.saudefinanceira.ui.screens.SettingsScreen
import br.com.saudefinanceira.ui.screens.SummaryScreen
import br.com.saudefinanceira.ui.screens.TipsScreen
import br.com.saudefinanceira.ui.screens.ReportsScreen
import br.com.saudefinanceira.ui.components.QuickGuideDialog
import br.com.saudefinanceira.update.UpdateInfo
import br.com.saudefinanceira.update.UpdateState
import br.com.saudefinanceira.update.UpdateViewModel

enum class Dest(val route: String, val label: String, val icon: ImageVector) {
    Home("inicio", "Hoje", Icons.Filled.Home),
    Bills("contas", "Contas", Icons.AutoMirrored.Filled.List),
    More("mais", "Mais", Icons.Filled.Menu),
}

/** Telas abertas a partir de "Mais" (ou de atalhos); têm botão Voltar no topo. */
private val subRoutes = setOf("resumo", "parcelas", "dicas", "ajustes", "relatorios")

private fun NavHostController.open(route: String) {
    navigate(route) { launchSingleTop = true }
}

@Composable
private fun BackBar(onBack: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("← Voltar") }
        }
    }
}

private fun NavHostController.goTo(route: String) {
    navigate(route) {
        popUpTo(Dest.Home.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun AppRoot(db: AppDatabase) {
    val vm: FinanceViewModel = viewModel(factory = FinanceViewModel.Factory(db))
    val updateVm: UpdateViewModel = viewModel()
    val backupVm: BackupViewModel = viewModel()
    val updateState by updateVm.state.collectAsStateWithLifecycle()
    val pendingUpdate: UpdateInfo? = when (val u = updateState) {
        is UpdateState.Available -> u.info
        is UpdateState.Downloading -> u.info
        is UpdateState.Ready -> u.info
        else -> null
    }

    // Reconsulta quando o app volta ao primeiro plano; o ViewModel limita a frequência.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, updateVm) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) updateVm.check()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val context = LocalContext.current
    val guidePrefs = remember(context) { context.getSharedPreferences("saude_financeira_guide", 0) }
    var showGuide by remember { mutableStateOf(!guidePrefs.getBoolean("completed", false)) }
    fun finishGuide() {
        guidePrefs.edit().putBoolean("completed", true).apply()
        showGuide = false
    }

    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { if (current != null && current in subRoutes) BackBar(onBack = { nav.popBackStack() }) },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                Dest.values().forEach { d ->
                    NavigationBarItem(
                        selected = if (d == Dest.More) current != Dest.Home.route && current != Dest.Bills.route else current == d.route,
                        onClick = {
                            if (d == Dest.More) {
                                nav.navigate(d.route) {
                                    popUpTo(Dest.Home.route)
                                    launchSingleTop = true
                                }
                            } else nav.goTo(d.route)
                        },
                        icon = { Icon(d.icon, contentDescription = d.label) },
                        label = { Text(d.label) },
                    )
                }
            }
        },
    ) { padding: PaddingValues ->
        NavHost(nav, startDestination = Dest.Home.route) {
            composable(Dest.Home.route) {
                HomeScreen(
                    vm, padding,
                    pendingUpdate = pendingUpdate,
                    onOpenSettings = { nav.open("ajustes") },
                    onOpenBills = { nav.goTo(Dest.Bills.route) },
                    onOpenSummary = { nav.open("resumo") },
                )
            }
            composable(Dest.Bills.route) { BillsScreen(vm, padding) }
            composable(Dest.More.route) { MoreScreen(padding, onOpen = { nav.open(it) }) }
            composable("resumo") {
                SummaryScreen(vm, padding, onOpenTips = { nav.open("dicas") }, onOpenReports = { nav.open("relatorios") })
            }
            composable("parcelas") { InstallmentsScreen(vm, padding) }
            composable("dicas") { TipsScreen(vm, padding) }
            composable("ajustes") { SettingsScreen(updateVm, backupVm, padding, onOpenGuide = { showGuide = true }) }
            composable("relatorios") { ReportsScreen(vm, padding) }
        }
    }

    if (showGuide) QuickGuideDialog(onFinish = ::finishGuide)
}
