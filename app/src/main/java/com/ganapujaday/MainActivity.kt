package com.ganapujaday

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ganapujaday.ui.DayScreen
import com.ganapujaday.ui.GanapujaDayTheme
import com.ganapujaday.ui.MonthScreen
import com.ganapujaday.ui.ThemePreference
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val themePreference = ThemePreference(applicationContext)

        setContent {
            GanapujaDayTheme(themePreference) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    // startDestination — сегодняшний день, передаём epochDay
                    val today = LocalDate.now().toEpochDay()

                    NavHost(
                        navController = navController,
                        startDestination = "day/$today",
                    ) {
                        composable(
                            route = "day/{epochDay}",
                            arguments = listOf(
                                navArgument("epochDay") { type = NavType.LongType }
                            ),
                        ) { backStackEntry ->
                            val epochDay = backStackEntry.arguments?.getLong("epochDay")
                                ?: LocalDate.now().toEpochDay()
                            val date = LocalDate.ofEpochDay(epochDay)

                            DayScreen(
                                date = date,
                                onOpenMonth = { navController.navigate("month") },
                            )
                        }

                        composable("month") {
                            MonthScreen(
                                onOpenDay = {
                                    // Возврат к сегодняшнему дню, стек не копим
                                    navController.navigate("day/${LocalDate.now().toEpochDay()}") {
                                        popUpTo("day/{epochDay}") { inclusive = true }
                                    }
                                },
                                onDayClick = { date ->
                                    // Клик по дню в месяце — открываем DayScreen для этой даты.
                                    // Без popUpTo: back вернёт в месяц.
                                    navController.navigate("day/${date.toEpochDay()}")
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}