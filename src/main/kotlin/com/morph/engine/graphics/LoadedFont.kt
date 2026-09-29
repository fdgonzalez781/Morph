package com.morph.engine.graphics

import com.morph.engine.math.Vector2f
import com.morph.engine.util.IOUtils
import org.lwjgl.BufferUtils
import org.lwjgl.stb.*
import org.lwjgl.system.MemoryStack
import java.io.IOException
import java.nio.CharBuffer

/**
 * Created on 7/30/2017.
 */
class LoadedFont(val fontName: String) {
    lateinit var textureAtlas: Texture
        private set
    private val charIndices: Array<IntArray> = Array(CHARSET_RANGE) { intArrayOf() }
    private var characters: Array<LoadedCharacter?> = arrayOfNulls(CHARSET_RANGE)
    var scale: Float = 0f
        private set
    var ascent: Float = 0f
        private set
    private var yAdvance: Int = 0
    private val size: Int = 0

    private var kerningTable: Array<FloatArray> = Array(CHARSET_RANGE) { FloatArray(CHARSET_RANGE) }

    init {
        try {
            STBTTPackContext.malloc().use { context ->
                STBTTPackedchar.malloc(CHARSET_RANGE).use { packedChars ->
                    val pixels = BufferUtils.createByteBuffer(LoadedFont.BITMAP_WIDTH * LoadedFont.BITMAP_HEIGHT)

                    val fontBuffer = IOUtils.getFileAsByteBuffer(fontName, 160 * 1024)

                    STBTruetype.stbtt_PackSetOversampling(context, 2, 2)

                    STBTruetype.stbtt_PackBegin(context, pixels, LoadedFont.BITMAP_WIDTH, LoadedFont.BITMAP_HEIGHT, 0, 1)
                    STBTruetype.stbtt_PackFontRange(context, fontBuffer, 0, LoadedFont.SIZE.toFloat(), 32, packedChars)

                    STBTruetype.stbtt_PackEnd(context)

                    this.textureAtlas = Texture(fontName, LoadedFont.BITMAP_WIDTH, LoadedFont.BITMAP_HEIGHT, pixels)

                    preloadPackedChars(packedChars)

                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }

    private fun preloadPackedChars(packedChars: STBTTPackedchar.Buffer) {
        try {
            MemoryStack.stackPush().use { stack ->
                val x = stack.floats(0.0f)
                val y = stack.floats(0.0f)
                val q = STBTTAlignedQuad.mallocStack(stack)
                val fontInfo = STBTTFontinfo.mallocStack(stack)
                val fontBuffer = IOUtils.getFileAsByteBuffer(fontName, 160 * 1024)

                val ascent = IntArray(1)
                val descent = IntArray(1)
                val lineGap = IntArray(1)

                STBTruetype.stbtt_InitFont(fontInfo, fontBuffer)

                scale = STBTruetype.stbtt_ScaleForPixelHeight(fontInfo, LoadedFont.SIZE.toFloat())
                STBTruetype.stbtt_GetFontVMetrics(fontInfo, ascent, descent, lineGap)

                yAdvance = ascent[0] - descent[0] + lineGap[0]

                this.ascent = ascent[0] * scale

                for (i in 0 until CHARSET_RANGE) {
                    val c = CHARSET[i]

                    val xAdvance = IntArray(1)
                    val leftSideBearing = IntArray(1)

                    STBTruetype.stbtt_GetPackedQuad(packedChars, LoadedFont.BITMAP_WIDTH, LoadedFont.BITMAP_HEIGHT, i, x, y, q, false)
                    STBTruetype.stbtt_GetCodepointHMetrics(fontInfo, c.code, xAdvance, leftSideBearing)

                    val texCoords = arrayOf(Vector2f(q.s0(), q.t0()), Vector2f(q.s1(), q.t0()), Vector2f(q.s1(), q.t1()), Vector2f(q.s0(), q.t1()))

                    val indices = sequenceOf(0, 1, 3, 1, 2, 3).map { it * 4 }

                    val pc = packedChars.get(i)
                    val offsetData = floatArrayOf(pc.xoff(), pc.yoff(), pc.xoff2(), pc.yoff2())

                    characters[c.code - CHAR_FIRST] = LoadedCharacter(c, texCoords, indices.toList().toTypedArray().toIntArray(), xAdvance[0].toFloat(), offsetData)

                    CharBuffer.wrap(CHARSET.toCharArray()).chars().forEach { c2 -> kerningTable[c.code - CHAR_FIRST][c2 - CHAR_FIRST] = STBTruetype.stbtt_GetCodepointKernAdvance(fontInfo,
                        c.code, c2) * scale }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun kerningLookup(c1: Char, c2: Char): Float {
        return if (c1 == '\n' || c2 == '\n') 0f else kerningTable[c1.code - CHAR_FIRST][c2.code - CHAR_FIRST]

    }

    fun getCharData(c: Char) = charIndices[c.code - CHAR_FIRST]

    fun getCharacter(c: Char) = characters[c.code - CHAR_FIRST]

    fun getYAdvance() = yAdvance.toFloat()

    fun destroy() {
        textureAtlas.destroy()
    }

    companion object {
        private const val BITMAP_WIDTH = 1024
        private const val BITMAP_HEIGHT = 1024
        const val SIZE = 64
        const val CHAR_FIRST = 32
        private const val CHAR_LAST = 128
        const val CHARSET_RANGE = CHAR_LAST - CHAR_FIRST

        @JvmStatic val CHARSET: String = buildString {
            for (i in CHAR_FIRST until CHAR_LAST) {
                append(i.toChar())
            }
        }

        fun isValidChar(c: Char): Boolean {
            return CHARSET.contains(Character.toString(c))
        }
    }
}
