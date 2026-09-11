package com.agrosense.app.ui.screen.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.agrosense.app.ui.components.AgroSenseLogo

private enum class AccessStage {
    Welcome,
    Access
}

enum class UserType(
    val title: String,
    val description: String
) {
    Producer(
        "Productor",
        "Gestiona tus parcelas y diagnósticos."
    ),
    Agronomist(
        "Agrónomo",
        "Da seguimiento técnico a productores."
    ),
    Cooperative(
        "Cooperativa",
        "Coordina los cultivos de tu comunidad."
    )
}

data class UserAccount(
    val type: UserType,
    val name: String,
    val email: String,
    val specialty: String = "",
    val locality: String = ""
)

@Composable
fun AuthGate(
    onAuthenticated: (UserAccount) -> Unit
) {
    var stage by remember {
        mutableStateOf(AccessStage.Welcome)
    }

    AnimatedContent(
        targetState = stage,
        transitionSpec = {
            (
                    slideInHorizontally(tween(450)) { it / 3 } +
                            fadeIn(tween(450))
                    ) togetherWith (
                    slideOutHorizontally(tween(300)) { -it / 5 } +
                            fadeOut(tween(250))
                    )
        },
        label = "welcome_to_access"
    ) { currentStage ->

        if (currentStage == AccessStage.Welcome) {
            WelcomeScreen {
                stage = AccessStage.Access
            }
        } else {
            AccessScreen(
                onAuthenticated = onAuthenticated
            )
        }
    }
}

@Composable
private fun WelcomeScreen(
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        AgroSenseLogo()

        Spacer(
            modifier = Modifier.height(40.dp)
        )

        Text(
            text = "Tu cultivo, siempre bajo observación",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Text(
            text = "Identifica señales de plagas y enfermedades, registra actividades y da seguimiento a cada parcela.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(36.dp)
        )

        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Comenzar")
        }
    }
}

@Composable
private fun AccessScreen(
    onAuthenticated: (UserAccount) -> Unit
) {
    var isRegistering by remember {
        mutableStateOf(false)
    }

    var userType by remember {
        mutableStateOf(UserType.Producer)
    }

    var name by remember {
        mutableStateOf("")
    }

    var specialty by remember {
        mutableStateOf("")
    }

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var submitted by remember {
        mutableStateOf(false)
    }

    val specialtyRequired =
        isRegistering &&
                userType == UserType.Agronomist

    val hasMissingFields =
        email.isBlank() ||
                password.isBlank() ||
                (isRegistering && name.isBlank()) ||
                (specialtyRequired && specialty.isBlank())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        AgroSenseLogo(
            compact = true
        )

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        Text(
            text = if (isRegistering) {
                "Crea tu cuenta"
            } else {
                "Bienvenido de nuevo"
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = if (isRegistering) {
                "Elige cómo usarás AgroSense."
            } else {
                "Inicia sesión para continuar."
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = "Tipo de usuario",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.fillMaxWidth()
        )

        UserType.entries.forEach { type ->
            UserTypeCard(
                type = type,
                selected = type == userType
            ) {
                userType = type
            }
        }

        if (isRegistering) {

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                },
                label = {
                    Text("Nombre completo")
                },
                isError =
                    submitted &&
                            name.isBlank(),
                supportingText = {
                    if (
                        submitted &&
                        name.isBlank()
                    ) {
                        Text(
                            "Este campo es obligatorio"
                        )
                    }
                },
                modifier =
                    Modifier.fillMaxWidth()
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            if (
                userType ==
                UserType.Agronomist
            ) {

                OutlinedTextField(
                    value = specialty,
                    onValueChange = {
                        specialty = it
                    },
                    label = {
                        Text("Especialidad")
                    },
                    placeholder = {
                        Text(
                            "Ej. Sanidad vegetal"
                        )
                    },
                    isError =
                        submitted &&
                                specialty.isBlank(),
                    supportingText = {
                        if (
                            submitted &&
                            specialty.isBlank()
                        ) {
                            Text(
                                "Este campo es obligatorio"
                            )
                        }
                    },
                    modifier =
                        Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )
            }
        }

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
            },
            label = {
                Text("Correo electrónico")
            },
            isError =
                submitted &&
                        email.isBlank(),
            supportingText = {
                if (
                    submitted &&
                    email.isBlank()
                ) {
                    Text(
                        "Este campo es obligatorio"
                    )
                }
            },
            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },
            label = {
                Text("Contraseña")
            },
            visualTransformation =
                PasswordVisualTransformation(),
            isError =
                submitted &&
                        password.isBlank(),
            supportingText = {
                if (
                    submitted &&
                    password.isBlank()
                ) {
                    Text(
                        "Este campo es obligatorio"
                    )
                }
            },
            modifier =
                Modifier.fillMaxWidth()
        )

        if (
            submitted &&
            hasMissingFields
        ) {
            Text(
                text =
                    "Completa todos los campos obligatorios para continuar.",
                color =
                    MaterialTheme
                        .colorScheme
                        .error,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
            )
        }

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )

        Button(
            onClick = {

                submitted = true

                if (!hasMissingFields) {

                    onAuthenticated(
                        UserAccount(
                            type = userType,
                            name = name.trim(),
                            email = email.trim(),
                            specialty =
                                specialty.trim()
                        )
                    )
                }
            },
            modifier =
                Modifier.fillMaxWidth()
        ) {
            Text(
                if (isRegistering) {
                    "Crear cuenta"
                } else {
                    "Iniciar sesión"
                }
            )
        }

        Spacer(
            modifier =
                Modifier.height(10.dp)
        )

        OutlinedButton(
            onClick = {
                isRegistering =
                    !isRegistering

                submitted = false
            },
            modifier =
                Modifier.fillMaxWidth()
        ) {
            Text(
                if (isRegistering) {
                    "Ya tengo una cuenta"
                } else {
                    "Crear una cuenta"
                }
            )
        }
    }
}

@Composable
private fun UserTypeCard(
    type: UserType,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (selected) {
                        MaterialTheme
                            .colorScheme
                            .primaryContainer
                    } else {
                        MaterialTheme
                            .colorScheme
                            .surfaceVariant
                    }
            )
    ) {

        androidx.compose.foundation.layout.Row(
            modifier =
                Modifier.padding(12.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            RadioButton(
                selected = selected,
                onClick = onClick
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text = type.title,
                    fontWeight =
                        FontWeight.SemiBold
                )

                Text(
                    text =
                        type.description,
                    style =
                        MaterialTheme
                            .typography
                            .bodySmall
                )
            }
        }
    }
}