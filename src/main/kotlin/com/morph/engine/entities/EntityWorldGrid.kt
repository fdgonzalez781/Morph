package com.morph.engine.entities

import com.morph.engine.core.Game
import com.morph.engine.core.TileWorld
import com.morph.engine.math.Vector2f
import com.morph.engine.physics.components.Transform2D

/**
 * Created on 6/30/2025.
 */
class EntityWorldGrid(val game: Game, width: Int, height: Int, val world: TileWorld) : EntityGrid(width, height) {
    override fun set(tileX: Int, tileY: Int, e: Entity?): Boolean = if (e == null) {
        removeEntity(tileX, tileY)
    } else {
        setNotNull(tileX, tileY, e)
    }

    fun setNotNull(tileX: Int, tileY: Int, e: Entity): Boolean {
        if (tileX < 0 || tileX >= width || tileY < 0 || tileY >= height)
            return false

        game.renderingEngine.register(e)

        val tmp = this[tileX, tileY]
        if (tmp != null)
            game.renderingEngine.unregister(tmp)

        super.set(tileX, tileY, e)

        e.also {
            given<Transform2D>(e) {
                it.position = Vector2f(world.xOffset + (tileX + 0.5f) * world.tileSize, world.yOffset + height * world.tileSize - (tileY + 0.5f) * world.tileSize)
                it.scale = Vector2f(world.tileSize, world.tileSize)
            }
        }

        return true
    }

    override fun removeEntity(tileX: Int, tileY: Int): Boolean {
        if (tileX + tileY * width >= width * height || this[tileX, tileY] == null)
            return false

        this[tileX, tileY]?.let {
            game.renderingEngine.unregister(it)
        }

//        this[tileX, tileY] = null
        super.removeEntity(tileX, tileY)

        return true
    }

    override fun moveEntity(startX: Int, startY: Int, endX: Int, endY: Int): Boolean {
        if (startX !in 0..<width || startY !in 0..<height
            || endX !in 0..<width || endY !in 0..<height)
            return false

        this[endX, endY]?.let {
            game.renderingEngine.unregister(it)
        }

        val movedEntity = this[startX, startY]
        if (movedEntity == null) {
            removeEntity(endX, endY)
            return true
        }

        super.set(endX, endY, movedEntity)
        super.removeEntity(startX, startY)
//        this[endX, endY] = this[startX, startY]
//        this[startX, startY] = null

        this[endX, endY]?.getComponent(Transform2D::class.java)!!.position = Vector2f(world.xOffset + (endX + 0.5f) * world.tileSize, world.yOffset + height * world.tileSize - (endY + 0.5f) * world.tileSize)

        return true
    }
}