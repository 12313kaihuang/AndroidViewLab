package com.yu.hu.viewlab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yu.hu.viewlab.navigation.ExpandableTextView
import com.yu.hu.viewlab.navigation.ProgressButton
import com.yu.hu.viewlab.navigation.RecordAnalysisCard
import com.yu.hu.viewlab.ui.theme.AndroidViewLabTheme

/**
 * huyu create
 * 2026/4/2 14:20
 */
@Composable
fun MainScreen(
    onNavigate: (Any) -> Unit
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(8.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionBtn(
                text = "RecordAnalysisCard",
                onClick = { onNavigate(RecordAnalysisCard) }
            )
            ActionBtn(
                text = "ExpandableTextView",
                onClick = { onNavigate(ExpandableTextView) }
            )
            ActionBtn(
                text = "ProgressButton",
                onClick = { onNavigate(ProgressButton) }
            )
        }
    }
}

@Composable
private fun ActionBtn(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Text(text)
    }
}

@Preview
@Composable
private fun MainScreenPreview() {
    AndroidViewLabTheme {
        MainScreen { }
    }
}