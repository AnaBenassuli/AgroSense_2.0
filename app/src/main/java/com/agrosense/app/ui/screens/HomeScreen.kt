package com.agrosense.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.agrosense.app.data.local.entity.Parcela
import com.agrosense.app.local.LocalAuthManager
import com.agrosense.app.navigation.Route
import com.agrosense.app.ui.viewmodel.ParcelaViewModel
import com.agrosense.app.ui.viewmodel.agroSenseViewModelFactory

/** Dashboard: lista de parcelas leída de Room y alta de parcelas nuevas. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val parcelaViewModel: ParcelaViewModel = viewModel(factory = agroSenseViewModelFactory(context))
    val authManager = remember(context) { LocalAuthManager(context) }

    val parcelas by parcelaViewModel.parcelas.collectAsStateWithLifecycle()
    val userName = authManager.getUserName() ?: "Usuario"

    var mostrarDialogo by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Parcelas") },
                actions = {
                    IconButton(onClick = { navController.navigate(Route.Perfil.route) }) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogo = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nueva parcela")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Hola, $userName", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            if (parcelas.isEmpty()) {
                Text(
                    "No tienes parcelas registradas. Crea una con el botón +",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(parcelas, key = { it.id }) { parcela ->
                        TarjetaParcela(
                            parcela = parcela,
                            onDiagnosticar = {
                                navController.navigate(Route.CapturaDiagnostico.create(parcela.id))
                            },
                            onHistorial = {
                                navController.navigate(Route.HistorialDiagnosticos.create(parcela.id))
                            },
                            onActividades = {
                                navController.navigate(Route.RegistroActividades.create(parcela.id))
                            }
                        )
                    }
                }
            }
        }
    }

    if (mostrarDialogo) {
        DialogoNuevaParcela(
            onDismiss = { mostrarDialogo = false },
            onGuardar = { nombre, ubicacion, cultivo ->
                parcelaViewModel.registrarParcela(nombre, ubicacion, cultivo)
                mostrarDialogo = false
            }
        )
    }
}

@Composable
private fun TarjetaParcela(
    parcela: Parcela,
    onDiagnosticar: () -> Unit,
    onHistorial: () -> Unit,
    onActividades: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onDiagnosticar),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(parcela.nombre, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text("Cultivo: ${parcela.cultivo}", style = MaterialTheme.typography.bodyMedium)
            Text(
                "Ubicación: ${parcela.ubicacion}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onDiagnosticar) { Text("Diagnosticar") }
                TextButton(onClick = onHistorial) { Text("Historial") }
                TextButton(onClick = onActividades) { Text("Actividades") }
            }
        }
    }
}

@Composable
private fun DialogoNuevaParcela(
    onDismiss: () -> Unit,
    onGuardar: (nombre: String, ubicacion: String, cultivo: String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var cultivo by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva parcela") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = { ubicacion = it },
                    label = { Text("Ubicación") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = cultivo,
                    onValueChange = { cultivo = it },
                    label = { Text("Cultivo") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onGuardar(nombre.trim(), ubicacion.trim(), cultivo.trim()) },
                enabled = nombre.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
