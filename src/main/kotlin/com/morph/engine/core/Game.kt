package com.morph.engine.core

import com.morph.engine.core.gui.ConsoleGUI
import com.morph.engine.entities.EntityFactory
import com.morph.engine.graphics.GLDisplay
import com.morph.engine.graphics.GLRenderingEngine
import com.morph.engine.input.InputMapping
import com.morph.engine.input.KeyPress
import com.morph.engine.input.Keyboard
import com.morph.engine.input.Mouse
import com.morph.engine.newgui.Container
import com.morph.engine.newgui.Element
import com.morph.engine.newgui.GUI
import com.morph.engine.script.GameBehavior
import com.morph.engine.script.ScriptSystem
import com.morph.engine.script.debug.Console
import com.morph.engine.util.ScriptUtils
import org.lwjgl.glfw.GLFW

import java.util.ArrayList
import java.util.HashMap

abstract class Game(
        val width: Int = 800,
        val height: Int = 600,
        protected var title: String,
        val fps: Float,
        protected var fullscreen: Boolean
) {
    @Volatile
    private var isRunning = false
    val timestep: Float = 1.0f / fps
    final var world: World = World(this)
//    var world: World? = null
//        set(nextWorld) {
//            if (world != null) {
//                world!!.entities.forEach { renderingEngine.unregister(it) }
//                world!!.destroy()
//            }
//
//            nextWorld!!.init()
//            nextWorld.entities.forEach { renderingEngine.register(it) }
//            field = nextWorld
//        }

    // TODO: Game is a god class, consider moving below fields to different classes (behaviors to camera)

    protected var behaviors = HashMap<String, GameBehavior>()

    protected lateinit var display: GLDisplay
    lateinit var renderingEngine: GLRenderingEngine

    protected var guiElements: MutableList<Element> = ArrayList()
    private val guis = ArrayList<GUI>()

    val console: Console
    private val consoleGUI: ConsoleGUI

    var camera: Camera = Camera.Identity
        set(camera) {
            field = camera
            renderingEngine.setCamera(camera)
        }

    private var delta: Long = 0

    var inputMapping: InputMapping? = null

    val actualFPS: Double
        get() = 1.0 / delta * 1000000000.0

    val isConsoleOpen: Boolean
        get() = consoleGUI.isOpen

    init {
        this.console = Console(Console.ScriptType.KOTLIN, this)
        this.consoleGUI = ConsoleGUI(this, console, width, height)
        this.inputMapping = InputMapping()
        EntityFactory.world = this.world
    }

    fun start() {
        if (isRunning)
            return

        isRunning = true

        init()

        var currentTime = System.nanoTime()
        var accumulator = 0.0
        while (isRunning) {
            preUpdate()

            pollEvents()

            handleInput()
            update()

            val newTime = System.nanoTime()
            val frameTime = (newTime - currentTime) / 1000000000.0
            currentTime = newTime

            delta = newTime - currentTime

            accumulator += frameTime

            while (accumulator >= timestep) {
                fixedUpdate(timestep)
                accumulator -= timestep.toDouble()
            }

            render()
            postUpdate()
        }

        destroy()
    }

    private fun preUpdate() {
        Keyboard.clear()
        Mouse.clear()

        preGameUpdate()

        world.preUpdate()

        guis.forEach { it.preUpdate() }
        behaviors.values.forEach { it.preUpdate() }
    }

    private fun postUpdate() {
        postGameUpdate()

        world.postUpdate()

        guis.forEach { it.postUpdate() }
        behaviors.values.forEach { it.postUpdate() }
    }

    protected fun destroy() {
        display.destroy()
        ScriptUtils.stop()

        System.exit(0)
    }

    private fun init() {
        ScriptUtils.init(this)

        display = GLDisplay(width, height, title)
//        renderingEngine = GLRenderingEngine(this)
        println(world)
        renderingEngine = world.addSystem { GLRenderingEngine(it, width, height) } as GLRenderingEngine
        val scriptSystem = world.addSystem { ScriptSystem(it) }

//        addSystem(renderingEngine)
//        addSystem(scriptSystem)

        display.init(this)
        display.show()

        if (fullscreen)
            display.setFullscreen(width, height)

        initGame()

        world.initSystems()

        consoleGUI.init()

        // TODO: Oh my god please move this somewhere else this is evil code
        //		Observable.combineLatest(
        //		        events.filter(e -> e == GameAction.UPDATE),
        //                Observable.concat(Observable.just(new StdMouseEvent(MouseRelease.INSTANCE, 0, 0)), Mouse.getStandardMouseEvents()),
        //                Mouse.INSTANCE.getScreenMousePosition(),
        //                Triple::new).subscribe(vals -> {
        //                    GameAction g = vals.getFirst();
        //                    StdMouseEvent m = vals.getSecond();
        //                    Vector2f mousePos = vals.getThird();
        //
        //                    if (m.getButton() == 0 && m.getAction() == MousePress.INSTANCE)
        //                        System.out.println("pressed");
        //
        //                    for (GUI gui : guis) {
        //                        for (Element e : gui.getElements()) {
        //                            switch (e.getState()) {
        //                                case "IDLE":
        //                                    if (mousePos != null && e.contains(mousePos)) {
        //                                        if (m.getButton() == 0 && m.getAction() == MousePress.INSTANCE) {
        //                                            e.setState("CLICK");
        //                                        } else {
        //                                            e.setState("HOVER");
        //                                        }
        //                                    }
        //                                    break;
        //                                case "HOVER":
        //                                    if (mousePos != null && e.contains(mousePos)) {
        //                                        if (m.getButton() == 0 && m.getAction() == MousePress.INSTANCE) {
        //                                            e.setState("CLICK");
        //                                        }
        //                                    } else {
        //                                        e.setState("IDLE");
        //                                    }
        //                                    break;
        //                                case "CLICK":
        //                                    if (m.getButton() == 0 && m.getAction() == MouseRelease.INSTANCE) {
        //                                        if (mousePos != null && e.contains(mousePos))
        //                                            e.setState("HOVER");
        //                                        else
        //                                            e.setState("IDLE");
        //                                    }
        //                                    break;
        //                            }
        //                        }
        //                    }
        //                }
        //        );
    }

    private fun update() {
        inputMapping!!.update()

        world.update()

        guis.forEach(GUI::update)
        behaviors.values.forEach(GameBehavior::update)

        Keyboard.standardKeyEvents.forEach { e -> if (e.action === KeyPress && e.key == GLFW.GLFW_KEY_GRAVE_ACCENT) toggleConsole() }
    }

    protected fun pollEvents() {
        display.pollEvents()
    }

    fun stop() {
        if (!isRunning)
            return

        isRunning = false
    }

    fun run() {
        start()
    }

    fun addElement(e: Element) {
        guiElements.add(e)
        renderingEngine.register(e)
        if (e is Container) {
            e.getChildren(true).forEach{ this.addElement(it) }
        }
    }

    fun removeElement(e: Element) {
        guiElements.remove(e)
        renderingEngine.unregister(e)
        if (e is Container) {
            e.getChildren(true).forEach { this.removeElement(it) }
        }
    }

    fun addElements(e: List<Element>) {
        e.forEach{ this.addElement(it) }
    }

    fun removeElements(e: List<Element>) {
        e.forEach{ this.removeElement(it) }
    }

    fun addGUI(gui: GUI) {
        guis.add(gui)
        gui.load()
        addElements(gui.getElements())
        gui.open()
    }

    fun removeGUI(gui: GUI) {
        guis.remove(gui)
        gui.unload()
        removeElements(gui.getElements())
        gui.close()
    }

    fun fixedUpdate(dt: Float) {
        fixedGameUpdate(dt)

        world.fixedUpdate(dt)

        guis.forEach { gui -> gui.fixedUpdate(dt) }

        behaviors.values.forEach { b -> b.fixedUpdate(dt) }
    }

    fun attachBehaviorAsync(filename: String) {
        ScriptUtils.getScriptBehaviorAsync(filename).subscribe { behavior ->
            behavior.setGame(this)
            behaviors[filename] = behavior
            behavior.init()
            behavior.start()
        }
    }

    fun replaceBehavior(filename: String, newBehavior: GameBehavior) {
        println("Behavior $filename has been modified.")
        newBehavior.setGame(this)
        behaviors.replace(filename, newBehavior)
        newBehavior.start()
    }

    fun loadScene(scene: Scene) {
        world.addScene(scene)
    }

    fun unloadScene(scene: Scene) {
        world.removeScene(scene)
    }

    abstract fun initGame()
    abstract fun preGameUpdate()
    abstract fun fixedGameUpdate(dt: Float)
    abstract fun postGameUpdate()
    abstract fun handleInput()

    fun render() {
        renderingEngine.render(display)
    }

    fun openConsole() {
        addGUI(consoleGUI)
    }

    fun closeConsole() {
        removeGUI(consoleGUI)
    }

    fun toggleConsole() {
        if (consoleGUI.isOpen)
            closeConsole()
        else
            openConsole()
    }

    fun handleExitEvent() {
        println("Morph is closing...")
        stop()
    }

    companion object {

        const val VERSION_MAJOR = 0
        const val VERSION_MINOR = 8
        const val VERSION_PATCH = 6

        const val VERSION_STRING = "$VERSION_MAJOR.$VERSION_MINOR.$VERSION_PATCH"
    }
}
