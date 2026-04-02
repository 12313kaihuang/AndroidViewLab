package com.yu.hu.viewlab.charts.analysis

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yu.hu.viewlab.ui.theme.AndroidViewLabTheme

/**
 * huyu create
 * 2026/3/27 10:21
 */
@Composable
fun RecordAnalysisScreen() {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            Text(
                text = "骑行数据卡片：",
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val viewModel: RecordAnalysisViewModel = viewModel()
            AndroidView(
                factory = { context ->
                    RecordAnalysisCard(context).apply {
                        init(
                            viewModel.tabIndex,
                            viewModel.tabs,
                            viewModel::getTabController
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            )
        }
    }
}

@Preview
@Composable
private fun RecordAnalysisScreenPreview() {
    AndroidViewLabTheme {
        RecordAnalysisScreen()
    }
}