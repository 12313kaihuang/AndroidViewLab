package com.yu.hu.viewlab.charts.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yu.hu.viewlab.charts.analysis.controller.AbsChartController
import com.yu.hu.viewlab.charts.analysis.controller.WeekDayDataControllerFake
import com.yu.hu.viewlab.charts.analysis.controller.FakeWeeksDataController
import com.yu.hu.viewlab.charts.analysis.controller.YearDataController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * huyu create
 * 2026/3/23 17:02
 */
class RecordAnalysisViewModel : ViewModel() {

    val tabIndex: Int = 3
    val tabs = listOf(
        RideRange.WeekDay(1),
        RideRange.WeekDay(6),
        RideRange.Weeks(12),
        RideRange.Year,
    )

    private val controllers = HashMap<RideRange, ChartController>()

    @Volatile
    private var earliestRideTime: LocalDateTime = LocalDateTime.now()

    private val _uiState = MutableStateFlow(RecordAnalysisUiState())
    val uiState = _uiState.asStateFlow()

    init {
        val range = tabs[tabIndex]
        controllers[range] = createController(range)
        viewModelScope.launch {
            initRecordPageCount()
        }
    }

    fun getTabController(tab: RideRange): ChartController {
        return controllers.getOrPut(tab) { createController(tab) }
    }

    private suspend fun initRecordPageCount() {
        runCatching {
            // mock data
            LocalDateTime.now().minusYears(20)
        }.onSuccess { time ->
            earliestRideTime = time
            controllers.values.forEach { controller ->
                if (controller is AbsChartController<*>) controller.updatePageCount(time)
            }
        }
    }

    private fun createController(tab: RideRange): ChartController {
        return when (tab) {
            is RideRange.WeekDay -> WeekDayDataControllerFake(viewModelScope, tab)
            is RideRange.Year -> YearDataController(viewModelScope)
            is RideRange.Weeks -> FakeWeeksDataController(viewModelScope, tab.weeks)
        }.apply { updatePageCount(earliestRideTime) }
    }

    companion object {
        private const val TAG = "RecordAnalysisViewModel"
    }
}