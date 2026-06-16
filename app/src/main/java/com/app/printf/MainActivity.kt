package com.app.printf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.app.printf.ui.navigation.PrintfNavGraph
import com.app.printf.ui.theme.PrintfTheme
import com.app.printf.ui.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        // Let the splash hide once the activity/compose content is ready.
        splashScreen.setKeepOnScreenCondition { false }
        val app = application as PrintfApplication
        val factory = ViewModelFactory(app.container)
        enableEdgeToEdge()
        setContent {
            PrintfTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    PrintfNavGraph(factory = factory)
                }
            }
        }
    }
}
