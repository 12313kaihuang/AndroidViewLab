package com.yu.hu.viewlab.charts.power

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * huyu create
 * 2026/4/2 16:28
 */
class PowerDataViewModel : ViewModel() {

    val dataBuffer: TimeWindowBuffer = TimeWindowBufferImpl(1000)

    init {
        viewModelScope.launch {
            val random = Random(System.currentTimeMillis())
            while (true) {
                delay(20)
                dataBuffer.add( //6.5 + 1.5 = 8   /16
                    PowerDistribution(
                        startAngle = random.nextInt(50).toFloat(),
                        endAngle = random.nextInt(120, 180).toFloat(),
                        peakStartAngle = random.nextInt(50, 90).toFloat(),
                        peakEndAngle = random.nextInt(100, 180).toFloat()
                    )
                )
            }
        }
    }

    private class TimeWindowBufferImpl(
        override val windowDurationsMs: Long
    ) : TimeWindowBuffer {

        private val data = ArrayDeque<DataPointWrapper>()
        private var startSum: Float = 0f
        private var endSum: Float = 0f
        private var peakStartSum: Float = 0f
        private var peakEndSum: Float = 0f
        private var avgData = PowerDistribution()

        private val lock = Any()

        override val size: Int
            get() = data.size

        override fun add(value: PowerDistribution) {
            synchronized(lock) {
                val wrapper = DataPointWrapper(value)
                data.addLast(wrapper)
                startSum += value.startAngle
                endSum += value.endAngle
                peakStartSum += value.peakStartAngle
                peakEndSum += value.peakEndAngle

                val expireTime = wrapper.timeMillis - windowDurationsMs
                while (data.isNotEmpty() && data.first().timeMillis < expireTime) {
                    val removed = data.removeFirst()
                    startSum -= removed.data.startAngle
                    endSum -= removed.data.endAngle
                    peakStartSum -= removed.data.peakStartAngle
                    peakEndSum -= removed.data.peakEndAngle
                }

                avgData = PowerDistribution(
                    startSum / data.size,
                    endSum / data.size,
                    peakStartSum / data.size,
                    peakEndSum / data.size
                )
            }
        }

        override fun snapshot(): List<PowerDistribution> {
            return data.map { it.data }
        }

        override fun average(): PowerDistribution {
            return avgData
        }

        private data class DataPointWrapper(
            val data: PowerDistribution,
            val timeMillis: Long = System.currentTimeMillis()
        )
    }
}