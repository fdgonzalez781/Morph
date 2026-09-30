package com.morph.engine.graphics

import com.morph.engine.core.Game
import com.morph.engine.input.Keyboard
import com.morph.engine.input.Mouse
import com.morph.engine.input.Mouse.setMousePosition
import com.morph.engine.math.Vector2f
import org.lwjgl.glfw.*
import org.lwjgl.opengl.GL
import org.lwjgl.system.MemoryUtil

// TODO: Generalize to platform agnostic version
class GLDisplay(private val width: Int, private val height: Int, private val title: String) {
    var window: Long = 0
        private set

    fun init(game: Game) {
        GLFWErrorCallback.createPrint(System.err).set()
        check(GLFW.glfwInit()) { "Failed to initialize GLFW" }

        GLFW.glfwDefaultWindowHints()
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE)
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE)

        window = GLFW.glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL)
        if (window == MemoryUtil.NULL) throw RuntimeException("Failure to create the GLFW window")

        GLFW.glfwSetKeyCallback(window, Keyboard::handleKeyEvent)

        GLFW.glfwSetCursorPosCallback(
            window
        ) { window: Long, x: Double, y: Double ->
            setMousePosition(
                window,
                Vector2f(x.toFloat(), y.toFloat()),
                game.camera
            )
        }

        GLFW.glfwSetMouseButtonCallback(window, Mouse::handleMouseEvent)

        GLFW.glfwSetWindowCloseCallback(window) { game.handleExitEvent() }

        val vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())

        GLFW.glfwSetWindowPos(window, (vidmode!!.width() - width) / 2, (vidmode.height() - height) / 2)

        GLFW.glfwMakeContextCurrent(window)
        GLFW.glfwSwapInterval(1)

        GL.createCapabilities()
    }

    fun update() {
        GLFW.glfwSwapBuffers(window)
    }

    fun pollEvents() {
        GLFW.glfwPollEvents()
    }

    fun destroy() {
        GLFW.glfwTerminate()
        GLFW.glfwSetErrorCallback(null)!!.free()
    }

    fun setFullscreen(resX: Int, resY: Int) {
        val newWindow = GLFW.glfwCreateWindow(resX, resY, title, GLFW.glfwGetPrimaryMonitor(), window)
        GLFW.glfwDestroyWindow(window)
        this.window = newWindow
    }

    fun show() {
        GLFW.glfwShowWindow(window)
    }

    fun enableVSync() {
        GLFW.glfwSwapInterval(1)
    }

    fun setTitle(title: String) {
        GLFW.glfwSetWindowTitle(window, title)
    }

    fun setSize(width: Int, height: Int) {
        GLFW.glfwSetWindowSize(window, width, height)
    }
}
