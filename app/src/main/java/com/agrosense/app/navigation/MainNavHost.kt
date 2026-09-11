package com.agrosense.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.agrosense.app.ui.screens.CapturaDiagnosticoScreen
import com.agrosense.app.ui.screens.HistorialDiagnosticosScreen
import com.agrosense.app.ui.screens.HomeScreen
import com.agrosense.app.ui.screens.LoginScreen
import com.agrosense.app.ui.screens.PerfilScreen
import com.agrosense.app.ui.screens.RegistroActividadesScreen
import com.agrosense.app.ui.screens.ResultadoDiagnosticoScreen

/**
 * Grafo de navegación de la app.
 *
 * [startDestination] permite arrancar en Home cuando ya hay una sesión
 * local recordada, sin pasar de nuevo por el login.
 */
@Composable
fun MainNavHost(
    navController: NavHostController,
    startDestination: String = Route.Login.route
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Route.Login.route) {
            LoginScreen(navController)
        }

        composable(Route.Home.route) {
            HomeScreen(navController)
        }

        composable(
            route = Route.CapturaDiagnostico.route,
            arguments = listOf(navArgument(Route.ARG_PARCELA_ID) { type = NavType.LongType })
        ) { backStackEntry ->
            CapturaDiagnosticoScreen(
                navController = navController,
                parcelaId = backStackEntry.parcelaId()
            )
        }

        composable(
            route = Route.ResultadoDiagnostico.route,
            arguments = listOf(navArgument(Route.ARG_DIAGNOSTICO_ID) { type = NavType.LongType })
        ) { backStackEntry ->
            ResultadoDiagnosticoScreen(
                navController = navController,
                diagnosticoId = backStackEntry.arguments?.getLong(Route.ARG_DIAGNOSTICO_ID) ?: 0L
            )
        }

        composable(
            route = Route.HistorialDiagnosticos.route,
            arguments = listOf(navArgument(Route.ARG_PARCELA_ID) { type = NavType.LongType })
        ) { backStackEntry ->
            HistorialDiagnosticosScreen(
                navController = navController,
                parcelaId = backStackEntry.parcelaId()
            )
        }

        composable(
            route = Route.RegistroActividades.route,
            arguments = listOf(navArgument(Route.ARG_PARCELA_ID) { type = NavType.LongType })
        ) { backStackEntry ->
            RegistroActividadesScreen(
                navController = navController,
                parcelaId = backStackEntry.parcelaId()
            )
        }

        composable(Route.Perfil.route) {
            PerfilScreen(navController)
        }
    }
}

private fun androidx.navigation.NavBackStackEntry.parcelaId(): Long =
    arguments?.getLong(Route.ARG_PARCELA_ID) ?: 0L
