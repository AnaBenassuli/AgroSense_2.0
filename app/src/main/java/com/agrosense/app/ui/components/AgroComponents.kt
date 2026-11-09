package com.agrosense.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.agrosense.app.R
import com.agrosense.app.ui.theme.AgroBackground
import com.agrosense.app.ui.theme.AgroBorder
import com.agrosense.app.ui.theme.AgroError
import com.agrosense.app.ui.theme.AgroErrorPressed
import com.agrosense.app.ui.theme.AgroErrorSoft
import com.agrosense.app.ui.theme.AgroGreen
import com.agrosense.app.ui.theme.AgroGreenDark
import com.agrosense.app.ui.theme.AgroGreenPressed
import com.agrosense.app.ui.theme.AgroGreenSoft
import com.agrosense.app.ui.theme.AgroInfo
import com.agrosense.app.ui.theme.AgroInfoSoft
import com.agrosense.app.ui.theme.AgroSuccess
import com.agrosense.app.ui.theme.AgroSuccessSoft
import com.agrosense.app.ui.theme.AgroSurface
import com.agrosense.app.ui.theme.AgroSurfaceAlt
import com.agrosense.app.ui.theme.AgroTextPrimary
import com.agrosense.app.ui.theme.AgroTextSecondary
import com.agrosense.app.ui.theme.AgroTextTertiary
import com.agrosense.app.ui.theme.AgroWarning
import com.agrosense.app.ui.theme.AgroWarningSoft

/*
 * Piezas reutilizables del sistema visual (mismas medidas que la guía de ToolFinder):
 * botón 54 dp / radio 14, campo 52 dp / radio 14, tarjeta radio 16, pastilla radio 12.
 * Las pantallas solo combinan estas piezas, así se ven como una sola aplicación.
 */

private val ButtonShape = RoundedCornerShape(14.dp)
private val FieldShape = RoundedCornerShape(14.dp)
private val CardShape = RoundedCornerShape(16.dp)
private val PillShape = RoundedCornerShape(12.dp)

// =====================================================
// TONOS DE ESTADO (color con significado, no decorativo)
// =====================================================

enum class AgroTone(
    val background: Color,
    val content: Color
) {
    NEUTRAL(AgroSurfaceAlt, AgroTextSecondary),
    ACCENT(AgroGreenSoft, AgroGreenDark),
    SUCCESS(AgroSuccessSoft, AgroSuccess),
    WARNING(AgroWarningSoft, AgroWarning),
    INFO(AgroInfoSoft, AgroInfo),
    ERROR(AgroErrorSoft, AgroError)
}

// =====================================================
// ESTRUCTURA DE PANTALLA
// =====================================================

/**
 * Contenedor base de cada pantalla: fondo papel, márgenes de 20 dp y,
 * si se pasa [bottomBar], una barra fija abajo para la acción principal.
 */
@Composable
fun AgroScreen(
    modifier: Modifier = Modifier,
    bottomBar: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AgroBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            content = content
        )

        if (bottomBar != null) {
            HorizontalDivider(
                thickness = 1.dp,
                color = AgroBorder
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AgroBackground)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                bottomBar()
            }
        }
    }
}

/** Barra superior: botón de regreso en recuadro acero + título de 17 sp. */
@Composable
fun AgroTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AgroSurfaceAlt)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_left),
                    contentDescription = "Volver",
                    tint = AgroTextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = AgroTextPrimary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        trailing?.invoke(this)
    }
}

/** Título grande de pantalla (26 sp, 700) con descripción opcional. */
@Composable
fun AgroScreenTitle(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = AgroTextPrimary
        )

        if (description != null) {
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = AgroTextSecondary
            )
        }
    }
}

/** Encabezado de sección con dato opcional a la derecha (ej. "3 registros"). */
@Composable
fun AgroSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: String? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = AgroTextPrimary,
            modifier = Modifier.weight(1f)
        )

        if (trailing != null) {
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelMedium,
                color = AgroTextTertiary
            )
        }
    }
}

