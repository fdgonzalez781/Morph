package com.morph.engine.script

import com.morph.engine.core.Game
import com.morph.engine.entities.Component
import com.morph.engine.entities.Entity
import com.morph.engine.util.ScriptUtils
import java.util.*

/**
 * Created on 7/5/2017.
 */
class ScriptContainer(private val game: Game) : Component() {
    private val behaviors: HashMap<String, EntityBehavior> = HashMap()

    fun addBehavior(filename: String) {
        ScriptUtils.getScriptBehaviorAsync(filename).subscribe { behavior ->
            val eBehavior = behavior as EntityBehavior
            eBehavior.setGame(game)
            eBehavior.self = parent
            behaviors[filename] = eBehavior
            eBehavior.init()
            eBehavior.start()
        }
    }

    fun replaceBehavior(filename: String, newBehavior: EntityBehavior) {
        newBehavior.setGame(game)
        newBehavior.self = parent
        behaviors.replace(filename, newBehavior)
        newBehavior.start()
    }

    fun removeBehavior(filename: String) {
        val behavior = behaviors[filename]
        behaviors.remove(filename)
        behavior?.destroy()
    }

    fun getBehaviors(): List<EntityBehavior> {
        return ArrayList<EntityBehavior>(behaviors.values)
    }
}
