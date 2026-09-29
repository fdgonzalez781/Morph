package com.morph.engine.graphics

import com.morph.engine.entities.Entity
import com.morph.engine.graphics.components.RenderData
import com.morph.engine.graphics.shaders.Shader
import com.morph.engine.newgui.Element
import com.morph.engine.physics.components.Transform
import com.morph.engine.physics.components.Transform2D

sealed class Renderable {
    data class REntity(val entity : Entity) : Renderable() {
        override val renderData: RenderData
            get() = entity.getComponent()!!

        override val transform: Transform2D
            get() = entity.getComponent()!!
    }

    data class RElement(val element : Element) : Renderable() {
        override val renderData: RenderData
            get() = element.renderData

        override val transform: Transform2D
            get() = element.transform
    }

    override fun equals(other: Any?): Boolean {
        return other is Renderable && when (other) {
            is RElement -> this is RElement && other == this
            is REntity -> this is REntity && other == this
        }
    }

    abstract val renderData : RenderData
    abstract val transform : Transform
}

class RenderBucket(val transparent: Boolean, val elements: MutableList<Renderable> = mutableListOf()) : MutableList<Renderable> by elements

class RenderBatcher(val elements : MutableMap<Shader<*>, RenderBucket> = mutableMapOf()) : MutableMap<Shader<*>, RenderBucket> by elements {
    fun add(e : Renderable) {
        val shader = e.renderData.shader
        shader?.let {
            if (elements.containsKey(it)) elements[it]!! += e
            else elements += it to RenderBucket(false, mutableListOf(e))
        }
    }

    fun remove(e : Entity) {
        if (!e.hasComponent<RenderData>()) return

        elements[e.getComponent<RenderData>()?.shader]?.remove(Renderable.REntity(e))
    }

    fun remove(e : Element) {
        elements[e.renderData.shader]?.remove(Renderable.RElement(e))
    }
}