/** Rótulo corto en mayúsculas (la única excepción al formato de oración). */
@Composable
fun AgroOverline(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AgroTextSecondary
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        letterSpacing = 0.6.sp,
        color = color,
        modifier = modifier
    )
}

// =====================================================
// BOTONES
// =====================================================

/**
 * Botón principal: 54 dp, radio 14, fondo verde y texto blanco 15 sp/600.
 * Al presionarse pasa al verde oscuro. Uno solo por pantalla.
 */
@Composable
fun AgroPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @DrawableRes icon: Int? = null,
    height: Dp = 54.dp,
    destructive: Boolean = false
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val base = if (destructive) AgroError else AgroGreen
    val pressedColor = if (destructive) AgroErrorPressed else AgroGreenPressed

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(height),
        shape = ButtonShape,
        interactionSource = interaction,
        elevation = null,
        contentPadding = PaddingValues(horizontal = 16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (pressed) pressedColor else base,
            contentColor = Color.White,
            disabledContainerColor = AgroBorder,
            disabledContentColor = AgroTextTertiary
        )
    ) {
        ButtonContent(text = text, icon = icon)
    }
}

/** Botón secundario: mismas medidas, fondo blanco, borde fino y texto en tinta media. */
@Composable
fun AgroSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @DrawableRes icon: Int? = null,
    height: Dp = 54.dp
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(height),
        shape = ButtonShape,
        border = BorderStroke(1.dp, AgroBorder),
        contentPadding = PaddingValues(horizontal = 16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = AgroSurface,
            contentColor = AgroTextSecondary,
            disabledContainerColor = AgroSurfaceAlt,
            disabledContentColor = AgroTextTertiary
        )
    ) {
        ButtonContent(text = text, icon = icon)
    }
}

/**
 * Botón de acción de apoyo (ej. "Analizar con IA"): fondo verde tenue y texto verde oscuro.
 * Destaca más que el secundario sin competir con el botón principal.
 */
@Composable
fun AgroTonalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @DrawableRes icon: Int? = null,
    height: Dp = 48.dp
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(height),
        shape = ButtonShape,
        elevation = null,
        contentPadding = PaddingValues(horizontal = 16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AgroGreenSoft,
            contentColor = AgroGreenDark,
            disabledContainerColor = AgroSurfaceAlt,
            disabledContentColor = AgroTextTertiary
        )
    ) {
        ButtonContent(text = text, icon = icon)
    }
}

/** Barra horizontal de proporción (0f a 1f), útil para porcentajes del modelo. */
@Composable
fun AgroProgressBar(
    fraction: Float,
    modifier: Modifier = Modifier,
    color: Color = AgroGreen
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(50))
            .background(AgroSurfaceAlt)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
    }
}

@Composable
private fun ButtonContent(
    text: String,
    @DrawableRes icon: Int?
) {
    if (icon != null) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))
    }

    Text(
        text = text,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

/** Acción pequeña dentro de tarjetas (Editar / Eliminar). */
@Composable
fun AgroChipButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    tone: AgroTone = AgroTone.NEUTRAL
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(tone.background)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = tone.content,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))
        }

        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = tone.content
        )
    }
}

/** Fila estándar de acciones de una tarjeta: Editar (neutro) y Eliminar (rojo). */
@Composable
fun AgroCardActions(
    onEditClick: (() -> Unit)?,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
    ) {
        if (onEditClick != null) {
            AgroChipButton(
                text = "Editar",
                icon = R.drawable.ic_edit,
                onClick = onEditClick
            )
        }

        AgroChipButton(
            text = "Eliminar",
            icon = R.drawable.ic_delete,
            tone = AgroTone.ERROR,
            onClick = onDeleteClick
        )
    }
}

