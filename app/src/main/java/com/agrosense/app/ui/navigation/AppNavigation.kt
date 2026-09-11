package com.agrosense.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.agrosense.app.data.local.DatabaseProvider
import com.agrosense.app.data.repository.ActividadRepository
import com.agrosense.app.data.repository.DiagnosticoRepository
import com.agrosense.app.data.repository.ParcelaRepository
import com.agrosense.app.ui.screen.ActividadScreen
import com.agrosense.app.ui.screen.DiagnosticoScreen
import com.agrosense.app.ui.screen.HistorialScreen
import com.agrosense.app.ui.screen.HomeScreen
import com.agrosense.app.ui.screen.ParcelaScreen
import com.agrosense.app.ui.screen.auth.AuthGate
import com.agrosense.app.ui.viewmodel.ActividadViewModel
import com.agrosense.app.ui.viewmodel.ActividadViewModelFactory
import com.agrosense.app.ui.viewmodel.DiagnosticoViewModel
import com.agrosense.app.ui.viewmodel.DiagnosticoViewModelFactory
import com.agrosense.app.ui.viewmodel.ParcelaViewModel
import com.agrosense.app.ui.viewmodel.ParcelaViewModelFactory

@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val context = LocalContext.current

    val database = DatabaseProvider.getDatabase(context)

    val parcelaRepository = ParcelaRepository(
        database.parcelaDao()
    )

    val diagnosticoRepository = DiagnosticoRepository(
        database.diagnosticoDao()
    )

    val actividadRepository = ActividadRepository(
        database.actividadDao()
    )

    val parcelaViewModel: ParcelaViewModel = viewModel(
        factory = ParcelaViewModelFactory(
            parcelaRepository
        )
    )

    val diagnosticoViewModel: DiagnosticoViewModel = viewModel(
        factory = DiagnosticoViewModelFactory(
            diagnosticoRepository
        )
    )

    val actividadViewModel: ActividadViewModel = viewModel(
        factory = ActividadViewModelFactory(
            actividadRepository
        )
    )

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {

        composable(Screen.Login.route) {

            AuthGate(
                onAuthenticated = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            // Por ahora el registro está integrado dentro de AuthGate.
        }

        composable(Screen.Home.route) {

            HomeScreen(
                onParcelasClick = {
                    navController.navigate(
                        Screen.Parcelas.route
                    )
                },
                onDiagnosticoClick = {
                    navController.navigate(
                        Screen.Diagnostico.route
                    )
                },
                onHistorialClick = {
                    navController.navigate(
                        Screen.Historial.route
                    )
                },
                onActividadesClick = {
                    navController.navigate(
                        Screen.Actividades.route
                    )
                }
            )
        }

        composable(Screen.Parcelas.route) {

            ParcelaScreen(
                viewModel = parcelaViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Diagnostico.route) {

            DiagnosticoScreen(
                diagnosticoViewModel = diagnosticoViewModel,
                parcelaViewModel = parcelaViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Historial.route) {

            HistorialScreen(
                diagnosticoViewModel = diagnosticoViewModel,
                parcelaViewModel = parcelaViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Actividades.route) {

            ActividadScreen(
                actividadViewModel = actividadViewModel,
                parcelaViewModel = parcelaViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}