package com.local.paybio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.local.paybio.ui.PaymentViewModel
import com.local.paybio.ui.screens.AddEditScreen
import com.local.paybio.ui.screens.DetailScreen
import com.local.paybio.ui.screens.HomeScreen
import com.local.paybio.ui.screens.IngestScreen
import com.local.paybio.ui.screens.KioskScreen
import com.local.paybio.ui.screens.SettingsScreen
import com.local.paybio.ui.theme.PayBioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PayBioTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    PayBioNavHost()
                }
            }
        }
    }
}

@Composable
fun PayBioNavHost() {
    val navController = rememberNavController()
    val vm: PaymentViewModel = viewModel()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = vm,
                onAddManual = { navController.navigate("edit/-1") },
                onIngest = { navController.navigate("ingest") },
                onOpenDetail = { id -> navController.navigate("detail/$id") },
                onOpenSettings = { navController.navigate("settings") },
                onOpenKiosk = { navController.navigate("kiosk") }
            )
        }
        composable("ingest") {
            IngestScreen(
                viewModel = vm,
                onUseSuggestion = { navController.navigate("edit/-1") },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "edit/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entry ->
            val id = entry.arguments?.getInt("id") ?: -1
            AddEditScreen(
                viewModel = vm,
                methodId = id,
                onDone = { navController.popBackStack("home", inclusive = false) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entry ->
            val id = entry.arguments?.getInt("id") ?: -1
            DetailScreen(
                viewModel = vm,
                methodId = id,
                onEdit = { editId -> navController.navigate("edit/$editId") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenKiosk = { navController.navigate("kiosk") }
            )
        }
        composable("kiosk") {
            KioskScreen(
                viewModel = vm,
                onExit = { navController.popBackStack("home", inclusive = false) }
            )
        }
    }
}