// =====================================================
// CAMPOS
// =====================================================

/**
 * Campo de texto: 52 dp, borde color línea y radio 14. Con foco, el borde pasa
 * a verde con un halo suave. Rótulo arriba del campo, como en la guía.
 */
@Composable
fun AgroTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    @DrawableRes leadingIcon: Int? = null,
    isPassword: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()

    Column(modifier = modifier) {

        if (label != null) {
            FieldLabel(label)
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = AgroTextPrimary),
            cursorBrush = SolidColor(AgroGreen),
            visualTransformation =
                if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else keyboardType
            ),
            interactionSource = interaction,
            modifier = Modifier
                .fillMaxWidth()
                .focusHalo(focused)
                .clip(FieldShape)
                .background(if (enabled) AgroSurface else AgroSurfaceAlt)
                .border(1.dp, if (focused) AgroGreen else AgroBorder, FieldShape),
            decorationBox = { innerTextField ->
                Row(
                    modifier =
                        if (singleLine) {
                            Modifier
                                .heightIn(min = 52.dp)
                                .padding(horizontal = 14.dp)
                        } else {
                            Modifier.padding(14.dp)
                        },
                    verticalAlignment =
                        if (singleLine) Alignment.CenterVertically else Alignment.Top
                ) {
                    if (leadingIcon != null) {
                        Icon(
                            painter = painterResource(leadingIcon),
                            contentDescription = null,
                            tint = if (focused) AgroGreen else AgroTextTertiary,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty() && placeholder != null) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = AgroTextTertiary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        innerTextField()
                    }
                }
            }
        )
    }
}

/**
 * Selector desplegable con el mismo aspecto que un campo de texto.
 * Sustituye a los OutlinedButton + DropdownMenu de antes, sin cambiar la lógica.
 */
@Composable
fun <T> AgroSelectField(
    valueText: String,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    @DrawableRes leadingIcon: Int? = null,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {

        if (label != null) {
            FieldLabel(label)
        }

        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusHalo(expanded)
                    .heightIn(min = 52.dp)
                    .clip(FieldShape)
                    .background(if (enabled) AgroSurface else AgroSurfaceAlt)
                    .border(1.dp, if (expanded) AgroGreen else AgroBorder, FieldShape)
                    .clickable(enabled = enabled) { expanded = true }
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    Icon(
                        painter = painterResource(leadingIcon),
                        contentDescription = null,
                        tint = if (expanded) AgroGreen else AgroTextTertiary,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))
                }

                Text(
                    text = valueText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) AgroTextPrimary else AgroTextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    painter = painterResource(R.drawable.ic_expand_more),
                    contentDescription = null,
                    tint = AgroTextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                shape = RoundedCornerShape(14.dp),
                containerColor = AgroSurface
            ) {
                options.forEach { opcion ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = optionLabel(opcion),
                                style = MaterialTheme.typography.bodyMedium,
                                color = AgroTextPrimary
                            )
                        },
                        onClick = {
                            onSelect(opcion)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/** Selector de dos o más opciones sobre fondo acero (ej. Cultivo | Ganado). */
@Composable
fun <T> AgroSegmented(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(ButtonShape)
            .background(AgroSurfaceAlt)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { (valor, etiqueta) ->

            val activo = valor == selected
            val forma = RoundedCornerShape(11.dp)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(forma)
                    .background(if (activo) AgroSurface else Color.Transparent)
                    .then(
                        if (activo) Modifier.border(1.dp, AgroBorder, forma) else Modifier
                    )
                    .clickable { onSelect(valor) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = etiqueta,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (activo) AgroGreenDark else AgroTextSecondary
                )
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = AgroTextSecondary,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

/** Halo suave alrededor del campo con foco (se dibuja por fuera, sin mover el diseño). */
private fun Modifier.focusHalo(visible: Boolean): Modifier =
    drawBehind {
        if (visible) {
            val extra = 3.dp.toPx()
            drawRoundRect(
                color = AgroGreen.copy(alpha = 0.14f),
                topLeft = Offset(-extra, -extra),
                size = Size(size.width + extra * 2, size.height + extra * 2),
                cornerRadius = CornerRadius(17.dp.toPx())
            )
        }
    }

// =====================================================
// TARJETAS Y DATOS
// =====================================================

/** Tarjeta: fondo blanco, borde fino, radio 16 y espacio interno de 16. */
@Composable
fun AgroCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = AgroSurface,
    borderColor: Color? = AgroBorder,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(CardShape)
            .background(containerColor)
            .then(
                if (borderColor != null) Modifier.border(1.dp, borderColor, CardShape) else Modifier
            )
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
            .padding(contentPadding),
        content = content
    )
}

/** Tarjeta de dato (como Precio / Existencias): rótulo pequeño y cifra grande. */
@Composable
fun AgroStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    tone: AgroTone = AgroTone.NEUTRAL,
    valueColor: Color = if (tone == AgroTone.NEUTRAL) AgroTextPrimary else tone.content
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(tone.background)
            .padding(14.dp)
    ) {
        AgroOverline(text = label, color = tone.content)

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Pastilla de dato: fondo acero, rótulo pequeño arriba y valor debajo. */
@Composable
fun AgroDataPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(PillShape)
            .background(AgroSurfaceAlt)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        AgroOverline(text = label, color = AgroTextTertiary)

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = AgroTextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Etiqueta de estado tipo "Disponible" / "En línea". */
@Composable
fun AgroStatusChip(
    text: String,
    modifier: Modifier = Modifier,
    tone: AgroTone = AgroTone.SUCCESS,
    showDot: Boolean = false
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(tone.background)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showDot) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(tone.content)
            )

            Spacer(modifier = Modifier.width(6.dp))
        }

        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = tone.content
        )
    }
}

