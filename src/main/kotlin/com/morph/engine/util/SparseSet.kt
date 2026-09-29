package com.morph.engine.util

/**
 * Created on 7/7/2025.
 */
class SparseSet<T> {
    private val sparse = mutableListOf<Int?>()
    private val dense = mutableListOf<T>()

    operator fun set(id: Int, value: T): T? {
        return sparse[id]?.let { index -> // If entity id already has value in dense array
            val prev = dense[index]
            dense[index] = value
            prev
        } ?: run { // If entity id has no value in dense array
            sparse[id] = dense.size
            dense.add(value)
            null
        }
    }

    fun remove(key: Int): T? {
        return sparse[key]?.let { index ->
            val last = dense.size - 1
            val lastContents = dense[last]
            dense[last] = dense[index]
            dense[index] = lastContents
            sparse[key] = null
            sparse[last] = index
            dense.removeLast()
        }
    }

    fun clear() {
        sparse.clear()
        dense.clear()
    }

    val size: Int
        get() = dense.size

    fun isEmpty(): Boolean = dense.isEmpty()

    operator fun get(key: Int): T? {
        return sparse[key]?.let { dense[it] }
    }
}