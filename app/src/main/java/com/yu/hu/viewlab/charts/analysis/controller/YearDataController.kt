package com.yu.hu.viewlab.charts.analysis.controller

import com.yu.hu.viewlab.charts.analysis.RidePageData
import com.yu.hu.viewlab.charts.analysis.RideSummaryData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import kotlin.random.Random

/**
 * huyu create
 * 2026/3/20 16:15
 */
class YearDataController(
    scope: CoroutineScope,
) : AbsChartController<Int>(scope) {
    override val slotCount: Int = 12
    override val slotBarWidth: Float = 14f
    override var pageCount: Int = 2
    override val tag: String = "YearDataController"
    override val isXAxisEdgeAligned: Boolean = false

    private val dateTime = LocalDateTime.now()

    override fun getPageKey(page: Int): Int {
        return dateTime.year - page
    }

    override fun emptySummaryData(page: Int): RideSummaryData {
        return RideSummaryData(getPageKey(page).toString())
    }

    override fun getPageCount(earliestRideTime: LocalDateTime): Int {
        return (dateTime.year - earliestRideTime.year + 1).coerceAtLeast(1)
    }

    override suspend fun loadPageData(page: Int): RidePageData {
        delay(500)
        val key = getPageKey(page)
        //mock data
        val random = Random(System.currentTimeMillis())
        var distance = 0f
        var timeSeconds = 0L
        var climb = 0f
        var times = 0
        val details = buildList {
            repeat(slotCount) { count ->
                val data = RideSummaryData(
                    label = "${key}/${count + 1}",
                    distance = random.nextFloat().coerceIn(0f, 200f),
                    timeSeconds = random.nextLong(0, 5 * 60 * 60),
                    climb = random.nextFloat().coerceIn(0f, 1000f),
                    times = random.nextInt(0, 20)
                )
                distance += data.distance
                timeSeconds += data.timeSeconds
                climb += data.climb
                times += data.times
                add(data)
            }
        }

        val summary = RideSummaryData(
            label = key.toString(),
            distance = distance,
            timeSeconds = timeSeconds,
            climb = climb,
            times = times,
        )
        return RidePageData.Success(summary, details)
    }

    override fun getLabel(page: Int, index: Int): String {
        return (index + 1).toString()
    }
}