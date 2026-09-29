package com.morph.demos.test.particlePhysics

import com.morph.engine.core.GameSystem
import com.morph.engine.core.World
import com.morph.engine.entities.Component
import com.morph.engine.entities.Entity
import com.morph.engine.input.Mouse
import com.morph.engine.physics.components.Transform2D

class FollowerSystem(world: World) : GameSystem(world) {
    var time = 0f
    override fun acceptEntity(e: Entity): Boolean = e.hasComponent<Transform2D>() && e.hasComponent<PlayerFollower>()
    override val requiredComponents: List<Class<out Component>>
        get() = listOf(Transform2D::class.java, PlayerFollower::class.java)

    override fun initSystem() {}

    override fun fixedUpdate(e: Entity, dt: Float) {
        val t2d = e.getComponent<Transform2D>()!!
        t2d.position = Mouse.worldMousePosition
//        t2d.position = Vector2f(Math.cos(time.toDouble() * 3).toFloat(), Math.sin(time.toDouble() * 3).toFloat()) * 10f
    }

    override fun systemFixedUpdate(world: World, dt: Float) {
        time += dt
    }
}