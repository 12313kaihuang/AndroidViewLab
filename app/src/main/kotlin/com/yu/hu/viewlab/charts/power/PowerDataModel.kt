package com.yu.hu.viewlab.charts.power

import com.yu.hu.viewlab.model.DataBuffer

/**
 * huyu create
 * 2026/4/2 16:12
 */
interface TimeWindowBuffer : DataBuffer<PowerDistribution> {

    /**
     * 时间窗口大小 ms
     */
    val windowDurationsMs: Long

    /**
     * 时间窗口内平均值
     */
    fun average(): PowerDistribution
}

//功率分布
data class PowerDistribution(
    val startAngle: Float = 0f, //起始角度
    val endAngle: Float = 0f, //结束角度
    val peakStartAngle: Float = 0f, //峰值起始角度
    val peakEndAngle: Float = 0f //峰值结束角度
)