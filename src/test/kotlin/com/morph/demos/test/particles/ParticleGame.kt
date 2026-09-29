package com.morph.demos.test.particles

import com.morph.engine.core.Game
import com.morph.engine.core.OrthoCam2D
import com.morph.engine.core.Scene
import com.morph.engine.entities.EntityFactory
import com.morph.engine.graphics.Color
import com.morph.engine.graphics.EmitterSystem
import com.morph.engine.graphics.ParticleSystem
import com.morph.engine.graphics.components.Emitter
import com.morph.engine.graphics.shaders.InstancedShader
import com.morph.engine.math.Vector2f
import com.morph.engine.math.Vector3f
import com.morph.engine.physics.components.Transform2D

class ParticleGame : Game(800, 600, "Particle Effect Testing Zone", 60f, false) {
    override fun initGame() {
        camera = OrthoCam2D(Vector2f(0f, 0f), 0f, 10f * (width.toFloat() / height), 10f)
        world.addSystem(::EmitterSystem)
        world.addSystem(::ParticleSystem)

        val emitter1 = world.createEntity("rainEmitter")
            .addComponent(Transform2D(position = Vector2f(-3f, 0f)))
            .addComponent(
                Emitter(
                color = Color(0.2f, 0.2f, 1f, 0.5f),
                spawnRate = 15f,
                velocity = Vector3f(0f, 1f, 0f),
                lifetime = 2f,
                shader = InstancedShader()
            )
            )

        val emitter2 = world.createEntity("snowEmitter")
            .addComponent(Transform2D(position = Vector2f(-1f, 0f)))
            .addComponent(
                Emitter(
                color = Color(0.7f, 0.7f, 1.0f, 0.5f),
                spawnRate = 60f,
                velocity = Vector3f(0f, 1f, 0f),
                lifetime = 4f,
                shader = InstancedShader()
            )
            )

        val emitter3 = world.createEntity("fireEmitter")
            .addComponent(Transform2D(position = Vector2f(1f, 0f)))
            .addComponent(
                Emitter(
                color = Color(1.0f, 0.2f, 0.2f, 0.5f),
                spawnRate = 120f,
                velocity = Vector3f(0f, 1f, 0f),
                lifetime = 6f,
                shader = InstancedShader()
            )
            )

        val emitter4 = world.createEntity("greenEmitter")
            .addComponent(Transform2D(position = Vector2f(3f, 0f)))
            .addComponent(
                Emitter(
                color = Color(0.2f, 1.0f, 0.2f, 0.5f),
                spawnRate = 300f,
                velocity = Vector3f(0f, 1f, 0f),
                lifetime = 8f,
                shader = InstancedShader()
            )
            )

        val scEnts = mutableListOf(emitter1, emitter2, emitter3, emitter4)
        val scene = Scene("particleScene", scEnts)
        world.addScene(scene)
    }

    override fun preGameUpdate() {
    }

    override fun fixedGameUpdate(dt: Float) {
    }

    override fun postGameUpdate() {
    }

    override fun handleInput() {
    }
}
