package com.morph.engine.util

import com.morph.engine.collision.components.BoundingBox2D
import com.morph.engine.core.World
import com.morph.engine.entities.Component
import com.morph.engine.entities.Entity
import com.morph.engine.entities.EntityFactory
import com.morph.engine.graphics.Color
import com.morph.engine.graphics.Texture
import com.morph.engine.graphics.shaders.BasicTexturedShader
import com.morph.engine.math.Vector2f
import com.morph.engine.physics.components.Transform2D
import java.util.*

/**
 * Created by Fernando on 1/16/2017.
 */
object EntityGenUtils {
    fun createEntity(world: World, name: String, vararg components: Component): Entity {
        val e = world.createEntity(name)
        Arrays.stream(components).forEach { e.addComponent(it) }
        return e
    }

    fun createEntityRectangle(world: World, name: String, width: Float, height: Float, isTrigger: Boolean, vararg additionalComponents: Component): Entity {
        val e = createEntity(world, name)
        val halfSize = Vector2f(width / 2.0f, height / 2.0f)

        e.addComponent(Transform2D(Vector2f(0f, 0f), scale = Vector2f(width, height)))
        e.addComponent(BoundingBox2D(Vector2f(0f, 0f), halfSize, isTrigger))
        e.addComponent(RenderDataUtils.createSquare(Color(1f, 1f, 1f), BasicTexturedShader(), Texture(null)))
        Arrays.stream(additionalComponents).forEach { e.addComponent(it) }

        return e
    }
}
