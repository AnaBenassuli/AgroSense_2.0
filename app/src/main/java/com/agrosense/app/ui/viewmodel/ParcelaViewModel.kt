package com.agrosense.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrosense.app.data.local.entity.Parcela
import com.agrosense.app.data.repository.ParcelaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ParcelaViewModel(
    private val repository: ParcelaRepository
) : ViewModel() {

    val parcelas: StateFlow<List<Parcela>> =
        repository.observarParcelas()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun registrarParcela(
        nombre: String,
        ubicacion: String,
        cultivo: String
    ) {
        viewModelScope.launch {
            repository.registrarParcela(
                nombre = nombre,
                ubicacion = ubicacion,
                cultivo = cultivo
            )
        }
    }

    fun actualizarParcela(parcela: Parcela) {
        viewModelScope.launch {
            repository.actualizarParcela(parcela)
        }
    }

    fun eliminarParcela(parcela: Parcela) {
        viewModelScope.launch {
            repository.eliminarParcela(parcela)
        }
    }
}