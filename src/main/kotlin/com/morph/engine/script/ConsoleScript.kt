package com.morph.engine.script

import com.morph.engine.core.Game
import com.morph.engine.entities.Component
import com.morph.engine.entities.Entity
import com.morph.engine.script.debug.Console
import com.morph.engine.util.EntityGenUtils

abstract class ConsoleScript : Runnable {
    private var console: Console? = null

    fun setConsole(console: Console) {
        this.console = console
    }

    protected fun echo(message: Any) = Console.out.println(message)

    protected fun getVersion() {
        echo("Morph ${Game.VERSION_STRING}")
    }

    protected fun addEntity(name: String, vararg components: Component) {
        val e = EntityGenUtils.createEntity(console?.getGame()?.world!!, name, *components)
        console?.getGame()?.world?.addEntity(e)
        echo("Created new entity $name (ID #${e.id})")
    }

    protected fun addEntityRectangle(name: String, width: Float, height: Float, isTrigger: Boolean, vararg components: Component) {
        val e = EntityGenUtils.createEntityRectangle(console?.getGame()?.world!!, name, width, height, isTrigger, *components)
        console?.getGame()?.world?.addEntity(e)
        echo("Created new entity $name (ID #${e.id})")
    }

    protected fun getEntity(name: String): Entity? {
        return console?.getGame()?.world?.getEntityByName(name)
    }

    protected fun clear() {
        console?.clear()
    }

    abstract override fun run()
}
