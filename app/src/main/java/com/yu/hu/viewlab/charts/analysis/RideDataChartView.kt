package com.yu.hu.viewlab.charts.analysis

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.OverScroller
import androidx.core.content.ContextCompat
import com.yu.hu.viewlab.R
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * huyu create
 * 2026/3/20 9:07
 */
class RideDataChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // ====== 尺寸配置 ======
    private val lineHeight = dp(1f)
    private var barWidth = dp(20f)
    private val barRadius = dp(2f)
    private val topSpace = dp(8f)
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    // 用于绘制x y轴刻度
    private val startLabelSpace = dp(28f)
    private val bottomLabelSpace = dp(24f)

    // ====== 画笔 ======
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF6E9C85")
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4D000000")
        textSize = sp(12f)
    }

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4D000000")
        strokeWidth = lineHeight
        style = Paint.Style.STROKE
    }

    private val dashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4D000000")
        strokeWidth = lineHeight
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(dp(2f), dp(2f)), 0f)
    }

    private val barPath = Path()
    private val contentTop get() = paddingTop
    private val contentBottom get() = height - paddingBottom
    private val contentStart get() = paddingStart
    private val contentEnd get() = width - paddingEnd

    //图表部分宽度
    val pageDataWidth: Float get() = (contentEnd - contentStart - startLabelSpace).coerceAtLeast(0f)

    private var controller: ChartController = ChartController.EMPTY
    private val slotCount get() = controller.slotCount
    private var currentPage: Int
        get() = controller.currentPage
        set(value) {
            controller.currentPage = value
        }

    private var loadingRotation: Float = 0f
    private val loadingDrawable by lazy {
        ContextCompat.getDrawable(context, R.drawable.ic_toast_loading)
    }
    private val loadingAnimator by lazy {
        ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 800
            repeatCount = ValueAnimator.INFINITE
            interpolator = null // 线性旋转
            addUpdateListener {
                loadingRotation = it.animatedValue as Float
                postInvalidateOnAnimation()
            }
        }
    }

    private var isSelecting: Boolean = false
    private var selectedSlotIndex: Int = -1
    private var onSlotSelected: ((Pair<Float, RideSummaryData>?) -> Unit)? = null

    private var displayMaxAxisY: Float = DEFAULT_MAX_AXIS_Y
    private var maxAxisYAnimator: ValueAnimator? = null
    private val scroller = OverScroller(context)
    private var isDragging: Boolean = false
    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                var targetX = scrollX + distanceX.toInt()
                if (targetX > 0 || targetX < getPageScrollX(controller.pageCount - 1)) {
                    // 没有数据了 只生效一半
                    scrollBy(distanceX.times(0.5f).toInt(), 0)
                } else {
                    scrollBy(distanceX.toInt(), 0)
                }
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                super.onLongPress(e)
                if (onSlotSelected != null) {
                    isSelecting = true
                    checkSlotSelection(e.x)
                }
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                // 基于当前位置计算基准页，而不是未更新的 currentPage
                val basePage = getScrollPage()
                // velocityX > 0 是向右快速滑动（想看左边），velocityX < 0 是向左快速滑动
                smoothScrollToPage(
                    when {
                        abs(velocityX) < 500 -> basePage
                        velocityX > 0 -> basePage + 1
                        else -> basePage - 1
                    }
                )
                return true
            }

            override fun onDown(e: MotionEvent): Boolean {
                return true
            }
        })

    /**
     * 设置长按选中图表监听器
     */
    fun setOnSlotSelectedListener(listener: (Pair<Float, RideSummaryData>?) -> Unit) {
        onSlotSelected = listener
    }

    fun attach(controller: ChartController) {
        this.controller = controller
        this.barWidth = dp(controller.slotBarWidth)
        val pageData = controller[currentPage]
        this.displayMaxAxisY = pageData.maxAxisY
        this.scrollX = getPageScrollX(currentPage)
        resetLoadingAnimator(pageData)
        invalidate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        resetLoadingAnimator(controller[currentPage])
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        loadingAnimator.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 绘制固定坐标轴
        drawAxis(canvas)
        // 计算可见范围
        // scrollX 范围在 [-(pageSize-1) * pageDataWidth, 0] 之间
        val scrollRatio = -scrollX.toFloat() / pageDataWidth
        val firstVisiblePage =
            floor(scrollRatio.toDouble()).toInt().coerceIn(0, controller.pageCount - 1)
        val lastVisiblePage =
            ceil(scrollRatio.toDouble()).toInt().coerceIn(0, controller.pageCount - 1)
        canvas.withSave {
            // 裁剪绘制区域，避免图表重叠绘制在y轴上
            clipRect(
                scrollX.toFloat() + contentStart + startLabelSpace,
                0f,
                scrollX.toFloat() + contentEnd,
                height.toFloat()
            )

            // 绘制图表
            for (page in firstVisiblePage..lastVisiblePage) {
                drawContent(canvas, page)
            }
        }
    }

    /**
     * 绘制坐标轴
     */
    private fun drawAxis(canvas: Canvas) {
        canvas.withSave {
            //平移至scrollX 让坐标轴位置保持不变
            translate(scrollX.toFloat(), 0f)

            val data = controller[currentPage]
            val maxAxisY = data.maxAxisY

            // 绘制Y轴刻度值
            textPaint.textAlign = Paint.Align.LEFT
            drawText(
                maxAxisY.toInt().toString(),
                0f,
                contentTop + topSpace - textPaint.fontMetrics.ascent, //ascent < 0，所以是-
                textPaint
            )
            drawText(
                0.toString(),
                0f,
                contentBottom - bottomLabelSpace - textPaint.fontMetrics.descent,
                textPaint
            )

            // 绘制x轴刻度线
            val lineCount = 5
            val lineSpace =
                (contentBottom - contentTop - topSpace - bottomLabelSpace - lineCount * lineHeight) / (lineCount - 1)
            val startY = contentTop + topSpace + lineHeight / 2
            for (i in 0 until lineCount) {
                drawLine(
                    contentStart + startLabelSpace,
                    startY + (lineHeight + lineSpace) * i,
                    contentEnd.toFloat(),
                    startY + (lineHeight + lineSpace) * i,
                    if (i == lineCount - 1) axisPaint else dashPaint
                )
            }

            // 绘制x轴刻度值
            forEachSlot { index, left, right ->
                val label = controller.getLabel(currentPage, index) ?: return@forEachSlot
                val textX: Float
                if (index == 0 && controller.isXAxisEdgeAligned) {
                    textPaint.textAlign = Paint.Align.LEFT
                    textX = contentStart + startLabelSpace
                } else if (index == slotCount - 1 && controller.isXAxisEdgeAligned) {
                    textPaint.textAlign = Paint.Align.RIGHT
                    textX = contentEnd.toFloat()
                } else {
                    textPaint.textAlign = Paint.Align.CENTER
                    textX = (left + right) / 2
                }
                drawText(
                    label,
                    textX,
                    contentBottom - textPaint.fontMetrics.descent,
                    textPaint
                )
            }
        }
    }

    /** 绘制内容 */
    private fun drawContent(canvas: Canvas, page: Int) {
        val data = controller[page]
        if (data is RidePageData.Error) return

        canvas.withSave {
            translate(-page * pageDataWidth, 0f)

            when (data) {
                is RidePageData.Loading -> drawLoading(canvas)
                is RidePageData.Success -> drawSlots(canvas, data)
                else -> Unit
            }
        }
    }

    /** 绘制loading */
    private fun drawLoading(canvas: Canvas) {
        val drawable = loadingDrawable ?: return

        canvas.withSave {
            val centerX = (contentEnd - contentStart) / 2f
            val centerY = (contentBottom - bottomLabelSpace - contentTop) / 2
            translate(centerX, centerY)
            rotate(loadingRotation)

            val size = dp(28f).toInt()
            drawable.setBounds(-size / 2, -size / 2, size / 2, size / 2)
            drawable.draw(this)
        }
    }

    /** 绘制图表柱子 */
    private fun drawSlots(canvas: Canvas, data: RidePageData) {
        data.forEachSlot { i, _, rect ->
            // 选中态指示线
            if (i == selectedSlotIndex) {
                val centerX = (rect.left + rect.right) / 2
                canvas.drawLine(
                    centerX, contentTop.toFloat(),
                    centerX, rect.top,
                    axisPaint
                )
            }

            // 柱状图
            barPath.reset()
            barPath.addRoundRect(
                rect,
                floatArrayOf(
                    barRadius, barRadius, //左上
                    barRadius, barRadius, //右上
                    0f, 0f,
                    0f, 0f
                ),
                Path.Direction.CW
            )
            canvas.drawPath(barPath, barPaint)
        }
    }

    private fun RidePageData.forEachSlot(block: (index: Int, RideSummaryData, RectF) -> Unit) {
        val details = detailsOrNull ?: return
        if (details.isEmpty()) return
        val barBottom = contentBottom - bottomLabelSpace - lineHeight
        val barMaxHeight = barBottom - contentTop - topSpace
        val maxAxisY = displayMaxAxisY
        val rect = RectF()
        forEachSlot { i, left: Float, right: Float ->
            val item = details.getOrNull(i)
            if (item != null && item.distance > 0f) {
                val ratio = item.distance / maxAxisY
                // 限制下top不要超出控件范围 越往上值越小所以是coerceAtLeast
                val top = (barBottom - barMaxHeight * ratio).coerceAtLeast(contentTop.toFloat())
                rect.set(left, top, right, barBottom)
                block(i, item, rect)
            }
        }
    }

    private fun forEachSlot(block: (index: Int, left: Float, right: Float) -> Unit) {
        val barSpace =
            (contentEnd - contentStart - startLabelSpace - slotCount * barWidth) / slotCount
        val startX = contentStart + startLabelSpace + barSpace / 2
        for (i in 0 until slotCount) {
            val left = startX + i * (barWidth + barSpace)
            val right = left + barWidth
            block(i, left, right)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!scroller.isFinished) return false
        if (maxAxisYAnimator?.isRunning == true) return false
        // 处理滑动冲突
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(true)
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }
        // 处理点击
        if (handleSelection(event)) return true
        // 处理滑动
        val handled = gestureDetector.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> isDragging = handled
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                if (!handled) {
                    // 手指抬起时，如果没有触发 Fling，则自动对齐到最近的一页
                    // scrollX < 0, 在负坐标系下，计算 page 需取反
                    smoothScrollToPage(getScrollPage())
                }
            }
        }
        return true
    }

    /**
     * 处理图表柱子区域点击
     *
     * @return 是否消费事件
     */
    private fun handleSelection(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isSelecting = false
            }

            MotionEvent.ACTION_MOVE -> {
                if (isSelecting) checkSlotSelection(event.x)
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                if (isSelecting) {
                    this.selectedSlotIndex = -1
                    isSelecting = false
                    onSlotSelected?.invoke(null)
                    invalidate()
                    return true
                }
            }
        }
        return isSelecting
    }

    /**
     * 判断是否在图表柱子区域
     *
     * 如果在区域内，会更新[selectedSlotIndex]并触发[controller]回调
     *
     * @return 是否在图表柱子区域
     */
    private fun checkSlotSelection(selectionX: Float): Boolean {
        if (selectionX < contentStart + startLabelSpace) return false

        // 1. 计算页码 (基于负坐标系)
        val page = ((-scrollX + pageDataWidth / 2) / pageDataWidth).toInt()
            .coerceIn(0, controller.pageCount - 1)
        if (page != currentPage) Log.w(TAG, "page not matched $page $currentPage")

        // 2. 计算在该页内的 Slot 索引
        var slotIndex: Int = -1
        controller[page].forEachSlot { index, summaryData, rect ->
            if (selectionX > rect.left - touchSlop && selectionX < rect.right + touchSlop) {
                slotIndex = index
                if (selectedSlotIndex != index) {
                    selectedSlotIndex = index
                    val slotCenterX: Float = (rect.left + rect.right) / 2
                    onSlotSelected?.invoke(slotCenterX to summaryData)
                    invalidate()
                }
                return@forEachSlot
            }
        }
        Log.i(TAG, "checkSlotSelection $selectionX $slotIndex")
        return slotIndex != -1
    }

    /**
     * 平滑滑动到指定页
     */
    private fun smoothScrollToPage(page: Int) {
        val targetPage = page.coerceIn(0, controller.pageCount - 1)
        val targetX = getPageScrollX(targetPage)
        val deltaX = targetX - scrollX
        Log.i(TAG, "smoothScrollToPage $targetPage $deltaX")
        scroller.startScroll(scrollX, 0, deltaX, 0, 400)
        invalidate()
    }

    private fun getScrollPage(): Int {
        val pageWidth = pageDataWidth.roundToInt()
        return (-scrollX + pageWidth / 2) / pageWidth
    }

    private fun getPageScrollX(page: Int) = -page * pageDataWidth.roundToInt()

    override fun computeScroll() {
//        Log.d("hytest", "computeScroll")
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.currX, 0)
            postInvalidateOnAnimation()
        } else if (!isSelecting && !isDragging) {
            val pageWidth = pageDataWidth.roundToInt()
            val finalPage = (-scrollX + pageWidth / 2) / pageWidth
            val pageData = controller[finalPage]
            resetLoadingAnimator(pageData)
            if (currentPage == finalPage) return
            currentPage = finalPage
            if (pageData is RidePageData.Success) animateAxisY(pageData.maxAxisY)
        }
    }

    private fun resetLoadingAnimator(data: RidePageData) {
        if (data is RidePageData.Loading) {
            if (!loadingAnimator.isStarted) {
                loadingAnimator.start()
            } else {
                loadingAnimator.resume()
            }
        } else {
            loadingAnimator.cancel()
        }
    }

    /**
     * 执行高度伸缩动画
     */
    private fun animateAxisY(targetAxis: Float) {
        maxAxisYAnimator?.cancel()
        maxAxisYAnimator = ValueAnimator.ofFloat(displayMaxAxisY, targetAxis).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                displayMaxAxisY = it.animatedValue as Float
                postInvalidateOnAnimation()
            }
            start()
        }
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

    private fun Canvas.withSave(block: Canvas.() -> Unit) {
        val save = save()
        try {
            block()
        } finally {
            restoreToCount(save)
        }
    }

    /**
     * 计算y轴最大刻度值
     */
    private val RidePageData.maxAxisY: Float
        get() {
            val details = detailsOrNull ?: return DEFAULT_MAX_AXIS_Y
            if (details.isEmpty()) return DEFAULT_MAX_AXIS_Y
            val maxDistance = details.maxOf { it.distance }
            // 最大距离的1.1倍 向上取整
            return ceil(maxDistance.times(1.1f)).coerceAtLeast(1f)
        }

    companion object {
        private const val TAG = "RideDataChartView"
        private const val DEFAULT_MAX_AXIS_Y = 50f
    }
}