/** Icono sobre fondo verde tenue (equivalente al "naranja tenue" de la guía). */
@Composable
fun AgroIconBadge(
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp,
    tone: AgroTone = AgroTone.ACCENT,
    tint: Color = if (tone == AgroTone.ACCENT) AgroGreen else tone.content
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.3f))
            .background(tone.background),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

/** Aviso en línea: fondo tenue + texto en la versión oscura del mismo color. */
@Composable
fun AgroBanner(
    text: String,
    modifier: Modifier = Modifier,
    tone: AgroTone = AgroTone.INFO,
    @DrawableRes icon: Int = R.drawable.ic_info
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(PillShape)
            .background(tone.background)
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = tone.content,
            modifier = Modifier
                .padding(top = 1.dp)
                .size(16.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = tone.content
        )
    }
}

/** Estado vacío centrado con icono, título y descripción. */
@Composable
fun AgroEmptyState(
    @DrawableRes icon: Int,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    AgroCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 28.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AgroIconBadge(icon = icon, size = 52.dp, iconSize = 26.dp)

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = AgroTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = AgroTextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

// =====================================================
// DIÁLOGOS
// =====================================================

/**
 * Diálogo con el estilo de la app: fondo papel, radio 20 y botones
 * Cancelar (secundario) + acción (principal) del mismo ancho.
 */
@Composable
fun AgroDialog(
    title: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissText: String = "Cancelar",
    confirmEnabled: Boolean = true,
    destructive: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(AgroBackground)
                .padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = AgroTextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                content = content
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AgroSecondaryButton(
                    text = dismissText,
                    onClick = onDismiss,
                    height = 48.dp,
                    modifier = Modifier.weight(1f)
                )

                AgroPrimaryButton(
                    text = confirmText,
                    onClick = onConfirm,
                    enabled = confirmEnabled,
                    destructive = destructive,
                    height = 48.dp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun FieldSpacer() {
    Spacer(modifier = Modifier.height(14.dp))
}