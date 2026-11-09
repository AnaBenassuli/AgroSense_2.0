package com.agrosense.app.ui.components

import androidx.compose.foundation.BorderStroke
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
import com.agrosense.app.ui.theme.AgroGreenDark
import com.agrosense.app.ui.theme.AgroGreenPressed
import com.agrosense.app.ui.theme.AgroSuccess
import com.agrosense.app.ui.theme.AgroTextSecondary

/**
 * Mismo tono de verde que en la ventana de ayuda "Tipos de usuario".
 * Los tres pasan el contraste mínimo con texto blanco (4.5:1).
 */
fun colorTipoUsuario(tipo: UserType): Color =
    when (tipo) {
        UserType.PRODUCER -> AgroGreenPressed
        UserType.AGRONOMIST -> AgroSuccess
        UserType.COOPERATIVE -> AgroGreenDark
    }

/**
 * Una opción de tipo de usuario con su explicación. Se usa en el registro y en Mi perfil.
 * - Elegida: fondo de su verde y letra blanca.
 * - No elegida: fondo muy claro de su verde y letra oscura, para que se lea bien
 *   (antes era el mismo verde transparente con letra blanca y casi no se leía).
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
                    color.copy(alpha = 0.06f)
                }
        ),
        border =
            if (seleccionado) {
                null
            } else {
                BorderStroke(1.dp, color.copy(alpha = 0.35f))
            },
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
                    unselectedColor = color
                )
            )

            Column(
                modifier = Modifier.padding(start = 4.dp)
            ) {

                Text(
                    text = tipo.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (seleccionado) Color.White else color
                )

                Text(
                    text = tipo.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (seleccionado) Color.White else AgroTextSecondary
                )
            }
        }
    }
}