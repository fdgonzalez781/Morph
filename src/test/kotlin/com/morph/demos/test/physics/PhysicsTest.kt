package com.morph.demos.test.physics

import com.morph.engine.collision.CollisionEngine
import com.morph.engine.collision.PerfectlyInelasticSolver
import com.morph.engine.collision.RestitutionSolver
import com.morph.engine.core.*
import com.morph.engine.entities.Entity
import com.morph.engine.entities.EntityFactory
import com.morph.engine.graphics.Color
import com.morph.engine.graphics.shaders.TintShader
import com.morph.engine.input.InputMapping
import com.morph.engine.input.KeyDown
import com.morph.engine.input.Mouse
import com.morph.engine.input.MousePress
import com.morph.engine.math.Vector2f
import com.morph.engine.physics.PhysicsEngine
import com.morph.engine.physics.components.RigidBody
import com.morph.engine.physics.components.Transform2D
import org.lwjgl.glfw.GLFW.*

fun main() = GameApplication(PhysicsGame()).launchGame()

class PhysicsGame : Game(800, 600, "Physics Demo", 60f, false) {
    var latestEntity: Entity? = null

    override fun initGame() {
        val size = 100f

        camera = OrthoCam2D(Vector2f(0f, 0f), 0f, size * (width.toFloat() / height), size)

        world.addSystem(::ForceSystem)
        world.addSystem{ CollisionEngine(it, RestitutionSolver(0.9f)) }
        world.addSystem(::PhysicsEngine)

        val inputMapping = InputMapping()

        inputMapping.mapButton(GLFW_MOUSE_BUTTON_1, MousePress) {
            val entity = EntityFactory.getCustomTintRectangle(
                "block-${System.nanoTime()}",
                3f,
                3f,
                Color(0.8f, 0.8f, 0.8f),
                TintShader()
            )
            entity.addComponent(RigidBody())
            entity.getComponent<Transform2D>()!!.position = Mouse.worldMousePosition

            world.addEntity(entity)
            latestEntity = entity
        }

        val playerSupplier = { world.getEntityByName("player") }

        inputMapping.mapKey(GLFW_KEY_W, KeyDown) {
            playerSupplier()?.getComponent<RigidBody>()?.applyForce(Vector2f(0f, 20f))
        }

        inputMapping.mapKey(GLFW_KEY_A, KeyDown) {
            playerSupplier()?.getComponent<RigidBody>()?.applyForce(Vector2f(-20f, 0f))
        }

        inputMapping.mapKey(GLFW_KEY_S, KeyDown) {
            playerSupplier()?.getComponent<RigidBody>()?.applyForce(Vector2f(0f, -20f))
        }

        inputMapping.mapKey(GLFW_KEY_D, KeyDown) {
            playerSupplier()?.getComponent<RigidBody>()?.applyForce(Vector2f(20f, 0f))
        }

        this.inputMapping = inputMapping

        val floor = EntityFactory.getCustomTintRectangle("floor", 200f, 2f, Color(0f, 0f, 0.7f), TintShader())
        floor.getComponent<Transform2D>()?.position = Vector2f(0f, -49f)

        val player = EntityFactory.getCustomTintRectangle("player", 2f, 2f, Color(0.5f, 0.9f, 0.5f), TintShader()).addComponent(RigidBody())

        val physicsScene = Scene("physicsScene", mutableListOf(floor, player))
        loadScene(physicsScene)
    }

    override fun preGameUpdate() {
    }

    override fun fixedGameUpdate(dt: Float) {
//        latestEntity?.let { println(it.getComponent<RigidBody>()!!.acceleration) }
    }

    override fun postGameUpdate() {
    }

    override fun handleInput() {
    }
}