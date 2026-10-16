package com.ganapujaday

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.ganapujaday.ui.DayScreen
import com.ganapujaday.ui.GanapujaDayTheme
import com.ganapujaday.ui.ThemePreference

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val themePreference = ThemePreference(applicationContext)

        setContent {
            GanapujaDayTheme(themePreference) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DayScreen()
                }
            }
        }
    }
}