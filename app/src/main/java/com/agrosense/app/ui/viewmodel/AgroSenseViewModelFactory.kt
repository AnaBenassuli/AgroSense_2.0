package com.agrosense.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.agrosense.app.di.AppModule

/**
 * Factory única para los ViewModels de la app.
 *
 * Se usa el applicationContext a propósito: el ViewModel sobrevive al
 * cambio de configuración y quedarse con el Context de la Activity lo
 * convertiría en una fuga de memoria.
 */
fun agroSenseViewModelFactory(context: Context): ViewModelProvider.Factory {
    val app = context.applicationContext
    return viewModelFactory {
        initializer { ParcelaViewModel(AppModule.parcelaRepository(app)) }
        initializer { DiagnosticoViewModel(AppModule.diagnosticoRepository(app)) }
        initializer { ActividadViewModel(AppModule.actividadRepository(app)) }
        initializer { SensorViewModel(AppModule.sensorRepository(app)) }
        initializer { UsuarioViewModel(AppModule.usuarioRepository(app)) }
    }
}
