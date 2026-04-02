package com.yu.hu.viewlab.charts.analysis.controller

import com.yu.hu.viewlab.charts.analysis.RidePageData
import com.yu.hu.viewlab.charts.analysis.RideRange
import com.yu.hu.viewlab.charts.analysis.RideSummaryData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * 周 - 天
 *
 * huyu create
 * 2026/3/24 10:58
 */
class WeekDayDataControllerFake(
    scope: CoroutineScope,
    range: RideRange.WeekDay
) : FakeWeeksDataController(scope, range.weeks) {

    // 柱子个数 = 天数
    override val slotCount: Int = weeks * 7
    override val slotBarWidth: Float = when (weeks) {
        1 -> 20f
        6 -> 3f
        else -> 14f //12周
    }

    override val tag: String = "WeekDayDataController"

    // 7天居中对齐，否则两边对齐
    override val isXAxisEdgeAligned: Boolean = weeks != 1

    override suspend fun loadPageData(page: Int): RidePageData {
        //mock data
        delay(500)
        val random = Random(System.currentTimeMillis())
        val pageStart = getPageStart(page)
        val pageEnd = getPageEnd(page)
        var distance = 0f
        var timeSeconds = 0L
        var climb = 0f
        var times = 0
        val details = buildList {
            repeat(slotCount) { count ->
                val data = RideSummaryData(
                    label = pageStart.plusDays(count.toLong()).format(fullFormatter),
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
            label = generateLabel(pageStart, pageEnd),
            distance = distance,
            timeSeconds = timeSeconds,
            climb = climb,
            times = times,
        )
        return RidePageData.Success(summary, details)
    }

    override fun getLabel(page: Int, index: Int): String? {
        if (weeks == 1) {
            return getPageStart(page).plusDays(index.toLong()).format(monthDayFormatter)
        }
        return super.getLabel(page, index)
    }
}