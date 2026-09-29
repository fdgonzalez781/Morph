package com.morph.engine.graphics.components

import com.morph.engine.graphics.Color
import com.morph.engine.graphics.LoadedFont.Companion.CHARSET
import com.morph.engine.math.MathUtils.clamp
import com.morph.engine.graphics.shaders.Shader
import com.morph.engine.graphics.LoadedFont
import com.morph.engine.graphics.components.RenderData
import com.morph.engine.math.Vector2f
import com.morph.engine.graphics.LoadedCharacter
import java.util.stream.IntStream
import java.util.function.IntUnaryOperator
import java.nio.CharBuffer

// TODO: Migrate to Kotlin
/**
 * Created on 7/30/2017.
 */
class TextRenderData(shader: Shader<*>?, text: String, font: LoadedFont, color: Color?) :
    RenderData(shader, font.textureAtlas) {
    private val font: LoadedFont
    private var text = ""
    private var cursorPosition: Vector2f
    private var previousPointLength = 0
    private var charCursorPosition = 0
    var width = 0f
        private set
    var height = 0f
        private set
    private val numLines = 0

    init {
        cursorPosition = Vector2f(0f, 0f)
        this.font = font
        setTint(color)
        if (text != "") addString(text)
    }

    fun loadCharacter(c: Char) {
        if (c == '\n') {
            newLine()
            return
        }
        if (c == '\r') return
        if (!CHARSET.contains(c.toString())) return
        val charData = font.getCharacter(c)
        val offsetData = charData!!.offsetData
        val texCoords = charData.texCoords
        var kernAdvance = 0f
        if (text.length >= 1) kernAdvance = font.kerningLookup(text.codePointAt(text.length - 1).toChar(), c)
        cursorPosition = cursorPosition.plus(Vector2f(kernAdvance, 0f))
        val offsetMin = Vector2f(offsetData[0], -offsetData[3])
        val offsetMax = Vector2f(offsetData[2], -offsetData[1])
        addVertex(cursorPosition.plus(offsetMin), texCoords[3])
        addVertex(cursorPosition.plus(Vector2f(offsetMin.x, offsetMax.y)), texCoords[0])
        addVertex(cursorPosition.plus(offsetMax), texCoords[1])
        addVertex(cursorPosition.plus(Vector2f(offsetMax.x, offsetMin.y)), texCoords[2])
        addIndices(*intArrayOf(0, 1, 3, 1, 2, 3).map { i: Int -> i + previousPointLength }.toIntArray())
        previousPointLength += 4
        text += c
        cursorPosition = cursorPosition.plus(Vector2f(charData.xAdvance * font.scale, 0f))
        charCursorPosition++
        width += charData.xAdvance * font.scale + kernAdvance
        height = if (numLines <= 1) font.scale else font.scale + font.getYAdvance() * numLines
    }

    fun addCharacter(c: Char) {
        updateAll { data: RenderData? -> loadCharacter(c) }
    }

    fun newLine() {
        cursorPosition.x = 0f
        cursorPosition.y = cursorPosition.y - font.getYAdvance() * font.scale
        text += "\n"
    }

    fun loadString(text: String) {
        CharBuffer.wrap(text.toCharArray()).chars().mapToObj { c: Int -> c.toChar() }
            .forEach { c: Char -> loadCharacter(c) }
    }

    fun addString(text: String) {
        updateAll { data: RenderData? -> loadString(text) }
    }

    fun setText(text: String) {
        clearText()
        addString(text)
    }

    fun moveCursor(pos: Int) {
        charCursorPosition = clamp(pos, 0, text.length - 1)
    }

    fun removeCharacter(index: Int) {
        if (index >= text.length) {
            System.err.println("Attempt to remove character beyond string length")
            return
        }
        updateIndices { data: RenderData? -> for (i in 0..5) removeIndexAtPosition(index * 6) }
        updateVertices { data: RenderData? -> for (i in 0..3) removeVertexAtPosition(index * 4) }
        val charData = font.getCharacter(text[index])
        charCursorPosition--
        previousPointLength -= 4
        cursorPosition = cursorPosition.minus(Vector2f(charData!!.xAdvance * font.scale, 0f))
        text = text.substring(0, index) + text.substring(index + 1)
    }

    fun removeCharacter(c: Char) {
        updateIndices { data: RenderData? ->
            if (text.indexOf(c) == -1) System.err.println("Character not present in text") else removeCharacter(
                text.indexOf(c)
            )
        }
    }

    fun removeCharacter() {
        updateIndices { data: RenderData? ->
            if (text.length == 0) System.err.println("Text is empty") else removeCharacter(
                text.length - 1
            )
        }
    }

    fun clearText() {
        updateAll { data: RenderData ->
            data.getVertices().clear()
            data.getIndices().clear()
        }
        text = ""
        cursorPosition = Vector2f(0f, 0f)
        charCursorPosition = 0
        previousPointLength = 0
        width = 0f
        height = 0f
    }
}