package com.agrosense.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.agrosense.app.ui.navigation.AppNavigation
import com.agrosense.app.ui.theme.AgroSenseTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            AgroSenseTheme {
                AppNavigation()
            }
        }
    }
}