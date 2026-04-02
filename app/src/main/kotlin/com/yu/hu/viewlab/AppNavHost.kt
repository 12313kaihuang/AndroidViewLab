package com.yu.hu.viewlab

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yu.hu.viewlab.charts.analysis.RecordAnalysisScreen
import com.yu.hu.viewlab.components.ProgressButton.DisplayMode
import com.yu.hu.viewlab.navigation.ExpandableTextView
import com.yu.hu.viewlab.navigation.Home
import com.yu.hu.viewlab.navigation.ProgressButton
import com.yu.hu.viewlab.navigation.RecordAnalysisCard
import kotlin.math.roundToInt

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
        composable<ExpandableTextView> {
            InnerScaffold {
                AndroidView(
                    factory = { context ->
                        //todo compose下点击有点问题 可能跟绘制有关
                        com.yu.hu.viewlab.components.ExpandableTextView(context).apply {
                            isClickable = true
                            isFocusable = true
                            setExpandableText(
                                context.getString(R.string.expandable_text)
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .pointerInteropFilter {
                            false
                        }
                        .clickable {

                        }
                )
            }
        }
        composable<ProgressButton> {
            InnerScaffold {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var progress by remember { mutableStateOf(0.3f) }
                    Slider(
                        value = progress,
                        onValueChange = { progress = it },
                        modifier = Modifier
                    )
                    AndroidView(
                        factory = { context ->
                            com.yu.hu.viewlab.components.ProgressButton(context)
                        },
                        update = {
                            val value = (progress * 100).roundToInt()
                            it.setBtnText(
                                content = "$value%",
                                mode = DisplayMode.PROGRESS,
                                progress = value
                            )
                        },
                        modifier = Modifier.size(width = 80.dp, height = 35.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InnerScaffold(content: @Composable BoxScope.() -> Unit) {
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .padding(8.dp)
                .fillMaxSize()
                .pointerInteropFilter { false },
            content = content
        )
    }
}