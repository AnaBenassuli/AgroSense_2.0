package com.agrosense.app.ui.navigation

sealed class Screen(val route: String) {

    data object Login : Screen("login")

    data object Home : Screen("home")

    data object Parcelas : Screen("parcelas")

    data object Diagnostico : Screen("diagnostico")

    data object Historial : Screen("historial")

    data object Actividades : Screen("actividades")

    data object Perfil : Screen("perfil")
}