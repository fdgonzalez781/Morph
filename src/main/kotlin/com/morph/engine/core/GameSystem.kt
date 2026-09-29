package com.morph.engine.core

import com.morph.engine.entities.Component
import com.morph.engine.entities.Entity

abstract class GameSystem(val world: World) {
    protected abstract fun acceptEntity(e: Entity): Boolean
    abstract val requiredComponents: List<Class<out Component>>

    abstract fun initSystem()

    fun preUpdate() {
        world.entities.filter(this::acceptEntity).forEach(this::preUpdate)
        systemPreUpdate(world)
    }

    fun update() {
        world.entities.filter(this::acceptEntity).forEach(this::update)
        systemUpdate(world)
    }

    fun fixedUpdate(dt: Float) {
        world.entities.filter(this::acceptEntity).forEach { fixedUpdate(it, dt) }
        systemFixedUpdate(world, dt)
    }

    fun postUpdate() {
        world.entities.filter(this::acceptEntity).forEach(this::postUpdate)
        systemPostUpdate(world)
    }

    protected open fun preUpdate(e: Entity) {}
    protected open fun update(e: Entity) {}
    protected open fun fixedUpdate(e: Entity, dt: Float) {}
    protected open fun postUpdate(e: Entity) {}

    protected open fun systemPreUpdate(world: World) {}
    protected open fun systemUpdate(world: World) {}
    protected open fun systemFixedUpdate(world: World, dt: Float) {}
    protected open fun systemPostUpdate(world: World) {}
}
