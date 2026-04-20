package com.yu.hu.viewlab.charts.power

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * huyu create
 * 2026/4/3 10:27
 */

/**
 * 功率分布环形图
 */
@Composable
fun PowerDistributionChart(
    powerData: PowerDistribution,
    modifier: Modifier = Modifier,
    style: PowerDistributionChartStyle = PowerDistributionChartDefaults.style()
) {
    val bgStyle = remember(style.bgColor) { TextStyle(color = style.bgColor, fontSize = 10.sp) }
    val percentStyle =
        remember(style.bgColor) { TextStyle(color = style.bgColor, fontSize = 14.sp) }
    val measurer = rememberTextMeasurer()
    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val strokeWidth = style.strokeWidth.toPx()
        val peakStrokeWidth = style.peakStrokeWidth.toPx()
        // 内切圆半径
        val outerRadius = size.minDimension / 2
        val innerRadius = outerRadius - peakStrokeWidth

        // 背景
        val normalRadius = innerRadius + strokeWidth / 2
        val arcSize = Size(
            width = normalRadius * 2,
            height = normalRadius * 2
        )
        val arcOffset = Offset(centerX - normalRadius, centerY - normalRadius)
        drawArc(
            color = style.bgColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth),
            size = arcSize,
            topLeft = arcOffset
        )

        // 功率分布弧
        drawArc(
            color = style.arcColor,
            //0°在右侧
            startAngle = powerData.startAngle - 90f,
            sweepAngle = powerData.endAngle - powerData.startAngle,
            useCenter = false,
            style = Stroke(width = strokeWidth),
            size = arcSize,
            topLeft = arcOffset
        )

        // 峰值功率分布 弧
        val peakRadius = innerRadius + peakStrokeWidth / 2
        drawArc(
            color = style.peakArcColor,
            startAngle = powerData.peakStartAngle - 90f,
            sweepAngle = powerData.peakEndAngle - powerData.peakStartAngle,
            useCenter = false,
            style = Stroke(width = peakStrokeWidth),
            size = Size(
                width = peakRadius * 2,
                height = peakRadius * 2
            ),
            topLeft = Offset(
                x = centerX - peakRadius,
                y = centerY - peakRadius
            )
        )

        // 小圆点
        for (angle in 0..360 step 45) {
            val dotRadius = if (angle % 90 == 0) 3.dp.toPx() else 2.dp.toPx()
            // 圆点中心半径 距离内圈5dp
            val dotArcRadius = innerRadius - 5.dp.toPx() - dotRadius
            val rad = Math.toRadians(angle.toDouble())
            val x = centerX + dotArcRadius * kotlin.math.cos(rad)
            val y = centerY + dotArcRadius * kotlin.math.sin(rad)
            drawCircle(
                color = style.bgColor,
                radius = dotRadius,
                center = Offset(x.toFloat(), y.toFloat())
            )
        }

        //起止角度文案
        val startAngleResult = measurer.measure("TDC", bgStyle)
        val offsetY = centerY - innerRadius + 12.dp.toPx()
        drawText(
            textLayoutResult = startAngleResult,
            topLeft = Offset(
                x = centerX - startAngleResult.size.width / 2,
                y = offsetY
            )
        )
        val endAngleResult = measurer.measure("BDC", bgStyle)
        drawText(
            textLayoutResult = endAngleResult,
            topLeft = Offset(
                x = centerX - startAngleResult.size.width / 2,
                y = size.height - offsetY - endAngleResult.size.height
            )
        )

        // 百分比文案
        val layoutResult = measurer.measure("46", style.textStyle)
        drawText(
            textLayoutResult = layoutResult,
            topLeft = Offset(
                x = centerX - layoutResult.size.width / 2,
                y = centerY - layoutResult.size.height / 2
            )
        )
        val percentResult = measurer.measure("%", percentStyle)
        drawText(
            textLayoutResult = percentResult,
            topLeft = Offset(
                x = centerX + layoutResult.size.width / 2 + 2.dp.toPx(),
                y = centerY - layoutResult.size.height / 2
            )
        )

    }
}

data class PowerDistributionChartStyle(
    val strokeWidth: Dp,
    val peakStrokeWidth: Dp,
    val bgColor: Color,
    val arcColor: Color,
    val peakArcColor: Color,
    val textStyle: TextStyle
)

object PowerDistributionChartDefaults {

    fun style(
        strokeWidth: Dp = 10.dp,
        peakStrokeWidth: Dp = 14.dp,
        bgColor: Color = Color(0x33FFFFFF),
        arcColor: Color = Color(0xFF2FE6E9),
//        peakArcColor: Color = Color(0xFF2FE6E9),
        textStyle: TextStyle = TextStyle(
            color = Color.White,
            fontSize = 24.sp
        )
    ) = PowerDistributionChartStyle(
        strokeWidth = strokeWidth,
        peakStrokeWidth = peakStrokeWidth,
        bgColor = bgColor,
        arcColor = arcColor.copy(alpha = 0.5f),
        peakArcColor = arcColor,
        textStyle = textStyle
    )
}

@Preview
@Composable
private fun PowerDistributionChartPreview() {
    PowerDistributionChart(
        powerData = PowerDistribution(20f, 165f, 45f, 98f),
        modifier = Modifier
            .background(Color.Black)
            .size(200.dp)
    )
}