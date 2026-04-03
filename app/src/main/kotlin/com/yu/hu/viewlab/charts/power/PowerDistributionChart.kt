package com.yu.hu.viewlab.charts.power

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
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
    direction: String?,
    powerData: PowerDistribution,
    modifier: Modifier = Modifier,
    style: PowerDistributionChartStyle = PowerDistributionChartDefaults.style()
) {
    val strokeWidth = style.strokeWidth
    val measurer = rememberTextMeasurer()
    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val peakStrokeWidth = strokeWidth + 10.dp.toPx()
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
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
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
        val startAngleResult = measurer.measure("TDC", style.textStyle)
        val offsetY = centerY - innerRadius + 10.dp.toPx()
        drawText(
            textLayoutResult = startAngleResult,
            topLeft = Offset(
                x = centerX - startAngleResult.size.width / 2,
                y = offsetY
            )
        )
        val endAngleResult = measurer.measure("BDC", style.textStyle)
        drawText(
            textLayoutResult = endAngleResult,
            topLeft = Offset(
                x = centerX - startAngleResult.size.width / 2,
                y = size.height - offsetY - endAngleResult.size.height
            )
        )

        // 方向文案
        if (direction != null) {
            val layoutResult = measurer.measure(direction, style.textStyle)
            drawText(
                textLayoutResult = layoutResult,
                topLeft = Offset(
                    x = centerX - layoutResult.size.width / 2,
                    y = centerY - layoutResult.size.height / 2
                )
            )
        }
    }
}

data class PowerDistributionChartStyle(
    val strokeWidth: Float,
    val bgColor: Color,
    val arcColor: Color,
    val peakArcColor: Color,
    val textStyle: TextStyle
)

object PowerDistributionChartDefaults {

    fun style(
        strokeWidth: Float = 40f,
        bgColor: Color = Color.Gray,
        arcColor: Color = Color.Green,
        peakArcColor: Color = Color.Black,
        textStyle: TextStyle = TextStyle(
            color = Color.Black,
            fontSize = 15.sp
        )
    ) = PowerDistributionChartStyle(
        strokeWidth = strokeWidth,
        bgColor = bgColor,
        arcColor = arcColor,
        peakArcColor = peakArcColor,
        textStyle = textStyle
    )
}

@Preview
@Composable
private fun PowerDistributionChartPreview() {
    PowerDistributionChart(
        direction = "左",
        powerData = PowerDistribution(20f, 165f, 45f, 98f),
        modifier = Modifier
            .background(Color.White)
            .size(200.dp)
    )
}