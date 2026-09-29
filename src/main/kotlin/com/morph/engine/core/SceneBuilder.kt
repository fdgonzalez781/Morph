package com.morph.engine.core

import com.morph.engine.entities.Entity
import com.morph.engine.graphics.components.light.Light
import java.util.function.Consumer

class SceneBuilder {
    var name: String = ""
    var entities: MutableList<Entity> = mutableListOf()
    var lights: MutableList<Light> = mutableListOf()

    fun name(name: String): SceneBuilder {
        this.name = name
        return this
    }

    fun entities(entities: List<Entity>): SceneBuilder {
        this.entities.addAll(entities)
        return this
    }

    fun entities(block: Consumer<List<Entity>>): SceneBuilder {
        val list = mutableListOf<Entity>()
        block.accept(list)
        this.entities = list
        return this
    }

    fun lights(lights: List<Light>): SceneBuilder {
        this.lights.addAll(lights)
        return this
    }

    fun lights(block: Consumer<List<Light>>): SceneBuilder {
        val list = mutableListOf<Light>()
        block.accept(list)
        this.lights = list
        return this
    }

    fun build(): Scene {
        return Scene(name, entities, lights)
    }
}