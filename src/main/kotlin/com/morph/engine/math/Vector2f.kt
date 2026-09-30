package com.morph.engine.math

data class Vector2f(var x: Float = 0f, var y: Float = 0f) {
    val length: Float
        get() = Math.sqrt((x * x + y * y).toDouble()).toFloat()

    /**
     * Clones a Vector2f.
     */
    constructor(v: Vector2f) : this(v.x, v.y)

    override fun toString(): String {
        return "Vector2f($x, $y)"
    }

    @JvmName("add")
    operator fun plus(v: Vector2f): Vector2f {
        return Vector2f(x + v.x, y + v.y)
    }

    @JvmName("sub")
    operator fun minus(v: Vector2f): Vector2f {
        return Vector2f(x - v.x, y - v.y)
    }

    @JvmName("mul")
    operator fun times(v: Vector2f): Vector2f {
        return Vector2f(x * v.x, y * v.y)
    }

    operator fun div(v: Vector2f): Vector2f {
        return Vector2f(x / v.x, y / v.y)
    }

    @JvmName("scale")
    operator fun times(k: Float): Vector2f {
        return Vector2f(x * k, y * k)
    }

    @JvmName("invScale")
    operator fun div(k: Float): Vector2f {
        return Vector2f(x / k, y / k)
    }

    fun normalize(): Vector2f {
        val length = length
        return this / length
    }

    fun abs(): Vector2f {
        return Vector2f(Math.abs(x), Math.abs(y))
    }

    operator fun set(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    fun set(v: Vector2f) {
        this.x = v.x
        this.y = v.y
    }

    fun asTranslationMatrix(): Matrix4f {
        return Matrix4f(
                1f, 0f, 0f, x,
                0f, 1f, 0f, y,
                0f, 0f, 1f, 0f,
                0f, 0f, 0f, 1f
        )
    }

    fun asScaleMatrix(): Matrix4f {
        return Matrix4f(
                x, 0f, 0f, 0f,
                0f, y, 0f, 0f,
                0f, 0f, 1f, 0f,
                0f, 0f, 0f, 1f
        )
    }

    infix fun cross(v: Vector2f): Float {
        return x * v.y - y * v.x
    }

    fun clear() {
        this.x = 0f
        this.y = 0f
    }

    @JvmName("negate")
    operator fun unaryMinus(): Vector2f {
        return Vector2f(-x, -y)
    }

    infix fun dot(v: Vector2f): Float {
        return x * v.x + y * v.y
    }

    fun map(f: (Float) -> Float) = Vector2f(f(x), f(y))

    fun toList(): List<Float> {
        return listOf(x, y)
    }

    companion object {
        fun reflect(n: Vector2f, v: Vector2f): Vector2f {
            return v + (n * (-2 * (v dot n)))
        }
    }
}
