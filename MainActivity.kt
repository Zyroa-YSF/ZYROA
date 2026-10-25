package com.skingold.market

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = C.gold, onPrimary = Color.Black,
                    background = C.bg, onBackground = Color.White,
                    surface = C.surface, onSurface = Color.White,
                    surfaceVariant = C.card, onSurfaceVariant = C.silver,
                    secondary = C.blue, tertiary = C.purple
                )
            ) {
                val vm: GameVM = viewModel()
                AppRoot(vm)
            }
        }
    }
}
