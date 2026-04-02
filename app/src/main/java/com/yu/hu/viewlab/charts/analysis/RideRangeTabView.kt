package com.yu.hu.viewlab.charts.analysis

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import java.util.Locale

/**
 * huyu create
 * 2026/3/23 16:09
 */
class RideRangeTabView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // ====== 尺寸 ======
    private val bgRadius = dp(8f)
    private val dividerPadding = dp(8f)
    private val dividerWidth = dp(0.5f)
    private val foregroundPadding = dp(2f)

    // ====== 画笔 ======
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = sp(13f)
        textAlign = Paint.Align.CENTER
    }

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    var selectedIndex: Int = 0
        private set
    private var selectionOffset: Float = 0f
    private var selectionAnimator: ValueAnimator? = null
    private var tabs: List<RideRange> = emptyList()
    private var onClickListener: ((Int, RideRange) -> Unit)? = null

    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                handleClick(e)
                return true
            }

            override fun onDown(e: MotionEvent): Boolean {
//                if (selectionAnimator.isRunning) selectionAnimator.d
                return true
            }
        })

    fun init(index: Int, tabs: List<RideRange>, onClick: (Int, RideRange) -> Unit) {
        this.selectedIndex = index
        this.tabs = tabs
        this.onClickListener = onClick
        // 如果 init 时 View 已经布局过了（width > 0），则直接同步偏移量
        if (width > 0) selectionOffset = getTargetOffset(selectedIndex)
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    private fun handleClick(event: MotionEvent) {
        val tabWidth = calcTabWidth()
        var start = foregroundPadding
        tabs.forEachIndexed { index, range ->
            if (event.x >= start && event.x <= start + tabWidth) {
                if (index == selectedIndex) return@forEachIndexed

                if (selectionAnimator?.isRunning == true) selectionAnimator?.cancel()
                val targetOffset = getTargetOffset(index)
                selectionAnimator = ValueAnimator.ofFloat(selectionOffset, targetOffset).apply {
                    duration = 200
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener {
                        selectionOffset = it.animatedValue as Float
                        invalidate()
                    }
                    start()
                }
                this.selectedIndex = index
                onClickListener?.invoke(index, range)
                return
            }
            start += tabWidth + dividerWidth
        }
    }

    /**
     * 计算指定索引的目标偏移量
     */
    private fun getTargetOffset(index: Int): Float {
        if (tabs.isEmpty() || width <= 0) return foregroundPadding
        val tabWidth = calcTabWidth()
        return if (index == 0) {
            // 第一个 Tab 从 padding 处开始
            foregroundPadding
        } else {
            // 非第一个 Tab，起始位置向左偏移 dividerWidth 以遮盖前一个分割线
            foregroundPadding + index * (tabWidth + dividerWidth) - dividerWidth
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // 布局确定后，立即同步偏移量到当前选中的位置
        if (tabs.isNotEmpty()) {
            selectionOffset = getTargetOffset(selectedIndex)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val tabWidth = calcTabWidth()
        drawBackground(canvas, tabWidth)
        drawTabs(canvas, tabWidth)
    }

    private fun drawBackground(canvas: Canvas, tabWidth: Float) {
        // background
        bgPaint.color = Color.parseColor("#1F767680")
        canvas.drawRoundRect(
            RectF(0f, 0f, width.toFloat(), height.toFloat()),
            bgRadius,
            bgRadius,
            bgPaint
        )
        // divider
        for (i in 1 until tabs.size) {
            textPaint.color = Color.parseColor("#5C3C3C43")
            val dividerLeft = foregroundPadding + i * (tabWidth + dividerWidth) - dividerWidth
            canvas.drawRect(
                dividerLeft,
                dividerPadding,
                dividerLeft + dividerWidth,
                height - dividerPadding,
                textPaint
            )
        }
        // foreground
        bgPaint.setShadowLayer(8f, 0f, 3f, Color.parseColor("#1F000000"))
        bgPaint.color = Color.WHITE
        val foregroundRight = when (selectedIndex) {
            0,
            tabs.lastIndex -> selectionOffset + tabWidth + dividerWidth

            else -> selectionOffset + tabWidth + dividerWidth * 2
        }
        canvas.drawRoundRect(
            RectF(
                selectionOffset,
                foregroundPadding,
                foregroundRight,
                height - foregroundPadding
            ),
            bgRadius,
            bgRadius,
            bgPaint
        )
        bgPaint.clearShadowLayer()
    }

    private fun drawTabs(canvas: Canvas, tabWidth: Float) {
        var startX = foregroundPadding + tabWidth / 2
        tabs.forEachIndexed { index, range ->
            textPaint.color = if (index == selectedIndex) {
                Color.BLACK
            } else {
                Color.parseColor("#80000000")
            }
            val fontMetrics = textPaint.fontMetrics
            canvas.drawText(
                formatRange(range),
                startX,
                height / 2f - (fontMetrics.ascent + fontMetrics.descent) / 2,
                textPaint
            )
            startX += tabWidth + dividerWidth
        }
    }

    private fun calcTabWidth(): Float {
        return (width - foregroundPadding * 2 - dividerWidth * (tabs.size - 1)) / tabs.size
    }

    private fun formatRange(range: RideRange): String = when (range) {
        is RideRange.WeekDay -> if (range.weeks == 1) {
            String.format(Locale.US, "%d天", 7)
        } else {
            String.format(Locale.US, "%s周", range.weeks.toString())
        }

        is RideRange.Weeks -> String.format(Locale.US, "%s周", range.weeks.toString())
        is RideRange.Year -> String.format(Locale.US, "%d年", 1)
    }

    private fun dp(value: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value,
            resources.displayMetrics
        )
    }

    private fun sp(value: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            value,
            resources.displayMetrics
        )
    }
}