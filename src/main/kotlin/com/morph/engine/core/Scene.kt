package com.morph.engine.core

import com.morph.engine.entities.Entity
import com.morph.engine.graphics.components.light.Light

/**
 * Created on 7/1/2025.
 */
//@Serializable
data class Scene(val name: String, val entities: List<Entity> = listOf(), val lights: List<Light> = listOf())

fun buildScene(init: SceneBuilder.() -> Unit): Scene {
    val builder = SceneBuilder()
    builder.init()
    return builder.build()
}