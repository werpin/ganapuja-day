package com.ganapujaday

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ganapujaday.ui.DayScreen
import com.ganapujaday.ui.GanapujaDayTheme
import com.ganapujaday.ui.MonthScreen
import com.ganapujaday.ui.ThemePreference

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val themePreference = ThemePreference(applicationContext)

        setContent {
            GanapujaDayTheme(themePreference) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    NavHost(navController, startDestination = "day") {
                        composable("day") {
                            DayScreen(
                                onOpenMonth = { navController.navigate("month") },
                            )
                        }
                        composable("month") {
                            MonthScreen(
                                onOpenDay = {
                                    navController.navigate("day") {
                                        popUpTo("day") { inclusive = true }
                                    }
                                },
                                onDayClick = { /* [DEBT] см. ниже */ },
                            )
                        }
                    }
                }
            }
        }
    }
}