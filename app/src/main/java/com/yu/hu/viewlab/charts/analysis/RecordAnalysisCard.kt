package com.yu.hu.viewlab.charts.analysis

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.yu.hu.viewlab.R
import com.yu.hu.viewlab.databinding.LayoutRecordAnalysisCardBinding
import java.util.Locale
import kotlin.math.roundToInt

/**
 * huyu create
 * 2026/3/23 9:03
 */
class RecordAnalysisCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val mBinding: LayoutRecordAnalysisCardBinding
    private var prevController: ChartController? = null

    init {
        orientation = VERTICAL
        setBackgroundResource(R.drawable.bg_white_r_12)
        val paddingTop = dp(12f).roundToInt()
        val paddingBottom = dp(8f).roundToInt()
        setPadding(paddingTop, paddingTop, paddingTop, paddingBottom)
        mBinding = LayoutRecordAnalysisCardBinding.inflate(LayoutInflater.from(context), this)
    }

    /**
     * @param index 初始化tab索引
     */
    fun init(
        index: Int,
        tabs: List<RideRange>,
        getController: (RideRange) -> ChartController
    ) {
        mBinding.tabView.init(index, tabs) { _, tab ->
            val controller = getController(tab)
            attach(controller)
        }
        attach(getController(tabs[index]))
    }

    private fun attach(dataController: ChartController) {
        // 移除旧数据变化监听
        prevController?.setOnDataChangedListener(null)
        prevController = dataController

        dataController.setOnPageChangedListener {
            showSummaryDataCard(dataController)
        }
        dataController.setOnDataChangedListener {
            showSummaryDataCard(dataController)
            mBinding.chartView.attach(dataController)
        }
        mBinding.chartView.setOnSlotSelectedListener { selection ->
            if (selection != null) {
                showSummaryDataCard(selection.first, selection.second)
            } else {
                showSummaryDataCard(dataController)
            }
        }
        showSummaryDataCard(dataController)
        mBinding.chartView.attach(dataController)
    }

    private fun showSummaryDataCard(dataController: ChartController) {
        showSummaryDataCard(null, dataController[dataController.currentPage].summary)
    }

    /**
     * [screenX]卡片偏移，null表示不偏移且为汇总信息
     */
    private fun showSummaryDataCard(screenX: Float?, data: RideSummaryData) {
        with(mBinding.tipsCard) {
            tvLabel.text = data.label
            val distance = String.format(Locale.US, "%.2f", data.distance)
            tvDistanceValue.text = distance
            tvDistanceUnit.text = "km"
            val hours = data.timeSeconds / (60 * 60)
            val minutes = (data.timeSeconds / 60) % 60
            val seconds = data.timeSeconds % 60
            tvTimeValue.text = String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
            tvClimbValue.text = String.format(Locale.US, "%.2f", data.climb)
            tvClimbUnit.text = "m"
            tvTimesValue.text = data.times.toString()
        }

        val cardRoot = mBinding.tipsCard.root
        val top = dp(8f).toInt()
        val bottom = dp(10f).toInt()
        if (screenX == null) {
            cardRoot.setPadding(0, top, 0, bottom)
            cardRoot.setBackgroundResource(0)
            cardRoot.translationX = 0f
        } else {
            val padding = dp(12f).toInt()
            cardRoot.setPadding(padding, top, padding, bottom)
            cardRoot.setBackgroundResource(R.drawable.bg_record_summary_data_card)
            cardRoot.post {
                val contentWidth = this@RecordAnalysisCard.width - paddingStart - paddingEnd
                val halfCardWidth = cardRoot.width / 2f
                cardRoot.translationX = when {
                    screenX < halfCardWidth -> 0f
                    screenX + halfCardWidth > contentWidth -> contentWidth - halfCardWidth * 2
                    else -> screenX - halfCardWidth
                }
            }
        }
    }

    private fun dp(value: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value,
            resources.displayMetrics
        )
    }
}