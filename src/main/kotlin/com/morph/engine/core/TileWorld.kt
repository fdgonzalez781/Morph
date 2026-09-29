package com.morph.engine.core

import com.morph.engine.entities.Entity
import com.morph.engine.entities.EntityGrid
import com.morph.engine.entities.EntityWorldGrid
import com.morph.engine.entities.given
import com.morph.engine.physics.components.Transform2D
import kotlin.math.floor

/**
 * Created by Fernando on 1/19/2017.
 */
class TileWorld(game: Game, val width: Int, val height: Int, val tileSize: Float) : World(game) {
    var xOffset: Float = 0f
    var yOffset: Float = 0f
    private val grid: EntityWorldGrid = EntityWorldGrid(game, width, height, this)

    init {
        this.xOffset = 0f
        this.yOffset = 0f
    }

    // TODO: Strict implementation = return false
    override fun addEntity(e: Entity): Boolean {
        var ret : Boolean = false
        given<Transform2D>(e) { t2D ->
            val tilePos = (t2D.position / tileSize).map { x -> floor(x.toDouble()).toFloat() }
            ret = grid.set(tilePos.x.toInt(), tilePos.y.toInt(), e)
        }
        return ret
    }

    fun addEntityGrid(grid: EntityGrid, startX: Int, startY: Int): Boolean {
        for (y in 0 until grid.height) {
            for (x in 0 until grid.width) {
                val e = grid[x, y]
                if (e != null) {
                    val success = grid.set(startX + x, startY + y, e)
                    if (!success) return false
                }
            }
        }

        return true
    }

    fun removeEntityGrid(grid: EntityGrid): Boolean {
        for (e in this.grid.asList())
            if (grid.asList().contains(e))
                removeEntity(e)

        return true
    }

    fun lazyMoveEntityGrid(grid: EntityGrid, x: Int, y: Int): Boolean {
        removeEntityGrid(grid)
        return addEntityGrid(grid, x, y)
    }

    private fun findMatch(e: Entity?): Pair<Int, Int> {
        for (y in 0 until height)
            for (x in 0 until width)
                if (grid[x, y] != null && e != null && e == grid[x, y])
                    return Pair(x, y)

        return Pair(-1, -1)
    }

    override fun removeEntity(e: Entity): Boolean {
        val (x, y) = findMatch(e)
        return if (x == -1) false else grid.removeEntity(x, y)

    }

    fun isEmpty(x: Int, y: Int): Boolean =
            if (x < 0 || x >= width || y < 0 || y >= height) false else grid[x, y] == null

    fun areEmpty(vararg positions: Pair<Int, Int>): Boolean = positions.all { (x, y) -> isEmpty(x, y) }
}
