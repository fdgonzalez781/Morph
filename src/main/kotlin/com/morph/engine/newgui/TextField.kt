package com.morph.engine.newgui

import com.morph.engine.graphics.Color
import com.morph.engine.input.KeyPress
import com.morph.engine.input.KeyRepeat
import com.morph.engine.input.StdKeyEvent
import com.morph.engine.math.Vector2f
import org.lwjgl.glfw.GLFW.*
import java.util.*

/**
 * Created on 8/2/2017.
 */
open class TextField @JvmOverloads constructor(
        name: String,
        text: String,
        font: String,
        size: Int,
        color: Color = Color(0f, 0f, 0f),
        position: Vector2f,
        depth: Int = 0
) : TextElement(name, text, font, size, position, color, depth) {
    fun replaceText(text: String) {
        this.text = text
        renderData.setText(text)
    }

    fun addCharacter(c: Char) {
        this.text += c
        renderData.addCharacter(c)
    }

    fun removeCharacter() {
        if (text.isNotEmpty()) {
            this.text = text.substring(0, text.length - 1)
            renderData.removeCharacter()
        } else
            System.err.println("Attempt to remove character from empty string")
    }

    fun clearText() {
        renderData.clearText()
        this.text = ""
    }

    fun addString(s: String) {
        this.text += s
        renderData.addString(s)
    }

    open fun handleGUIKeyEvent(e: StdKeyEvent) {
        when(e.action) {
            is KeyPress -> when {
                e.key == GLFW_KEY_BACKSPACE -> removeCharacter()
                e.key == GLFW_KEY_ESCAPE -> clearText()
                isLegalCharacter(e.key) -> addCharacter(getCharFromKeyData(e.key, e.hasMod(GLFW_MOD_SHIFT)))
            }
            is KeyRepeat ->
                if (e.key == GLFW_KEY_BACKSPACE) removeCharacter()
                else if (isLegalCharacter(e.key)) addCharacter(getCharFromKeyData(e.key, e.hasMod(GLFW_MOD_SHIFT)))

            else -> {}
        }
    }

    private fun isLegalCharacter(c: Int): Boolean {
        return c != GLFW_KEY_LEFT_SHIFT && c != GLFW_KEY_RIGHT_SHIFT
    }

    /*
    39 34
    44 60
    45 95
    46 62
    47 63
    48 41
    49 33
    50 64
    51 35
    52 36
    53 37
    54 94
    55 38
    56 42
    57 40
    59 58
    61 43
    91 123
    92 124
    93 125
    96 126
    97-122 map to uppercase
     */
    protected fun getCharFromKeyData(keycode: Int, shift: Boolean): Char {
        var key = keycode

        val isAlphabetical = keycode in 65..90

        if (isAlphabetical) key += 32

        if (shift) {
            return shiftMap[key]?.toChar() ?: ' '
        }

        return key.toChar()
    }

    companion object {
        private val shiftMap = HashMap<Int, Int>()

        init {
            val keys = sequenceOf(39, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 59, 61, 91, 92, 93, 96) + (97..122).asSequence()
            val shiftKeys = sequenceOf(34, 60, 95, 62, 63, 41, 33, 64, 35, 36, 37, 94, 38, 42, 40, 58, 43, 123, 124, 125, 126) + (65..90).asSequence()
            keys.zip(shiftKeys).forEach { shiftMap[it.first] = it.second }
        }
    }
}
