package com.morph.engine.graphics

import com.morph.engine.core.Camera
import com.morph.engine.core.GameSystem
import com.morph.engine.core.World
import com.morph.engine.entities.Component
import com.morph.engine.entities.Entity
import com.morph.engine.graphics.Renderable.RElement
import com.morph.engine.graphics.Renderable.REntity
import com.morph.engine.graphics.components.Emitter
import com.morph.engine.graphics.components.Particle
import com.morph.engine.graphics.components.RenderData
import com.morph.engine.graphics.components.light.Light
import com.morph.engine.graphics.shaders.Shader
import com.morph.engine.math.Matrix4f
import com.morph.engine.math.MatrixUtils.getOrthographicProjectionMatrix
import com.morph.engine.newgui.Element
import com.morph.engine.physics.components.Transform
import com.morph.engine.physics.components.Transform2D
import org.lwjgl.opengl.*
import org.lwjgl.system.MemoryUtil
import java.util.*
import java.util.function.Consumer

// TODO: Generalize to platform agnostic version
class GLRenderingEngine(world: World, width: Int, height: Int) : GameSystem(world) {
    private val screenProjection: Matrix4f =
        getOrthographicProjectionMatrix(height.toFloat(), 0f, 0f, width.toFloat(), -1f, 1f)
    private var camera: Camera? = null
    private val lights: MutableList<Light> = ArrayList<Light>()
    private val batcher: RenderBatcher = RenderBatcher()
    private val emitters: MutableList<Emitter> = ArrayList<Emitter>()
    private var activeFramebuffer: Framebuffer? = null

    private fun render(data: RenderData?, transform: Transform?) {
        if (data == null || transform == null) return

        data.shader.bind()
        data.shader.uniforms.setUniforms(
            transform,
            data,
            camera!!,
            screenProjection,
            lights
        ) // TODO: Generate UBO instead of setting uniforms

        GL30.glBindVertexArray(data.vertexArrayObject)
        GL11.glDrawElements(GL11.GL_TRIANGLES, data.indices.size, GL11.GL_UNSIGNED_INT, MemoryUtil.NULL)
        GL30.glBindVertexArray(0)

        data.shader.uniforms.unbind(transform, data)
        data.shader.unbind()
    }

    private fun render(emitter: Emitter) {
        val colors =
            emitter.stream().flatMapToDouble { particle: Particle? -> Arrays.stream(particle!!.color.toDoubleArray()) }
                .toArray()
        val transforms = emitter.stream().map<Matrix4f> { particle: Particle? ->
            camera!!.projectionMatrix.times(
                particle!!.parent!!.getComponent<Transform2D>(Transform2D::class.java)!!.transformationMatrix
            ).transpose
        }
            .flatMapToDouble { matrix: Matrix4f? -> Arrays.stream(matrix!!.toDoubleArray()) }.toArray()

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, emitter.colorBuffer)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colors, GL15.GL_DYNAMIC_DRAW)

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, emitter.transformBuffer)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, transforms, GL15.GL_DYNAMIC_DRAW)

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)

        emitter.shader.bind()
        emitter.shader.uniforms.setUniforms(emitter)

        GL30.glBindVertexArray(emitter.vao)
        GL31.glDrawElementsInstanced(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_INT, MemoryUtil.NULL, emitter.size)
        GL30.glBindVertexArray(0)

        emitter.shader.uniforms.unbind()
        emitter.shader.unbind()
    }

    private fun render(e: Entity) {
        render(e.getComponent<RenderData>(RenderData::class.java), e.getComponent<Transform>(Transform::class.java))
    }

    private fun render(e: Element) {
        render(e.renderData, e.transform)
    }

    fun register(e: Entity) {
        if (e.hasComponents(RenderData::class.java, Transform2D::class.java)) {
            val renderable = REntity(e)
            batcher.add(renderable)
        }

        e.getComponent<Emitter>()?.let { emitters.add(it) }
    }

    fun unregister(e: Entity) {
        batcher.remove(e)

        e.getComponent<Emitter>()?.let { emitters.remove(it) }
    }

    fun register(e: Element) {
        val renderable = RElement(e)
        batcher.add(renderable)
    }

    fun unregister(e: Element) {
        batcher.remove(e)
    }

    fun addLight(l: Light) {
        lights.add(l)
    }

    fun removeLight(l: Light) {
        lights.remove(l)
    }

    fun render(display: GLDisplay) {
        activeFramebuffer?.bindRenderTargets()

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)

        batcher.forEach { (shader: Shader<*>?, renderBucket: RenderBucket?) ->
            shader!!.bind()
            renderBucket!!.stream().filter { r: Renderable? -> r is REntity }
                .forEach { renderable: Renderable? -> this.render(renderable!!) }
            shader.unbind()
        }

        batcher.forEach { (shader: Shader<*>?, renderBucket: RenderBucket?) ->
            shader!!.bind()
            renderBucket!!.stream().filter { r: Renderable? -> r is RElement }
                .forEach { renderable: Renderable? -> this.render(renderable!!) }
            shader.unbind()
        }

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE)
        emitters.forEach(Consumer { emitter: Emitter? -> this.render(emitter!!) })
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        if (activeFramebuffer != null) {
            activeFramebuffer!!.unbindRenderTargets()

            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT)

            activeFramebuffer!!.shader.bind()
            activeFramebuffer!!.shader.uniforms.setUniforms(activeFramebuffer!!)

            GL30.glBindVertexArray(activeFramebuffer!!.vao)
            GL11.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_INT, MemoryUtil.NULL)
            GL30.glBindVertexArray(0)

            activeFramebuffer!!.shader.uniforms.unbind(activeFramebuffer!!)
            activeFramebuffer!!.shader.unbind()
        }

        display.update()
    }

    private fun render(renderable: Renderable) {
        val data = renderable.renderData
        val transform = renderable.transform

        data.shader.uniforms.setUniforms(
            transform,
            data,
            camera!!,
            screenProjection,
            lights
        ) // TODO: Generate UBO instead of setting uniforms

        GL30.glBindVertexArray(data.vertexArrayObject)
        GL11.glDrawElements(GL11.GL_TRIANGLES, data.indices.size, GL11.GL_UNSIGNED_INT, MemoryUtil.NULL)
        GL30.glBindVertexArray(0)

        data.shader.uniforms.unbind(transform, data)
    }

    fun setClearColor(clearColor: Color) {
        GL11.glClearColor(clearColor.red, clearColor.green, clearColor.blue, clearColor.alpha)
    }

    public override fun initSystem() {
        GL13.glActiveTexture(GL13.GL_TEXTURE0)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
    }

    fun setCamera(camera: Camera) {
        this.camera = camera
    }

    fun setClearColor(r: Float, g: Float, b: Float, a: Float) {
        GL11.glClearColor(r, g, b, a)
    }

    override fun acceptEntity(e: Entity): Boolean {
        return e.hasComponents(RenderData::class.java, Transform::class.java)
    }

    fun setActiveFramebuffer(activeFramebuffer: Framebuffer?) {
        this.activeFramebuffer = activeFramebuffer
    }

    override val requiredComponents: List<Class<out Component>> = listOf(Transform::class.java, RenderData::class.java)
}
