package com.agrosense.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agrosense.app.ui.screen.auth.UserType
import com.agrosense.app.ui.theme.AgroGreen
import com.agrosense.app.ui.theme.AgroGreenDark
import com.agrosense.app.ui.theme.AgroSuccess

/** Mismo tono de verde que en la ventana de ayuda "Tipos de usuario". */
fun colorTipoUsuario(tipo: UserType): Color =
    when (tipo) {
        UserType.PRODUCER -> AgroGreen
        UserType.AGRONOMIST -> AgroSuccess
        UserType.COOPERATIVE -> AgroGreenDark
    }

/**
 * Una opción de tipo de usuario con su explicación. Se usa en el registro y en Mi perfil.
 * Cada tipo tiene su verde; la opción elegida se ve con el color completo y las demás más tenues.
 */
@Composable
fun OpcionTipoUsuario(
    tipo: UserType,
    seleccionado: Boolean,
    onClick: () -> Unit
) {

    val color = colorTipoUsuario(tipo)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                if (seleccionado) {
                    color
                } else {
                    color.copy(alpha = 0.55f)
                }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (seleccionado) 4.dp else 0.dp
        )
    ) {

        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            RadioButton(
                selected = seleccionado,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color.White,
                    unselectedColor = Color.White.copy(alpha = 0.8f)
                )
            )

            Column(
                modifier = Modifier.padding(start = 4.dp)
            ) {

                Text(
                    text = tipo.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = tipo.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}