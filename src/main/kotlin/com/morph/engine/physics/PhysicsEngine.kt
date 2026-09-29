package com.morph.engine.physics

import com.morph.engine.core.GameSystem
import com.morph.engine.core.World
import com.morph.engine.entities.Component
import com.morph.engine.entities.Entity
import com.morph.engine.entities.given
import com.morph.engine.physics.components.RigidBody
import com.morph.engine.physics.components.Transform

class PhysicsEngine(world: World) : GameSystem(world) {
    override fun acceptEntity(e: Entity): Boolean = e.hasComponent<RigidBody>() && e.hasComponent<Transform>()
    override val requiredComponents: List<Class<out Component>>
        get() = listOf(RigidBody::class.java, Transform::class.java)

    override fun initSystem() {}

    override fun fixedUpdate(e: Entity, dt: Float) {
        given<RigidBody>(e) { rb ->
            given<Transform>(e) { t ->
                val translation = rb.update(dt)
                t.translate(translation.asTranslationMatrix())
            }
        }
    }
}
