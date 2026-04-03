package com.yu.hu.viewlab.charts.power

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.rememberLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * huyu create
 * 2026/4/2 16:32
 */
@Composable
fun PowerDataScreen() {
    Scaffold { innerPadding ->
        val viewModel: PowerDataViewModel = viewModel()
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            var powerData by remember { mutableStateOf(PowerDistribution()) }
            val lifecycleOwner = rememberLifecycleOwner()
            LaunchedEffect(lifecycleOwner) {
                lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    while (true) {
                        withFrameNanos {
                            powerData = viewModel.dataBuffer.average()
                        }
                    }
                }
            }

            Text(text = "${powerData.startAngle} ${powerData.endAngle} size:${viewModel.dataBuffer.size}")

            PowerDistributionChart(direction = "左", powerData, modifier = Modifier.size(200.dp))
        }
    }
}