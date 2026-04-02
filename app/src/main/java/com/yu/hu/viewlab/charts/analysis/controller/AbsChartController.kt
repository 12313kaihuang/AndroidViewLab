package com.yu.hu.viewlab.charts.analysis.controller

import android.util.Log
import com.yu.hu.viewlab.charts.analysis.ChartController
import com.yu.hu.viewlab.charts.analysis.RidePageData
import com.yu.hu.viewlab.charts.analysis.RideSummaryData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDateTime
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

/**
 * 统计分析图表控制器基类
 *
 * 封装了分页数据的异步加载、三级缓存（内存）、并发互斥以及页面切换通知逻辑。
 *
 * @param T 分页数据的唯一标识类型（如年份 Int, 周起始日期 String 等）
 * @param scope 协程作用域，用于执行异步加载任务
 *
 * @see com.yu.hu.viewlab.charts.analysis.RecordAnalysisCard
 * @see com.yu.hu.viewlab.charts.analysis.RideDataChartView
 *
 * huyu create
 * 2026/3/24 10:18
 */
abstract class AbsChartController<T : Any>(
    protected val scope: CoroutineScope
) : ChartController {
    abstract val tag: String

    /** 当前显示的页码，带有页面切换监听触发 */
    override var currentPage: Int = 0
        set(value) {
            val fixed = value.coerceIn(0, pageCount - 1)
            if (field != fixed) {
                field = fixed
                onPageChanged?.invoke(fixed)
            }
        }

    override var pageCount: Int = 1

    /** 互斥锁，确保同一时间只有一个加载任务在执行（防止多个分页请求竞争资源） */
    private val loadMutex = Mutex()

    /** 正在加载中的 Key 集合，用于同步阶段的去重 */
    private val loadingKeys = Collections.newSetFromMap<T>(ConcurrentHashMap())

    /** 已加载数据的内存缓存映射表 */
    private val loadedDataMap = ConcurrentHashMap<T, RidePageData>()

    private var onPageChanged: ((Int) -> Unit)? = null
    private var onDataChangedListener: (() -> Unit)? = null

    override fun setOnPageChangedListener(listener: (Int) -> Unit) {
        onPageChanged = listener
    }

    override fun setOnDataChangedListener(listener: (() -> Unit)?) {
        this.onDataChangedListener = listener
    }

    override fun get(page: Int): RidePageData {
        val targetKey = getPageKey(page)
        // 1. 从缓存获取或创建初始状态（Loading/Error）
        val pageData = loadedDataMap.getOrPut(targetKey) {
            val summaryData = emptySummaryData(page)
            if (page in 0 until pageCount) {
                RidePageData.Loading(summaryData)
            } else {
                RidePageData.Error(
                    summary = summaryData,
                    throwable = IllegalArgumentException("page $page out of range")
                )
            }
        }
        // 2. 如果是 Loading 或 Error 状态，触发后台加载逻辑
        when (pageData) {
            is RidePageData.Loading,
            is RidePageData.Error -> loadDataInternal(page, targetKey)

            else -> Unit
        }
        return pageData
    }

    /**
     * 内部异步加载逻辑
     */
    private fun loadDataInternal(page: Int, key: T) {
        if (page !in 0 until pageCount) return
        // 快速检查：如果该 Key 正在加载中，则不重复开启协程
        if (loadingKeys.contains(key)) return
        Log.i(tag, "loadDataInternal $page $key")
        loadingKeys.add(key)

        scope.launch {
            // 关键：排队等待，上一个 withLock 结束前，这里会挂起
            loadMutex.withLock {
                try {
                    // 二次检查：在等待锁的过程中，可能其他协程已经加载成功了
                    if (loadedDataMap[key] is RidePageData.Success) return@withLock
                    // 执行实际的加载逻辑（例如网络请求）
                    loadedDataMap[key] = loadPageData(page)
                    Log.i(tag, "loadPageData $key success")
                } catch (e: Exception) {
                    Log.e(tag, "loadPageData $key failed:${e.stackTraceToString()}")
                    loadedDataMap[key] = RidePageData.Error(
                        summary = emptySummaryData(page),
                        throwable = e
                    )
                } finally {
                    // 无论成功失败，移除加载中标记并通知 UI 刷新
                    loadingKeys.remove(key)
                    notifyDataChanged()
                }
            }
        }
    }

    /** 通知图表组件数据已更新，触发重绘 */
    private fun notifyDataChanged() {
        onDataChangedListener?.invoke()
    }

    /** 获取对应页码的唯一标识 Key */
    abstract fun getPageKey(page: Int): T

    /** 构造该页的初始概览数据（主要用于 Loading 时的标题占位） */
    abstract fun emptySummaryData(page: Int): RideSummaryData

    /** 具体的异步加载实现 */
    abstract suspend fun loadPageData(page: Int): RidePageData

    /** 更新最大页数 */
    fun updatePageCount(earliestRideTime: LocalDateTime) {
        val count = getPageCount(earliestRideTime)
        Log.i(tag, "updatePageCount $count")
        this.pageCount = count
        notifyDataChanged()
    }

    /** 根据最早骑行时间计算最大页数 */
    abstract fun getPageCount(earliestRideTime: LocalDateTime): Int

    /** 日期工具函数：判断是否为同一天 */
    protected fun LocalDateTime.isSameDay(another: LocalDateTime): Boolean {
        return toLocalDate().isEqual(another.toLocalDate())
    }
}