package com.morph.demos.test.particlePhysics

import com.morph.engine.collision.CollisionEngine
import com.morph.engine.collision.RestitutionSolver
import com.morph.engine.core.Game
import com.morph.engine.core.OrthoCam2D
import com.morph.engine.core.Scene
import com.morph.engine.entities.Entity
import com.morph.engine.entities.EntityFactory
import com.morph.engine.graphics.*
import com.morph.engine.graphics.components.Emitter
import com.morph.engine.graphics.shaders.InstancedShader
import com.morph.engine.graphics.shaders.TintShader
import com.morph.engine.input.*
import com.morph.engine.math.Vector2f
import com.morph.engine.math.Vector3f
import com.morph.engine.physics.PhysicsEngine
import com.morph.engine.physics.components.RigidBody
import com.morph.engine.physics.components.Transform2D
import org.lwjgl.glfw.GLFW

class PartPhysGame : Game(1366, 768, "Particle Physics Testing", 60f, false) {
//    var prevTime = System.nanoTime()

    override fun initGame() {
        val size = 50f

        renderingEngine.setActiveFramebuffer(Framebuffer(width, height, false, 0))

        val sc = mutableListOf<Entity>()

        val emitter1 = world.createEntity("rainEmitter")
            .addComponent(Transform2D(position = Vector2f(-9f, 5f)))
            .addComponent(
                Emitter(
                    color = Color(0.2f, 0.2f, 1.0f, 1.0f),
                    spawnRate = 150f,
                    velocity = Vector3f(0f, 1f, 0f),
                    lifetime = 2f,
                    shader = InstancedShader(),
                    texture = Texture("textures/particle.png")
                )
            )
//                .addComponent(PlayerFollower())

        val emitter2 = world.createEntity("snowEmitter")
            .addComponent(Transform2D(position = Vector2f(-3f, -5f)))
            .addComponent(
                Emitter(
                    color = Color(0.7f, 0.7f, 1.0f, 1.0f),
                    spawnRate = 150f,
                    velocity = Vector3f(0f, 1f, 0f),
                    lifetime = 2f,
                    shader = InstancedShader(),
                    texture = Texture("textures/particle.png")
                )
            )

        val emitter3 = world.createEntity("fireEmitter")
            .addComponent(Transform2D(position = Vector2f(3f, 5f)))
            .addComponent(
                Emitter(
                    color = Color(1.0f, 0.2f, 0.2f, 1.0f),
                    spawnRate = 150f,
                    velocity = Vector3f(0f, 1f, 0f),
                    lifetime = 2f,
                    shader = InstancedShader(),
                    texture = Texture("textures/particle.png")
                )
            )

        val emitter4 = world.createEntity("greenEmitter")
            .addComponent(Transform2D(position = Vector2f(9f, -5f)))
            .addComponent(
                Emitter(
                    color = Color(0.2f, 1.0f, 0.2f, 1.0f),
                    spawnRate = 150f,
                    velocity = Vector3f(0f, 1f, 0f),
                    lifetime = 2f,
                    shader = InstancedShader(),
                    texture = Texture("textures/particle.png")
                )
            )

        val crazyEmitter = world.createEntity("crazyEmitter")
            .addComponent(
                Emitter(
                    color = Color(1f, 1f, 1f, 1f),
                    spawnRate = 100f,
                    velocity = Vector3f(0f, 0f, 0f),
                    lifetime = 10f,
                    shader = InstancedShader(),
                    texture = Texture("textures/particle.png")
                )
            )

        sc += listOf(emitter1, emitter2, emitter3, emitter4)
        //        addEntity(crazyEmitter)

        val player = EntityFactory.getCustomTintRectangle("player", 1f, 1f, Color(0.5f, 0.5f, 0.5f), TintShader())
            .addComponent(PlayerFollower())
//                .addComponent(BoundingBox2D(Vector2f(), Vector2f(2.5f, 2.5f)))
            .addComponent(RigidBody())
            .addComponent(Emitter(
                color = Color(0.2f, 0.75f, 0.2f, 1f),
                spawnRate = 100f,
                velocity = Vector3f(0f, 0f, 0f),
                lifetime = 10f,
                shader = InstancedShader(),
                texture = Texture("textures/particle.png")
            ))

        sc += player

        val locus = EntityFactory.getCustomTintRectangle("locus", 1f, 1f, Color(0.3f, 0f, 0f), TintShader()).addComponent(Transform2D(position = Vector2f(0f, 0f)))
        sc += locus

        val particleScene = Scene("particleScene", sc)
        loadScene(particleScene)

        val inputMapping = InputMapping()
        inputMapping.mapKey(GLFW.GLFW_KEY_LEFT, KeyRepeat) { Emitter.size -= 0.01f }
        inputMapping.mapKey(GLFW.GLFW_KEY_RIGHT, KeyRepeat) { Emitter.size += 0.01f }
        inputMapping.mapKey(GLFW.GLFW_KEY_DOWN, KeyRepeat) { Emitter.spread -= 0.01f }
        inputMapping.mapKey(GLFW.GLFW_KEY_UP, KeyRepeat) { Emitter.spread += 0.01f }
        inputMapping.mapButton(GLFW.GLFW_MOUSE_BUTTON_1, MousePress) { player.getComponent<Emitter>()!!.enabled = true }
        inputMapping.mapButton(GLFW.GLFW_MOUSE_BUTTON_1, MouseRelease) { player.getComponent<Emitter>()!!.enabled = false }
        inputMapping.mapButton(GLFW.GLFW_MOUSE_BUTTON_2, MousePress) { locus.getComponent<Transform2D>()?.position = Mouse.worldMousePosition }

        this.inputMapping = inputMapping

//        this.world = PartPhysWorld(this)
        camera = OrthoCam2D(Vector2f(0f, 0f), 0f, size * (width.toFloat() / height), size)
        world.addSystem(::ParticlePhysicsSystem)
        world.addSystem { CollisionEngine(it, RestitutionSolver(1.0f)) }
        world.addSystem(::PhysicsEngine)
        world.addSystem(::EmitterSystem)
        world.addSystem(::ParticleSystem)
        world.addSystem(::FollowerSystem)
    }

    override fun preGameUpdate() {
    }

    override fun fixedGameUpdate(dt: Float) {
//        val currentTime = System.nanoTime()
//        val elapsedTime = currentTime - prevTime
//        val realFPS = 1000000000 * (1.0 / elapsedTime)
//        prevTime = currentTime
//        println("$realFPS frames per second")
    }

    override fun postGameUpdate() {
    }

    override fun handleInput() {
    }
}
