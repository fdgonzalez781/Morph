package com.morph.engine.math

class Matrix4f(
        m00: Float, m01: Float, m02: Float, m03: Float,
        m10: Float, m11: Float, m12: Float, m13: Float,
        m20: Float, m21: Float, m22: Float, m23: Float,
        m30: Float, m31: Float, m32: Float, m33: Float
) {
    val values: FloatArray = floatArrayOf(
            m00, m01, m02, m03,
            m10, m11, m12, m13,
            m20, m21, m22, m23,
            m30, m31, m32, m33
    )

    val transpose: Matrix4f
        get() = Matrix4f(
                values[0], values[4], values[8], values[12],
                values[1], values[5], values[9], values[13],
                values[2], values[6], values[10], values[14],
                values[3], values[7], values[11], values[15]
        )

    val inverse: Matrix4f
        get() {
            return computeGaussJordan()
        }

    val determinant: Float
        get() {
            val (reduction, swaps) = this.toRowEchelonForm()
            val power = (sequenceOf(1) + generateSequence { -1 }).take(swaps + 1).reduce {a, b -> a * b}
            return power * reduction[0, 0] * reduction[1, 1] * reduction[2, 2] * reduction[3, 3]
        }

    constructor(m: FloatArray) : this(
            m[0], m[1], m[2], m[3],
            m[4], m[5], m[6], m[7],
            m[8], m[9], m[10], m[11],
            m[12], m[13], m[14], m[15]
    )

    override fun toString(): String {
        val result = StringBuilder("Matrix4f(")
        for (i in 0..14) {
            result.append(values[i]).append(", ")
            if (i % 4 == 3)
                result.append("\n         ")
        }
        result.append(values[15]).append(")")

        return result.toString()
    }

    operator fun get(i: Int, j: Int): Float {
        return values[j + (i * 4)]
    }

    operator fun set(i: Int, j: Int, value: Float) {
        this.values[j + (i * 4)] = value
    }

    fun asTranslationVector(): Vector3f {
        return Vector3f(values[3], values[7], values[11])
    }

    fun as2DRotation(): Float {
        val cos = values[0]
        val sin = values[4]
        return Math.atan2(sin.toDouble(), cos.toDouble()).toFloat()
    }

    fun asScaleVector(): Vector3f {
        return Vector3f(values[0], values[5], values[10])
    }

    @JvmName("mul")
    operator fun times(other: Matrix4f): Matrix4f {
        val result = Matrix4f.empty

        for (i in 0..3) {
            for (j in 0..3) {
                result[i, j] = this[i, 0] * other[0, j] +
                        this[i, 1] * other[1, j] +
                        this[i, 2] * other[2, j] +
                        this[i, 3] * other[3, j]
            }
        }

        return result
    }

    @JvmName("mul")
    operator fun times(v: Vector4f) = Vector4f(
            x = this[0, 0] * v.x + this[0, 1] * v.y + this[0, 2] * v.z + this[0, 3] * v.w,
            y = this[1, 0] * v.x + this[1, 1] * v.y + this[1, 2] * v.z + this[1, 3] * v.w,
            z = this[2, 0] * v.x + this[2, 1] * v.y + this[2, 2] * v.z + this[2, 3] * v.w,
            w = this[3, 0] * v.x + this[3, 1] * v.y + this[3, 2] * v.z + this[3, 3] * v.w
    )

    fun toArray(): FloatArray {
        return values
    }

    @JvmName("mul")
    operator fun times(v: Vector2f): Vector2f {
        return times(Vector4f(v, 0f, 1f)).xy
    }

    @JvmName("mul")
    operator fun times(v: Vector3f): Vector3f {
        return times(Vector4f(v, 1f)).xyz
    }

    @JvmName("scale")
    operator fun times(k: Float): Matrix4f {
        val values = FloatArray(16)

        for (i in 0..15) {
            values[i] = this.values[i] * k
        }

        return Matrix4f(values)
    }

    fun swapRows(a: Int, b: Int) : Matrix4f {
        require(a < 4 && b < 4) { "Attempt to swap rows outside 0-3" }
        val rows = values.asList().chunked(4).toMutableList()
        val temp = rows[a]
        rows[a] = rows[b]
        rows[b] = temp
        return Matrix4f(rows.flatten().toFloatArray())
    }

    fun scaleRow(a: Int, k: Float) : Matrix4f {
        val result = Matrix4f(this.values)
        result[a, 0] *= k
        result[a, 1] *= k
        result[a, 2] *= k
        result[a, 3] *= k
        return result
    }

    fun addScaledRow(unscaledRow: Int, scaleFactor: Float, scaledRow: Int) : Matrix4f {
        val rowAdded = Vector4f(this[scaledRow, 0], this[scaledRow, 1], this[scaledRow, 2], this[scaledRow, 3]) * scaleFactor
        val rowKept = Vector4f(this[unscaledRow, 0], this[unscaledRow, 1], this[unscaledRow, 2], this[unscaledRow, 3])
        val newRow = rowAdded + rowKept
        val result = Matrix4f(this.values)
        result[unscaledRow, 0] = newRow.x
        result[unscaledRow, 1] = newRow.y
        result[unscaledRow, 2] = newRow.z
        result[unscaledRow, 3] = newRow.w
        return result
    }

    fun computeGaussJordan() : Matrix4f {
        var result = Matrix4f(this.values)
        var inverse = Matrix4f.identity

        if (Math.abs(this.determinant) == 0f) return Matrix4f.empty

        fun swap(a: Int, b: Int) {
            result = result.swapRows(a, b)
            inverse = inverse.swapRows(a, b)
        }

        fun scale(a: Int, k: Float) {
            result = result.scaleRow(a, k)
            inverse = inverse.scaleRow(a, k)
        }

        fun addScaled(a: Int, b: Int, k: Float) {
            result = result.addScaledRow(a, k, b)
            inverse = inverse.addScaledRow(a, k, b)
        }

        var row = 0
        var col = 0
        while (row <= 3 && col <= 3) {
            val max = (row..3).maxBy { Math.abs(result[it, col]) }!!
            if (result[max, col] == 0f) {
                col++
            } else {
                swap(row, max)
                for (i in (row + 1)..3) {
                    val k = result[i, col] / result[row, col]
                    addScaled(i, row, -k)
                }

                row++
                col++
            }
        }

        if (result[3, 3] != 0f) {
            addScaled(2, 3,  -result[2, 3] * (1f / result[3, 3]))
        }

        if (result[2, 2] != 0f) {
            addScaled(1, 2, -result[1, 2] * (1f / result[2, 2]))
        }

        if (result[3, 3] != 0f) {
            addScaled(1, 3, -result[1, 3] * (1f / result[3, 3]))
        }

        if (result[1, 1] != 0f) {
            addScaled(0, 1, -result[0, 1] * (1f / result[1, 1]))
        }

        if (result[2, 2] != 0f) {
            addScaled(0, 2, -result[0, 2] * (1f / result[2, 2]))
        }

        if (result[3, 3] != 0f) {
            addScaled(0, 3, -result[0, 3] * (1f / result[3, 3]))
        }

        if (result[0, 0] != 0f) scale(0, 1f / result[0, 0])
        if (result[1, 1] != 0f) scale(1, 1f / result[1, 1])
        if (result[2, 2] != 0f) scale(2, 1f / result[2, 2])
        if (result[3, 3] != 0f) scale(3, 1f / result[3, 3])

        return inverse
    }

    fun toRowEchelonForm() : Pair<Matrix4f, Int> {
        var result = Matrix4f(this.values)
        val pivot1 = sequenceOf(0, 1, 2, 3).filter { result[it, 0] != 0f }.minOrNull()
        var swaps = 0

        pivot1?.let {
            if (it != 0) {
                result = result.swapRows(it, 0)
                swaps++
            }
            for (i in 1..3) {
                if (result[i, 0] != 0f) {
                    result = result.addScaledRow(i, -result[i, 0] / result[0, 0], 0)
                }
            }
        }

        val pivot2 = sequenceOf(1, 2, 3).filter { result[it, 1] != 0f }.minOrNull()
        pivot2?.let {
            if (it != 1) {
                result = result.swapRows(it, 1)
                swaps++
            }
            for (i in 2..3) {
                if (result[i, 1] != 0f) {
                    result = result.addScaledRow(i, -result[i, 1] / result[1, 1], 1)
                }
            }
        }

        val pivot3 = sequenceOf(2, 3).filter { result[it, 2] != 0f }.minOrNull()
        pivot3?.let {
            if (it != 2) {
                result = result.swapRows(2, 3)
                swaps++
            }
            if (result[3, 2] != 0f) {
                result = result.addScaledRow(3, -result[3, 2] / result[2, 2], 2)
            }
        }

        return result to swaps
    }

    private fun mulComp(mat: Matrix4f): Matrix4f {
        val values = FloatArray(16)

        for (i in 0..15) {
            values[i] = this.values[i] * mat[i]
        }

        return Matrix4f(values)
    }

    operator fun get(i: Int): Float {
        return values[i]
    }

    fun toDoubleArray(): DoubleArray = values.map { it.toDouble() }.toDoubleArray()

    companion object {
        @JvmStatic
        val identity: Matrix4f
            get() = Matrix4f(
                    1f, 0f, 0f, 0f,
                    0f, 1f, 0f, 0f,
                    0f, 0f, 1f, 0f,
                    0f, 0f, 0f, 1f
            )

        @JvmStatic
        val empty: Matrix4f
            get() = Matrix4f(
                    0f, 0f, 0f, 0f,
                    0f, 0f, 0f, 0f,
                    0f, 0f, 0f, 0f,
                    0f, 0f, 0f, 0f
            )

        @JvmStatic
        fun scaleMatrix(k: Float) = Matrix4f.identity * k

        @JvmStatic
        fun approxIdentity(mat: Matrix4f, epsilon: Float): Boolean = (0..15).all {
            val i = it / 4
            val j = it % 4
            if (i == j) Math.abs(mat[i, j] - 1f) < epsilon else Math.abs(mat[i, j]) < epsilon
        }
    }
}
