package com.yu.hu.viewlab.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

/**
 * 踩踏中心偏移点状图
 *
 * huyu create
 * 2026/4/23 14:52
 */
@Composable
fun PedalCenterOffsetView(
    offsets: List<Int>,
    modifier: Modifier = Modifier,
    offsetRows: Int = 5,
    maxLines: Int = 16
) {
    val state by remember(offsets, offsetRows) {
        derivedStateOf { buildOffsetState(offsetRows, offsets) }
    }

    BoxWithConstraints(modifier) {
        val textMeasurer = rememberTextMeasurer()
        val density = LocalDensity.current
        val textStyle = TextStyle(fontSize = 16.sp, color = Color(0xE6FFFFFF))
        val positiveTextResult = textMeasurer.measure("+", textStyle)
        val negativeTextResult = textMeasurer.measure("−", textStyle)
        val positiveTextWidth = with(density) { positiveTextResult.size.width.toDp() }
        val positiveTextHeight = with(density) { positiveTextResult.size.height.toDp() }
        val negativeTextWidth = with(density) { negativeTextResult.size.width.toDp() }
        val rowCount = offsetRows * 2 + 1
        val (dotSize, dotSpacing, innerHorizontalPadding) = remember(
            rowCount,
            maxWidth,
            positiveTextWidth,
            negativeTextWidth
        ) {
            calcLayoutInfo(rowCount, maxWidth, positiveTextWidth, negativeTextWidth)
        }

        val maxColumnCount = state?.maxCount ?: 1
        val radio =
            if (maxColumnCount > maxLines) maxLines.toFloat() / maxColumnCount else 1f
        val finalColumn = maxColumnCount.coerceAtMost(maxLines)
        val dotHeight =
            (positiveTextHeight - dotSize) / 2 + dotSize * finalColumn + dotSpacing * (finalColumn - 1)
        // 最少留出绘制文字的高度
        val minDotHeight = dotHeight.coerceAtLeast(positiveTextHeight)

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(minDotHeight)
        ) {
            // +/-文案
            val leftResult = positiveTextResult
            drawText(
                textLayoutResult = leftResult,
                topLeft = Offset(
                    x = innerHorizontalPadding.toPx(),
                    y = 0f
                )
            )
            drawText(
                textLayoutResult = negativeTextResult,
                topLeft = Offset(
                    x = size.width - innerHorizontalPadding.toPx() - negativeTextResult.size.width,
                    y = 0f
                )
            )

            //点图
            if (dotSize <= 0.dp) return@Canvas
            val dotTopY = (leftResult.size.height - dotSize.toPx()) / 2
            val rectSize = Size(width = dotSize.toPx(), height = dotSize.toPx())
            val dotStartX = innerHorizontalPadding.toPx() * 2 + leftResult.size.width
            fun calcDotX(row: Int) = dotStartX + row * (dotSize + dotSpacing).toPx()
            fun calcDotY(column: Int) = dotTopY + column * (dotSize + dotSpacing).toPx()
            for (row in 0 until rowCount) {
                val x = calcDotX(row)
                val base = row - offsetRows
                val index = -base
                val columnCount = state?.offsets?.get(index)

                // 绘制背景格
                if (columnCount == null || columnCount == 0) {
                    drawRect(
                        color = Color(0xFF394242),
                        topLeft = Offset(x, dotTopY),
                        size = rectSize
                    )
                    continue
                }

                // 绘制进度格
                val radioedColumn = columnCount * radio
                val fullColumn = radioedColumn.toInt()
                repeat(fullColumn) { column ->
                    val y = calcDotY(column)
                    //进度格
                    drawRect(
                        color = Color(0xFF0CC1A6),
                        topLeft = Offset(x, y),
                        size = rectSize
                    )
                }

                val partial = radioedColumn - fullColumn
                if (partial > 0f) {
                    val y = calcDotY(fullColumn)
                    //背景格
                    drawRect(
                        color = Color(0xFF394242),
                        topLeft = Offset(x, y),
                        size = rectSize
                    )

                    //进度格
                    drawRect(
                        color = Color(0xFF0CC1A6),
                        topLeft = Offset(x, y),
                        size = rectSize.copy(height = rectSize.height * partial)
                    )
                }
            }
        }
    }
}

private data class LayoutInfo(
    val dotSize: Dp,
    val dotSpacing: Dp,
    val horizontalPadding: Dp
)

private fun calcLayoutInfo(
    rowCount: Int,
    maxWidth: Dp,
    positiveTextWidth: Dp,
    negativeTextWidth: Dp
): LayoutInfo {
    var dotSpacing = 3.dp //点横向间距
    var dotSize: Dp = 3.5.dp //点的大小
    var horizontalPadding = 12.dp //+/-号到两边和到dot padding
    val horizontalSpacingSize = 4 // 总共4个间隔
    val horizontalSpacing =
        maxWidth - positiveTextWidth - negativeTextWidth - dotSize * rowCount - dotSpacing * (rowCount - 1)
    val extraSpacing = horizontalSpacing - horizontalPadding * horizontalSpacingSize
    if (extraSpacing > 0.dp) {
        //多出来的平均加在dotSize上
        dotSize += (extraSpacing / rowCount)
    } else if (horizontalSpacing > 0.dp) {
        //缩减innerHorizontalPadding
        horizontalPadding = horizontalSpacing / 4
    } else {
        //放不下 修正dotSize
        horizontalPadding = 0.dp
        val size = (maxWidth - positiveTextWidth - negativeTextWidth) / (rowCount * 2 - 1)
        dotSpacing = size
        dotSize = size
    }
    return LayoutInfo(dotSize, dotSpacing, horizontalPadding)
}

private data class OffsetsState(
    val avgOffset: Float, //平均偏移
    val step: Int, //步进
    val maxCount: Int, //最大个数
    val offsets: Map<Int, Int>
)

private fun buildOffsetState(
    offsetRows: Int,
    offsets: List<Int>,
    threshold: Int = 30
): OffsetsState? {
    if (offsets.isEmpty()) return null

    // 第一遍遍历计算步进和最大值
    var sumOffset = 0f
    var validCount = 0
    var maxAbsOffset = 0
    offsets.forEach { offset ->
        val absOffset = abs(offset)
        // 超过阈值丢弃
        if (absOffset <= threshold) {
            validCount++
            maxAbsOffset = maxAbsOffset.coerceAtLeast(absOffset)
            sumOffset += offset
        }
    }
    val n = maxAbsOffset / offsetRows
    val step = if (maxAbsOffset % offsetRows == 0) n else n + 1

    // 第二次遍历构建map
    val offsetMap = mutableMapOf<Int, Int>()
    var maxCount = 0
    offsets.forEach { offset ->
        //假设step为2 那么1，2都要归到index=1里面去
        val base = (abs(offset) - 1) / step + 1
        val index = if (offset > 0) base else -base
        offsetMap[index] = offsetMap.getOrDefault(index, 0) + 1
        maxCount = maxCount.coerceAtLeast(offsetMap[index]!!)
    }
    return OffsetsState(
        avgOffset = sumOffset / validCount,
        step = step,
        maxCount = maxCount,
        offsets = offsetMap
    )
}

@Preview
@Composable
private fun PedalCenterOffsetViewPreview() {
    PedalCenterOffsetView(
        offsets = listOf(
//            0, 0, 0, 0,
            1, 2, 3, 4,
            -1, -2, 3, 3,
            3, 2
        ),
        modifier = Modifier
            .padding(8.dp)
            .width(180.dp)
    )
}