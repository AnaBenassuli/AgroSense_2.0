package com.agrosense.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.agrosense.app.data.local.DatabaseProvider
import com.agrosense.app.data.local.ImagenStorage
import java.io.File
import com.agrosense.app.data.repository.ActividadRepository
import com.agrosense.app.data.repository.DiagnosticoRepository
import com.agrosense.app.data.repository.ParcelaRepository
import com.agrosense.app.data.repository.UsuarioRepository
import com.agrosense.app.ui.screen.ActividadScreen
import com.agrosense.app.ui.screen.DiagnosticoScreen
import com.agrosense.app.ui.screen.HistorialScreen
import com.agrosense.app.ui.screen.HomeScreen
import com.agrosense.app.ui.screen.ParcelaScreen
import com.agrosense.app.ui.screen.PerfilScreen
import com.agrosense.app.ui.screen.auth.AuthGate
import com.agrosense.app.ui.viewmodel.ActividadViewModel
import com.agrosense.app.ui.viewmodel.ActividadViewModelFactory
import com.agrosense.app.ui.viewmodel.DiagnosticoViewModel
import com.agrosense.app.ui.viewmodel.DiagnosticoViewModelFactory
import com.agrosense.app.ui.viewmodel.EstadoAcceso
import com.agrosense.app.ui.viewmodel.ParcelaViewModel
import com.agrosense.app.ui.viewmodel.ParcelaViewModelFactory
import com.agrosense.app.ui.viewmodel.UsuarioViewModel
import com.agrosense.app.ui.viewmodel.UsuarioViewModelFactory

@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val context = LocalContext.current

    val database = DatabaseProvider.getDatabase(context)

    // =====================================================
    // REPOSITORIES
    // =====================================================

    val parcelaRepository = ParcelaRepository(
        database.parcelaDao()
    )

    val diagnosticoRepository = DiagnosticoRepository(
        database.diagnosticoDao()
    )

    val actividadRepository = ActividadRepository(
        database.actividadDao()
    )

    val usuarioRepository = UsuarioRepository(
        database.usuarioDao()
    )

    // =====================================================
    // VIEWMODELS
    // =====================================================

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

    val usuarioViewModel: UsuarioViewModel = viewModel(
        factory = UsuarioViewModelFactory(
            usuarioRepository
        )
    )

    // Una vez por arranque: borra fotos que ningún diagnóstico usa (de cualquier cuenta). Las
    // dejaban versiones anteriores, que guardaban la foto al elegirla aunque se cancelara.
    LaunchedEffect(Unit) {
        diagnosticoViewModel.limpiarImagenesHuerfanas(
            File(context.filesDir, ImagenStorage.CARPETA)
        )
    }

    // =====================================================
    // SESIÓN
    // =====================================================

    val estadoSesion by usuarioViewModel
        .estadoAcceso
        .collectAsState()

    val usuarioActual =
        (estadoSesion as? EstadoAcceso.Autenticado)?.usuario

    // Parcelas y diagnósticos se filtran por el usuario con la sesión iniciada. Sin sesión
    // (null) las listas quedan vacías, así una cuenta nueva no ve los datos de la anterior.
    LaunchedEffect(usuarioActual?.id) {
        parcelaViewModel.establecerUsuario(usuarioActual?.id)
        diagnosticoViewModel.establecerUsuario(usuarioActual?.id)
        actividadViewModel.establecerUsuario(usuarioActual?.id)
    }

    // Vuelve al login y borra el historial de pantallas, para que "atrás" no regrese a la cuenta anterior
    val cerrarSesion: () -> Unit = {
        usuarioViewModel.cerrarSesion()
        navController.navigate(
            Screen.Login.route
        ) {
            popUpTo(0)
        }
    }

    // =====================================================
    // NAVIGATION
    // =====================================================

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {

        // LOGIN
        composable(Screen.Login.route) {

            val estadoAcceso by usuarioViewModel
                .estadoAcceso
                .collectAsState()

            LaunchedEffect(estadoAcceso) {

                if (estadoAcceso is EstadoAcceso.Autenticado) {

                    navController.navigate(
                        Screen.Home.route
                    ) {

                        popUpTo(
                            Screen.Login.route
                        ) {
                            inclusive = true
                        }
                    }
                }
            }

            AuthGate(
                estado = estadoAcceso,
                onIniciarSesion = usuarioViewModel::iniciarSesion,
                onRegistrar = usuarioViewModel::registrarCuenta,
                onLimpiarError = usuarioViewModel::limpiarErrorAcceso
            )
        }

        // REGISTRO
        composable(Screen.Register.route) {

            // El registro actualmente está
            // integrado dentro de AuthGate.
        }

        // HOME
        composable(Screen.Home.route) {

            val parcelas by parcelaViewModel
                .parcelas
                .collectAsState()

            val totalDiagnosticos by diagnosticoViewModel
                .totalDiagnosticos
                .collectAsState()

            val totalActividades by actividadViewModel
                .totalActividades
                .collectAsState()

            HomeScreen(
                nombreUsuario = usuarioActual?.nombre.orEmpty(),
                totalParcelas = parcelas.size,
                totalDiagnosticos = totalDiagnosticos,
                totalActividades = totalActividades,
                onCerrarSesion = cerrarSesion,
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
                },

                onPerfilClick = {
                    navController.navigate(
                        Screen.Perfil.route
                    )
                }
            )
        }

        // PARCELAS
        composable(Screen.Parcelas.route) {

            ParcelaScreen(
                viewModel = parcelaViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // DIAGNÓSTICOS
        composable(Screen.Diagnostico.route) {

            DiagnosticoScreen(
                diagnosticoViewModel = diagnosticoViewModel,
                parcelaViewModel = parcelaViewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                // Sustituye Diagnóstico por Parcelas, así "Inicio" desde Parcelas vuelve al inicio
                onIrAParcelas = {
                    navController.navigate(
                        Screen.Parcelas.route
                    ) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        // HISTORIAL
        composable(Screen.Historial.route) {

            HistorialScreen(
                diagnosticoViewModel = diagnosticoViewModel,
                parcelaViewModel = parcelaViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // ACTIVIDADES
        composable(Screen.Actividades.route) {

            ActividadScreen(
                actividadViewModel = actividadViewModel,
                parcelaViewModel = parcelaViewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onIrAParcelas = {
                    navController.navigate(
                        Screen.Parcelas.route
                    ) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        // MI PERFIL
        composable(Screen.Perfil.route) {

            PerfilScreen(
                usuario = usuarioActual,
                onGuardar = usuarioViewModel::actualizarPerfil,
                onCerrarSesion = cerrarSesion,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}