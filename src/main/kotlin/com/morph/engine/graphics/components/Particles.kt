package com.morph.engine.graphics.components

import com.morph.engine.core.World
import com.morph.engine.entities.Component
import com.morph.engine.entities.EntityFactory
import com.morph.engine.graphics.Color
import com.morph.engine.graphics.Texture
import com.morph.engine.graphics.shaders.InstancedShader
import com.morph.engine.math.Vector2f
import com.morph.engine.math.Vector3f
import com.morph.engine.physics.components.RigidBody
import com.morph.engine.physics.components.Transform2D
import org.lwjgl.opengl.GL11.GL_DOUBLE
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glVertexAttribPointer
import org.lwjgl.opengl.GL30.glBindVertexArray
import org.lwjgl.opengl.GL30.glGenVertexArrays
import org.lwjgl.opengl.GL33.glVertexAttribDivisor
import java.util.*

class Particle(var color : Color, val emitter : Emitter) : Component() {
    var age = 0f
}

class Emitter(
        val color : Color,
        val spawnRate : Float,
        val velocity : Vector3f,
        val lifetime : Float,
        val shader : InstancedShader,
        val texture : Texture = Texture(null),
        var enabled : Boolean = true,
        private val particles: Queue<Particle> = LinkedList()
) : Component(), Queue<Particle> by particles {
    var acc = 0f
    val maxParticles
        get() = Math.ceil((lifetime * spawnRate).toDouble()).toLong()
    var vao : Int = 0
    var particleBuffer = 0
    var texCoordBuffer = 0
    var colorBuffer = 0
    var transformBuffer = 0
    var indexBuffer = 0

    override fun init() {
        particleBuffer = glGenBuffers()
        colorBuffer = glGenBuffers()
        texCoordBuffer = glGenBuffers()
        transformBuffer = glGenBuffers()
        indexBuffer = glGenBuffers()
        vao = glGenVertexArrays()

        val particleData = doubleArrayOf(
                -0.5, -0.5, 0.0,
                -0.5, 0.5, 0.0,
                0.5, 0.5, 0.0,
                0.5, -0.5, 0.0
        )

        val texCoordData = doubleArrayOf(
                0.0, 1.0,
                0.0, 0.0,
                1.0, 0.0,
                1.0, 1.0
        )

        val indexData = intArrayOf(0, 1, 3, 1, 2, 3)

        glBindBuffer(GL_ARRAY_BUFFER, particleBuffer)
        glBufferData(GL_ARRAY_BUFFER, particleData, GL_STATIC_DRAW)

        glBindBuffer(GL_ARRAY_BUFFER, colorBuffer)
        glBufferData(GL_ARRAY_BUFFER, maxParticles * 4, GL_DYNAMIC_DRAW)

        glBindBuffer(GL_ARRAY_BUFFER, texCoordBuffer)
        glBufferData(GL_ARRAY_BUFFER, texCoordData, GL_STATIC_DRAW)

        glBindBuffer(GL_ARRAY_BUFFER, transformBuffer)
        glBufferData(GL_ARRAY_BUFFER, maxParticles * 16, GL_DYNAMIC_DRAW)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexData, GL_STATIC_DRAW)

        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)

        glBindVertexArray(vao)
            glEnableVertexAttribArray(0)
            glBindBuffer(GL_ARRAY_BUFFER, particleBuffer)
            glVertexAttribPointer(0, 3, GL_DOUBLE, false, 0, 0)
            glVertexAttribDivisor(0, 0)

            glEnableVertexAttribArray(1)
            glBindBuffer(GL_ARRAY_BUFFER, colorBuffer)
            glVertexAttribPointer(1, 4, GL_DOUBLE, false, 0, 0)
            glVertexAttribDivisor(1, 1)

            glEnableVertexAttribArray(2)
            glBindBuffer(GL_ARRAY_BUFFER, texCoordBuffer)
            glVertexAttribPointer(2, 2, GL_DOUBLE, false, 0, 0)
            glVertexAttribDivisor(2, 0)

            glEnableVertexAttribArray(3)
            glEnableVertexAttribArray(4)
            glEnableVertexAttribArray(5)
            glEnableVertexAttribArray(6)

            glBindBuffer(GL_ARRAY_BUFFER, transformBuffer)

            glVertexAttribPointer(3, 4, GL_DOUBLE, false, 128, 0)
            glVertexAttribDivisor(3, 1)

            glVertexAttribPointer(4, 4, GL_DOUBLE, false, 128, 32)
            glVertexAttribDivisor(4, 1)

            glVertexAttribPointer(5, 4, GL_DOUBLE, false, 128, 64)
            glVertexAttribDivisor(5, 1)

            glVertexAttribPointer(6, 4, GL_DOUBLE, false, 128, 96)
            glVertexAttribDivisor(6, 1)

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer)
        glBindVertexArray(0)
    }

    fun spawnParticle(world : World) {
        val particle = Particle(color, this)
        val randomOffset = Vector2f((Math.random() - 0.5).toFloat() * spread, (Math.random() - 0.5).toFloat() * spread)
        val particlePos = parent?.getComponent<Transform2D>()?.position!! + randomOffset
        // TODO: Handle the case where a particle emitter has no parent entity (?)

        val entity = world.createEntity("Particle-${System.nanoTime()}")
                .addComponent(Transform2D(position = particlePos, scale = Vector2f(Emitter.size, Emitter.size)))
                .addComponent(particle)
                .addComponent(RigidBody(mass = 10f))
//                .addComponent(BoundingBox2D(Vector2f(), Vector2f(Emitter.size, Emitter.size) * 0.5f))// TODO: Remove; generalize

        entity.getComponent<Transform2D>()!!.position = particlePos

        world.addEntity(entity)
        particles.add(particle)
    }

    companion object {
        var size = 0.1f
        var spread = 3f
    }
}