package com.agrosense.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.agrosense.app.data.local.entity.Actividad
import com.agrosense.app.ui.viewmodel.ActividadViewModel
import com.agrosense.app.ui.viewmodel.agroSenseViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val TIPOS_ACTIVIDAD = listOf("Riego", "Tratamiento", "Fertilización", "Poda", "Cosecha")

/** Alta y consulta de actividades de campo de una parcela. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroActividadesScreen(navController: NavController, parcelaId: Long) {
    val context = LocalContext.current
    val actividadViewModel: ActividadViewModel = viewModel(factory = agroSenseViewModelFactory(context))

    val actividades by actividadViewModel.observarPorParcela(parcelaId)
        .collectAsStateWithLifecycle(emptyList())

    var tipo by remember { mutableStateOf(TIPOS_ACTIVIDAD.first()) }
    var desplegado by remember { mutableStateOf(false) }
    var descripcion by remember { mutableStateOf("") }
    var fecha by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var mostrandoCalendario by remember { mutableStateOf(false) }

    // Dentro de remember y no en una constante: el idioma puede cambiar con la
    // app abierta, y al hacerlo Android recrea la Activity y esto se reevalúa.
    val formatoFechaCorta = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Actividades", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Parcela $parcelaId",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = desplegado,
            onExpandedChange = { desplegado = it }
        ) {
            OutlinedTextField(
                value = tipo,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tipo de actividad") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = desplegado) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = desplegado,
                onDismissRequest = { desplegado = false }
            ) {
                TIPOS_ACTIVIDAD.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = {
                            tipo = opcion
                            desplegado = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = { mostrandoCalendario = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Fecha: ${formatoFechaCorta.format(Date(fecha))}")
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                actividadViewModel.registrarActividad(
                    Actividad(
                        parcelaId = parcelaId,
                        tipo = tipo,
                        descripcion = descripcion.trim(),
                        fecha = fecha
                    )
                )
                descripcion = ""
                fecha = System.currentTimeMillis()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar actividad")
        }

        Spacer(Modifier.height(24.dp))

        Text("Actividades recientes", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        if (actividades.isEmpty()) {
            Text(
                "Todavía no hay actividades registradas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(actividades, key = { it.id }) { actividad ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                actividad.tipo,
                                style = MaterialTheme.typography.titleSmall
                            )
                            if (actividad.descripcion.isNotBlank()) {
                                Text(
                                    actividad.descripcion,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Text(
                                formatoFechaCorta.format(Date(actividad.fecha)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (mostrandoCalendario) {
        val estado = rememberDatePickerState(initialSelectedDateMillis = fecha)
        DatePickerDialog(
            onDismissRequest = { mostrandoCalendario = false },
            confirmButton = {
                TextButton(onClick = {
                    estado.selectedDateMillis?.let { fecha = it }
                    mostrandoCalendario = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrandoCalendario = false }) { Text("Cancelar") }
            }
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                DatePicker(state = estado)
            }
        }
    }
}


