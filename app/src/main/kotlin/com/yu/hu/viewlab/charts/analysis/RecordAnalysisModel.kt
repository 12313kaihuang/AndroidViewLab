package com.yu.hu.viewlab.charts.analysis

/**
 * huyu create
 * 2026/3/19 15:12
 */

data class RecordAnalysisUiState(
    // 功率分布  6周-总计
    val powerDistribution: Pair<List<DistributionInfo>?, List<DistributionInfo>?> = Pair(
        null,
        null
    ),
    // 功率分布  6周-总计
    val hrDistribution: Pair<List<DistributionInfo>?, List<DistributionInfo>?> = Pair(null, null),
)

data class DistributionInfo(
    val precent: Float = 0f, //占比
    val timeSeconds: Long = 0 //时长 s
)

/**
 * 骑行数据范围
 */
sealed interface RideRange {
    data class WeekDay(val weeks: Int) : RideRange
    data class Weeks(val weeks: Int) : RideRange
    data object Year : RideRange
}

sealed interface RidePageData {
    val summary: RideSummaryData

    data class Loading(override val summary: RideSummaryData) : RidePageData
    data class Success(
        override val summary: RideSummaryData,
        val details: List<RideSummaryData>
    ) : RidePageData

    data class Error(
        override val summary: RideSummaryData,
        val throwable: Throwable
    ) : RidePageData

    val detailsOrNull get() = (this as? Success)?.details
}

/**
 * @param label 标签/id  2025 2025/6等
 * @param distance 总距离 km
 * @param timeSeconds 总时长 s
 * @param climb 总爬升 m
 * @param times 总次数
 */
data class RideSummaryData(
    val label: String,
    val distance: Float = 0f,
    val timeSeconds: Long = 0,
    val climb: Float = 0f,
    val times: Int = 0
)

interface ChartController {

    /**
     * 总页数
     */
    val pageCount: Int

    /**
     * 柱子数量
     */
    val slotCount: Int

    /**
     * 柱子宽度 dp
     */
    val slotBarWidth: Float

    /**
     * 当前页数
     */
    var currentPage: Int

    /**
     * 首个/最后一个x刻度值是否靠边对齐
     */
    val isXAxisEdgeAligned: Boolean

    /**
     * 获取[page]页数据
     */
    operator fun get(page: Int): RidePageData

    /**
     * 获取label
     */
    fun getLabel(page: Int, index: Int): String?

    /**
     * 页面切换监听
     */
    fun setOnPageChangedListener(listener: (Int) -> Unit)

    /**
     * 数据更新监听
     */
    fun setOnDataChangedListener(listener: (() -> Unit)?)

    companion object {
        val EMPTY = object : ChartController {
            override val isXAxisEdgeAligned: Boolean = false
            override val slotCount: Int = 12
            override val slotBarWidth: Float = 14f
            override var currentPage: Int = 0
            override val pageCount: Int = 1
            override fun get(page: Int): RidePageData =
                RidePageData.Loading(RideSummaryData("", 0f, 0, 0f, 0))

            override fun getLabel(page: Int, index: Int): String? = null

            override fun setOnPageChangedListener(listener: (Int) -> Unit) {

            }

            override fun setOnDataChangedListener(listener: (() -> Unit)?) {

            }
        }
    }
}