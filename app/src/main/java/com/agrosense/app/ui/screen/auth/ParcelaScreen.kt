package com.agrosense.app.ui.screen

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agrosense.app.data.local.entity.Parcela
import com.agrosense.app.ui.viewmodel.ParcelaViewModel

@Composable
fun ParcelaScreen(
    viewModel: ParcelaViewModel,
    onBackClick: () -> Unit
) {

    val parcelas by viewModel.parcelas.collectAsState()

    var mostrarFormulario by remember {
        mutableStateOf(false)
    }

    var nombre by remember {
        mutableStateOf("")
    }

    var ubicacion by remember {
        mutableStateOf("")
    }

    var cultivo by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            TextButton(
                onClick = onBackClick
            ) {
                Text("←")
            }

            Text(
                text = "Mis parcelas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Text(
            text = "Administra tus cultivos y consulta su información.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Button(
            onClick = {
                mostrarFormulario = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "+ Agregar parcela",
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        if (parcelas.isEmpty()) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "Aún no tienes parcelas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        text = "Agrega tu primera parcela para comenzar a registrar diagnósticos y actividades.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

        } else {

            Text(
                text = "${parcelas.size} parcelas registradas",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(
                    items = parcelas,
                    key = { parcela ->
                        parcela.id
                    }
                ) { parcela ->

                    ParcelaCard(
                        parcela = parcela,
                        onDeleteClick = {
                            viewModel.eliminarParcela(parcela)
                        }
                    )
                }
            }
        }
    }

    if (mostrarFormulario) {

        AlertDialog(
            onDismissRequest = {
                mostrarFormulario = false
            },

            shape = RoundedCornerShape(18.dp),

            title = {
                Text(
                    text = "Nueva parcela",
                    fontWeight = FontWeight.Bold
                )
            },

            text = {

                Column {

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = {
                            nombre = it
                        },
                        label = {
                            Text("Nombre de la parcela")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    OutlinedTextField(
                        value = cultivo,
                        onValueChange = {
                            cultivo = it
                        },
                        label = {
                            Text("Cultivo")
                        },
                        placeholder = {
                            Text("Ej. Maíz")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    OutlinedTextField(
                        value = ubicacion,
                        onValueChange = {
                            ubicacion = it
                        },
                        label = {
                            Text("Ubicación")
                        },
                        placeholder = {
                            Text("Ej. Chihuahua")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },

            confirmButton = {

                Button(
                    onClick = {

                        if (
                            nombre.isNotBlank() &&
                            cultivo.isNotBlank() &&
                            ubicacion.isNotBlank()
                        ) {

                            viewModel.registrarParcela(
                                nombre = nombre.trim(),
                                ubicacion = ubicacion.trim(),
                                cultivo = cultivo.trim()
                            )

                            nombre = ""
                            cultivo = ""
                            ubicacion = ""

                            mostrarFormulario = false
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        mostrarFormulario = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ParcelaCard(
    parcela: Parcela,
    onDeleteClick: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = parcela.nombre,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = parcela.cultivo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {

                    Text(
                        text = "Activa",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 7.dp
                        )
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            Text(
                text = "Ubicación",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(3.dp)
            )

            Text(
                text = parcela.ubicacion,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {

                TextButton(
                    onClick = onDeleteClick
                ) {
                    Text(
                        text = "Eliminar",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}