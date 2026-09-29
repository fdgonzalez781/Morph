package com.morph.engine.core

import com.morph.engine.entities.Component
import com.morph.engine.entities.Entity
import com.morph.engine.graphics.components.light.Light
import com.morph.engine.util.SparseSet
import kotlin.collections.forEach

/**
 * Created by Fernando on 1/19/2017.
 */
// TODO: Generate entities from World, not EntityFactory
open class World(val game: Game) {
    val entities : MutableList<Entity> = mutableListOf()
    private val systems : MutableList<GameSystem> = mutableListOf()
    private val components = mutableMapOf<Class<out Component>, SparseSet<Component>>()
    private val scenes = mutableListOf<Scene>()
    private var count = 0

    fun getEntityByName(name: String): Entity? = entities.find { it.name == name }
    fun getEntityByID(id: Int): Entity? = entities.find { it.id == id }

    fun createEntity(name: String): Entity {
        return Entity(name, count++)
    }

    open fun addEntity(e: Entity): Boolean {
        game.renderingEngine.register(e)
        return entities.add(e)
    }

    fun addEntities(e: List<Entity>) {
        entities.addAll(e)
        e.forEach { game.renderingEngine.register(it) }
    }

    fun addScene(sc: Scene) {
        scenes.add(sc)
        addEntities(sc.entities)
        addLights(sc.lights)
    }

    private fun addLights(lights: List<Light>) {
        lights.forEach { game.renderingEngine.addLight(it) }
    }

    open fun removeEntity(e: Entity): Boolean {
        game.renderingEngine.unregister(e)
        e.destroy()
        return entities.remove(e)
    }

    fun removeEntities(e: List<Entity>) {
        entities.removeAll(e)
        e.forEach { en ->
            game.renderingEngine.unregister(en)
            en.destroy()
        }
    }

    fun removeScene(sc: Scene) {
        scenes.remove(sc)
        removeEntities(sc.entities)
        removeLights(sc.lights)
    }

    private fun removeLights(lights: List<Light>) {
        lights.forEach { game.renderingEngine.removeLight(it) }
    }

    fun addComponent(entityId: Int, component: Component) {
        components.getOrPut(component::class.java) { SparseSet() }[entityId] = component
    }

    fun addComponents(entityId: Int, vararg comps: Component) {
        comps.forEach { addComponent(entityId, it) }
    }

    fun removeComponent(entityId: Int, component: Component) {
        components[component::class.java]?.let {
            val c = it.remove(entityId)
            c?.apply {
                parent = null
                destroy()
            }
        }
    }

    fun <T : Component> getComponent(entityId: Int, componentType: Class<T>): T? {
        return components[componentType]?.get(entityId) as T?
    }

    fun <T : Component> getComponents(componentType: Class<T>): MutableMap<Int, T> {
        return components[componentType] as MutableMap<Int, T>
    }

    fun destroy() {
//        components.values.forEach { cs -> cs.forEach { it.value.parent = null; it.value.destroy() }}
        components.clear()
    }

    inline fun <reified T : Component> getComponent(entityId: Int): T? = getComponents(T::class.java)[entityId]

    open fun init() {}

    fun addSystem(sysInit : (World) -> GameSystem): GameSystem {
        val gs = sysInit(this)
        systems.add(gs)
        return gs
    }

    fun removeSystem(gs: GameSystem): Boolean {
        return systems.remove(gs)
    }

    fun initSystems() {
        systems.forEach { it.initSystem() }
    }

    fun preUpdate() {
        systems.forEach { it.preUpdate() }
    }

    fun update() {
        systems.forEach { it.update() }
    }

    fun fixedUpdate(dt: Float) {
        systems.forEach { it.fixedUpdate(dt) }
    }

    fun postUpdate() {
        systems.forEach { it.postUpdate() }
    }
}
