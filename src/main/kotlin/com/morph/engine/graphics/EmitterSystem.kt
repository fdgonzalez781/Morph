package com.morph.engine.graphics

import com.morph.engine.core.GameSystem
import com.morph.engine.core.World
import com.morph.engine.entities.Component
import com.morph.engine.entities.Entity
import com.morph.engine.graphics.components.Emitter
import com.morph.engine.physics.components.Transform2D

class EmitterSystem(world : World) : GameSystem(world) {
    override fun acceptEntity(e: Entity): Boolean = e.hasComponent<Emitter>() && e.hasComponent<Transform2D>()
    override val requiredComponents: List<Class<out Component>>
        get() = listOf(Transform2D::class.java, Emitter::class.java)

    override fun initSystem() {}

    override fun fixedUpdate(e: Entity, dt: Float) {
        super.fixedUpdate(e, dt)
        val emitter = e.getComponent<Emitter>()!!
        val t2d = e.getComponent<Transform2D>()!!

        // TODO: Consider moving the spawn loop into Emitter.
        if (emitter.enabled) {
            emitter.acc += dt

            while (emitter.acc >= (1f / emitter.spawnRate)) {
                emitter.spawnParticle(world)
                emitter.acc -= (1f / emitter.spawnRate)
            }
        }

        while (emitter.peek() != null && emitter.peek().age >= emitter.lifetime) {
            emitter.peek().parent?.apply { world.removeEntity(this) }
            emitter.remove()
        }
    }
}