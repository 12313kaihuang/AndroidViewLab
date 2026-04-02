package com.yu.hu.viewlab.charts.analysis.controller

import com.yu.hu.viewlab.charts.analysis.RidePageData
import com.yu.hu.viewlab.charts.analysis.RideSummaryData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import kotlin.random.Random

/**
 * huyu create
 * 2026/3/24 10:58
 */
open class FakeWeeksDataController(
    scope: CoroutineScope,
    protected val weeks: Int,
) : AbsChartController<String>(scope) {

    // 柱子个数 = 周数
    override val slotCount: Int = weeks

    // 当前仅适配了12周宽度
    override val slotBarWidth: Float = 14f

    override val tag: String = "WeeksDataController"
    override val isXAxisEdgeAligned: Boolean = true

    protected val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    protected val fullFormatter = DateTimeFormatter.ofPattern("yyyy/M/dd")
    protected val monthDayFormatter = DateTimeFormatter.ofPattern("M/dd")

    private val weekStart =
        LocalDateTime.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .minusWeeks(weeks.toLong() - 1)

    private val weekEnd =
        LocalDateTime.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

    override fun getPageKey(page: Int): String {
        return LocalDateTime.now().minusWeeks(page * weeks.toLong()).format(dateFormatter)
    }

    override fun emptySummaryData(page: Int): RideSummaryData {
        val pageStart = getPageStart(page)
        val pageEnd = getPageEnd(page)
        return RideSummaryData(generateLabel(pageStart, pageEnd))
    }

    override fun getPageCount(earliestRideTime: LocalDateTime): Int {
        var page = 0
        val localDate = earliestRideTime.toLocalDate()
        while (!localDate.isAfter(getPageEnd(page).toLocalDate())) page++
        return page.coerceAtLeast(1)
    }

    protected fun generateLabel(start: LocalDateTime, end: LocalDateTime): String {
        return if (start.year == end.year) {
            "${start.format(fullFormatter)}-${end.format(monthDayFormatter)}"
        } else {
            "${start.format(fullFormatter)}-${end.format(fullFormatter)}"
        }
    }

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
                    label = generateLabel(
                        pageStart.plusWeeks(count.toLong()),
                        pageEnd.plusWeeks(count.toLong())
                    ),
                    distance = random.nextInt(50,1000).times(0.01f).coerceIn(0f, 200f),
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

    override fun getLabel(page: Int, index: Int): String? = when (index) {
        0 -> getPageStart(page).format(monthDayFormatter)
        slotCount - 1 -> getPageEnd(page).format(monthDayFormatter)
        else -> null
    }

    protected fun getPageStart(page: Int): LocalDateTime =
        weekStart.minusWeeks(page * weeks.toLong())

    protected fun getPageEnd(page: Int): LocalDateTime =
        weekEnd.minusWeeks(page * weeks.toLong())
}