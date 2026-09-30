package com.morph.engine.math

import java.util.function.Function

class Vector3f {
    var x: Float = 0.toFloat()
    var y: Float = 0.toFloat()
    var z: Float = 0.toFloat()

    val xy: Vector2f
        get() = Vector2f(x, y)

    val length: Float
        get() = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()

    constructor(x: Float, y: Float, z: Float) {
        this.x = x
        this.y = y
        this.z = z
    }

    /**
     * Clones a Vector3f.
     */
    constructor(v: Vector3f) : this(v.x, v.y, v.z) {}

    /**
     * Creates a new Vector3f from a Vector2f and a supplied z value.
     * @param v Vector2f containing new x and y values.
     */
    @JvmOverloads constructor(v: Vector2f, z: Float = 0f) : this(v.x, v.y, z) {}

    constructor() {
        this.x = 0f
        this.y = 0f
        this.z = 0f
    }

    override fun toString(): String {
        return "Vector3f($x, $y, $z)"
    }

    fun toArray(): FloatArray {
        return floatArrayOf(x, y, z);
    }

    fun toList(): List<Float> {
        return listOf(x, y, z);
    }

    fun asTranslationMatrix(): Matrix4f {
        return Matrix4f(
                1f, 0f, 0f, x,
                0f, 1f, 0f, y,
                0f, 0f, 1f, z,
                0f, 0f, 0f, 1f
        )
    }

    fun asScaleMatrix(): Matrix4f {
        return Matrix4f(
                x, 0f, 0f, 0f,
                0f, y, 0f, 0f,
                0f, 0f, z, 0f,
                0f, 0f, 0f, 1f
        )
    }

    @JvmName("add")
    operator fun plus(v: Vector3f): Vector3f {
        return Vector3f(x + v.x, y + v.y, z + v.z)
    }

    @JvmName("sub")
    operator fun minus(v: Vector3f): Vector3f {
        return Vector3f(x - v.x, y - v.y, z - v.z)
    }

    @JvmName("scale")
    operator fun times(k: Float): Vector3f {
        return Vector3f(x * k, y * k, z * k)
    }

    @JvmName("invScale")
    operator fun div(k: Float): Vector3f {
        return Vector3f(x / k, y / k, z / k)
    }

    infix fun cross(v: Vector3f): Vector3f {
        return Vector3f(y * v.z - z * v.y, z * v.x - x * v.z, x * v.y - y * v.x)
    }

    infix fun dot(v: Vector3f): Float {
        return x * v.x + y * v.y + z * v.z
    }

    operator fun set(x: Float, y: Float, z: Float) {
        this.x = x
        this.y = y
        this.z = z
    }

    fun set(v: Vector3f) {
        this.x = v.x
        this.y = v.y
        this.z = v.z
    }

    @JvmName("negate")
    operator fun unaryMinus(): Vector3f {
        return Vector3f(-x, -y, -z)
    }

    fun map(`fun`: Function<Float, Float>): Vector3f {
        return Vector3f(`fun`.apply(x), `fun`.apply(y), `fun`.apply(z))
    }

    companion object {
        fun reflect(n: Vector3f, v: Vector3f): Vector3f {
            return v + (n * (2 * (v dot n)))
        }
    }
}
