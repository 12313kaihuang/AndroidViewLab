package com.yu.hu.viewlab

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yu.hu.viewlab.charts.analysis.RecordAnalysisScreen
import com.yu.hu.viewlab.navigation.Home
import com.yu.hu.viewlab.navigation.RecordAnalysisCard

/**
 * huyu create
 * 2026/4/2 14:20
 */

@Composable
fun AppNavHost() {
    val controller = rememberNavController()
    NavHost(
        navController = controller,
        startDestination = Home
    ) {
        composable<Home> {
            MainScreen(
                onNavigate = controller::navigate
            )
        }
        composable<RecordAnalysisCard> {
            RecordAnalysisScreen()
        }
    }
}