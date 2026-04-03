package com.yu.hu.viewlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.yu.hu.viewlab.charts.power.PowerDataScreen
import com.yu.hu.viewlab.ui.theme.AndroidViewLabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidViewLabTheme {
//                AppNavHost()
                PowerDataScreen()
            }
        }
    }
}