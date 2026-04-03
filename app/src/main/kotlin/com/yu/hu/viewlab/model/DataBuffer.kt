package com.yu.hu.viewlab.model

/**
 * 数据缓冲
 *
 * huyu create
 * 2026/4/2 16:05
 */
interface DataBuffer<T> {

    val size: Int

    /**
     * 添加数据
     */
    fun add(value: T)

    /**
     * 数据快照
     */
    fun snapshot(): List<T>
}