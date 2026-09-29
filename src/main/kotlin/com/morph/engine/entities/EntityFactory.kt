package com.morph.engine.entities

import com.morph.engine.collision.components.BoundingBox2D
import com.morph.engine.core.World
import com.morph.engine.graphics.Color
import com.morph.engine.graphics.Texture
import com.morph.engine.graphics.shaders.BasicTexturedShader
import com.morph.engine.graphics.shaders.Shader
import com.morph.engine.math.Vector2f
import com.morph.engine.physics.components.Transform2D
import com.morph.engine.util.RenderDataUtils

/**
 * Created by Fernando on 1/15/2017.
 */
object EntityFactory {
    var world: World? = null

    fun createEntity(name: String) = world?.createEntity(name) ?: throw RuntimeException("Cannot create entity: no world attached to EntityFactory")

    fun getRectangleAt(
        name: String,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        c: Color,
        isTrigger: Boolean
    ): Entity {
        val result = createEntity(name)

        val halfSize = Vector2f(width / 2.0f, height / 2.0f)
        result.addComponent(Transform2D(Vector2f(x, y), scale = Vector2f(width, height)))
        result.addComponent(BoundingBox2D(Vector2f(x, y), halfSize, isTrigger))
        result.addComponent(RenderDataUtils.createSquare(c, BasicTexturedShader(), Texture(null)))

        return result
    }

    fun getRectangle(name: String, width: Float, height: Float, c: Color): Entity {
        val result = createEntity(name)
        val halfSize = Vector2f(width / 2.0f, height / 2.0f)
        result.addComponent(Transform2D(Vector2f(0f, 0f), scale = Vector2f(width, height)))
        result.addComponent(BoundingBox2D(Vector2f(0f, 0f), halfSize, false))

        result.addComponent(RenderDataUtils.createSquare(c, BasicTexturedShader(), Texture(null)))

        return result
    }

    fun getCustomRectangle(name: String, width: Float, height: Float, c: Color, shader: Shader<*>): Entity {
        val result = createEntity(name)
        val halfSize = Vector2f(width / 2.0f, height / 2.0f)
        result.addComponent(Transform2D(Vector2f(0f, 0f), scale = Vector2f(width, height)))
        result.addComponent(BoundingBox2D(Vector2f(0f, 0f), halfSize, false))

        result.addComponent(RenderDataUtils.createSquare(c, shader, Texture(null)))

        return result
    }

    fun getCustomTintRectangle(name: String, width: Float, height: Float, c: Color, shader: Shader<*>): Entity {
        val result = createEntity(name)
        val halfSize = Vector2f(width / 2.0f, height / 2.0f)
        result.addComponent(Transform2D(Vector2f(0f, 0f), scale = Vector2f(width, height)))
        result.addComponent(BoundingBox2D(Vector2f(0f, 0f), halfSize, false))

        val data = RenderDataUtils.createSquare(Color(1f, 1f, 1f), shader, Texture(null))
        data.tint = c

        result.addComponent(data)

        return result
    }
}
