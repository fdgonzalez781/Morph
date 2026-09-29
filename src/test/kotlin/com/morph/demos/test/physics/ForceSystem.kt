package com.morph.demos.test.physics

import com.morph.engine.core.GameSystem
import com.morph.engine.core.World
import com.morph.engine.entities.Component
import com.morph.engine.entities.Entity
import com.morph.engine.math.Vector2f
import com.morph.engine.physics.components.RigidBody
import com.morph.engine.physics.components.Transform2D

class ForceSystem(world: World) : GameSystem(world) {
    override fun acceptEntity(e: Entity): Boolean = e.hasComponent<Transform2D>() && e.hasComponent<RigidBody>()
    override val requiredComponents: List<Class<out Component>>
        get() = listOf(Transform2D::class.java, RigidBody::class.java)

    override fun initSystem() {}

    override fun fixedUpdate(e: Entity, dt: Float) {
        val rb = e.getComponent<RigidBody>()!!
        rb.applyForce(Vector2f(0f, -9.8f) * rb.mass)
    }
}
