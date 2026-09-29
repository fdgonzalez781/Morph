package com.morph.demos.test.particlePhysics

import com.morph.engine.core.GameSystem
import com.morph.engine.core.World
import com.morph.engine.entities.Component
import com.morph.engine.entities.Entity
import com.morph.engine.graphics.components.Particle
import com.morph.engine.math.Vector2f
import com.morph.engine.math.Vector3f
import com.morph.engine.physics.components.RigidBody
import com.morph.engine.physics.components.Transform2D

class ParticlePhysicsSystem(world : World) : GameSystem(world) {
    var time = 0f
    override fun acceptEntity(e: Entity): Boolean = e.hasComponent<Transform2D>() && e.hasComponent<RigidBody>() && e.hasComponent<Particle>()
    override val requiredComponents: List<Class<out Component>>
        get() = listOf(Transform2D::class.java, RigidBody::class.java, Particle::class.java)

    override fun initSystem() {}

    override fun fixedUpdate(e: Entity, dt: Float) {
        val rb = e.getComponent<RigidBody>()!!
        val gravitationalCenter = Vector2f(0f, 0f)
//        val gravitationalCenter = Vector2f(Math.cos(time.toDouble() * 3).toFloat(), Math.sin(time.toDouble() * 3).toFloat()) * 2f
        val gravity = gravityForce(e.getComponent<Transform2D>()!!.position, gravitationalCenter, rb.mass, 50000f)
        val downGravity = Vector2f(0f, -9.8f * rb.mass)
        val position = e.getComponent<Transform2D>()!!.position
        val centripetalForce = (gravitationalCenter - position).normalize() *
                (rb.mass * (rb.velocity dot rb.velocity)) * (1f / (gravitationalCenter - position).length)

//        rb.applyForce(centripetalForce)

//        rb.applyForce(gravity)
//        rb.applyForce(downGravity)
        val scale = 20f
//        rb.applyForce(turningForce(position, Vector2f(0f, 0f)) * scale * rb.mass)
//        rb.applyForce(-position.normalize() * 25f * scale * rb.mass)
        val center = Vector2f(0f, 10f)
        val rest = Vector2f(0f, 10f)
        val k = 20f
        val b = 0.75f
        val springForce = (position - rest) * -k
        val frictionForce = rb.velocity * -b
        rb.applyForce(springForce)
//        rb.applyForce(frictionForce)
//        rb.applyForce(downGravity)

        val locusP = world.getEntityByName("locus")?.getComponent<Transform2D>()?.position!!
//        val denom = (locusP - position).length
        val tolerance = 0.01f
        val power = 0.0

        val locusGravity = (locusP - position).normalize() * 200f
//        rb.applyForce(locusGravity)
    }

    fun turningForce(position : Vector2f, center : Vector2f) : Vector2f =
            (Vector3f((center - position), 0f) cross Vector3f(0f, 0f, -1f)).xy.normalize()

    fun gravityForce(position : Vector2f, center : Vector2f, mass1 : Float, mass2 : Float) : Vector2f {
        val direction = (center - position).normalize()
        val gravityConstant = 1e-3f
        val forceStrength = gravityConstant * mass1 * mass2 / Math.abs((center - position) dot (center - position))

        return direction * forceStrength
    }

    fun gravityStrength(position : Vector2f, center : Vector2f, mass1 : Float, mass2 : Float) : Float {
        val direction = (center - position).normalize()
        val gravityConstant = 1e-3f
        val forceStrength = gravityConstant * mass1 * mass2 / Math.abs((center - position) dot (center - position))

        return forceStrength
    }

    override fun systemFixedUpdate(world: World, dt: Float) {
        time += dt
    }

//    override fun systemFixedUpdate(dt: Float) {
//        time += dt
//
//        val entities = game.world.entities.filter(::acceptEntity)
//
//        for (i in 0 until entities.size) {
//            for (j in i + 1 until entities.size) {
//                val e1 = entities[i]
//                val e2 = entities[j]
//
//                val t1 = e1.getComponent<Transform2D>()!!
//                val t2 = e2.getComponent<Transform2D>()!!
//                val rb1 = e1.getComponent<RigidBody>()!!
//                val rb2 = e2.getComponent<RigidBody>()!!
//
//                val gravity = gravityStrength(
//                        t1.position,
//                        t2.position,
//                        rb1.mass,
//                        rb2.mass
//                )
//
//                val d1 = (t2.position - t1.position).normalize()
//                val d2 = (t1.position - t2.position).normalize()
//
//                rb1.applyForce(d1 * gravity)
//                rb2.applyForce(d2 * gravity)
//            }
//        }
//    }
}