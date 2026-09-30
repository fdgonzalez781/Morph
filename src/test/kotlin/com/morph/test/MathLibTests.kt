package com.morph.test

import com.morph.engine.math.Matrix4f
import java.util.*
import kotlin.test.assertTrue

class MathLibTests {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            val numberOfTests = 500
            val r = Random()
            for (i in 1..numberOfTests) {
                val values = generateSequence { (r.nextInt(11) - 5).toFloat() }.take(16).toList().toFloatArray()
                val matrix = Matrix4f(values)
                val inverse = matrix.inverse
                val result = matrix * inverse
                println("MATRIX: $matrix")
                println("INVERSE: $inverse")
                println("RESULT: $result")
                println("DETERMINANT: ${matrix.determinant}")
                if (matrix.determinant != 0f)
                    assertTrue(
                            Matrix4f.approxIdentity(result, 0.1f) || matrix.determinant < 1e-3f,
                            "Expected either M * M^(-1) = I, or the determinant to be extremely small.\n" +
                                    "Failed on attempt #$i.\n" +
                                    "Determinant: ${matrix.determinant}.\n" +
                                    "Matrix: $matrix\n" +
                                    "Inverse: $inverse\n" +
                                    "Result: $result"
                    )
            }
        }
    }
}