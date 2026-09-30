package com.morph.engine.graphics.shaders

import com.morph.engine.core.Camera
import com.morph.engine.graphics.Color
import com.morph.engine.graphics.Framebuffer
import com.morph.engine.graphics.Texture
import com.morph.engine.graphics.components.Emitter
import com.morph.engine.graphics.components.RenderData
import com.morph.engine.graphics.components.light.Light
import com.morph.engine.graphics.components.light.SceneLight
import com.morph.engine.math.Matrix4f
import com.morph.engine.physics.components.Transform

class BasicTexturedShaderUniforms : Uniforms() {
    private lateinit var mvp: Matrix4f
    private lateinit var diffuse: Texture

    override fun defineUniforms(shader: Int) {
        addUniform("mvp", shader)
        addUniform("diffuse", shader)
    }

    override fun setUniforms(t: Transform, data: RenderData, camera: Camera, screen: Matrix4f, lights: List<Light>) {
        mvp = t.transformationMatrix
        diffuse = data.getTexture(0) ?: throw IllegalStateException("No diffuse texture")

        setUniformMatrix4fv("mvp", camera.modelViewProjection * mvp)
        setUniform1i("diffuse", 0)

        diffuse.bind()
    }

    override fun unbind(t: Transform, data: RenderData) {
        diffuse.unbind()
    }
}

class GUIShaderUniforms : Uniforms() {
    private lateinit var mvp: Matrix4f
    private lateinit var diffuse: Texture

    override fun defineUniforms(shader: Int) {
        addUniform("mvp", shader)
        addUniform("diffuse", shader)
    }

    override fun setUniforms(t: Transform, data: RenderData, camera: Camera, screen: Matrix4f, lights: List<Light>) {
        mvp = t.transformationMatrix
        diffuse = data.getTexture(0) ?: throw IllegalStateException("No diffuse texture")

        setUniformMatrix4fv("mvp", screen * mvp)
        setUniform1i("diffuse", 0)

        diffuse.bind()
    }

    override fun unbind(t: Transform, data: RenderData) {
        diffuse.unbind()
    }
}

class GUITextShaderUniforms : Uniforms() {
    private lateinit var mvp: Matrix4f
    private lateinit var diffuse: Texture

    override fun defineUniforms(shader: Int) {
        addUniform("mvp", shader)
        addUniform("diffuse", shader)
        addUniform("diffuseColor", shader)
    }

    override fun setUniforms(t: Transform, data: RenderData, camera: Camera, screen: Matrix4f, lights: List<Light>) {
        mvp = t.transformationMatrix
        diffuse = data.getTexture(0) ?: throw IllegalStateException("No diffuse texture")

        setUniformMatrix4fv("mvp", screen * mvp)
        setUniform1i("diffuse", 0)
        setUniform3f("diffuseColor", data.tint)

        diffuse.bind()
    }

    override fun unbind(t: Transform, data: RenderData) {
        diffuse.unbind()
    }
}

class GUITintShaderUniforms : Uniforms() {
    private lateinit var mvp: Matrix4f
    private lateinit var diffuse: Texture

    override fun defineUniforms(shader: Int) {
        addUniform("mvp", shader)
        addUniform("diffuse", shader)
        addUniform("diffuseColor", shader)
    }

    override fun setUniforms(t: Transform, data: RenderData, camera: Camera, screen: Matrix4f, lights: List<Light>) {
        mvp = t.transformationMatrix
        diffuse = data.getTexture(0) ?: throw IllegalStateException("No diffuse texture")

        setUniformMatrix4fv("mvp", screen * mvp)
        setUniform1i("diffuse", 0)
        setUniform4f("diffuseColor", data.tint)

        diffuse.bind()
    }

    override fun unbind(t: Transform, data: RenderData) {
        diffuse.unbind()
    }
}

class GUITintTransitionShaderUniforms : Uniforms() {
    private lateinit var mvp: Matrix4f
    private lateinit var diff1: Texture
    private lateinit var diff2: Texture
    private lateinit var diffuseColor: Color
    private var lerpFactor: Float = 0f

    override fun defineUniforms(shader: Int) {
        addUniform("mvp", shader)
        addUniform("diff1", shader)
        addUniform("diff2", shader)
        addUniform("diffuseColor", shader)
        addUniform("lerpFactor", shader)
    }

    override fun setUniforms(t: Transform, data: RenderData, camera: Camera, screen: Matrix4f, lights: List<Light>) {
        this.mvp = t.transformationMatrix
        this.diff1 = data.getTexture(0) ?: throw IllegalStateException("No diffuse texture 1")
        this.diff2 = data.getTexture(1) ?: throw IllegalStateException("No diffuse texture 2")
        this.diffuseColor = data.tint
        this.lerpFactor = data.lerpFactor

        setUniformMatrix4fv("mvp", screen * mvp)
        setUniform1i("diff1", 0)
        setUniform1i("diff2", 1)
        setUniform4f("diffuseColor", diffuseColor)
        setUniform1f("lerpFactor", lerpFactor)

        diff1.bind(0)
        diff2.bind(1)
    }

    override fun unbind(t: Transform, data: RenderData) {
        diff1.unbind()
        diff2.unbind()
    }
}

class GUITransitionShaderUniforms : Uniforms() {
    private lateinit var mvp: Matrix4f
    private lateinit var diff1: Texture
    private lateinit var diff2: Texture
    private var lerpFactor: Float = 0f

    override fun defineUniforms(shader: Int) {
        addUniform("mvp", shader)
        addUniform("diff1", shader)
        addUniform("diff2", shader)
        addUniform("lerpFactor", shader)
    }

    override fun setUniforms(t: Transform, data: RenderData, camera: Camera, screen: Matrix4f, lights: List<Light>) {
        this.mvp = t.transformationMatrix
        this.diff1 = data.getTexture(0) ?: throw IllegalStateException("No diffuse texture 1")
        this.diff2 = data.getTexture(1) ?: throw IllegalStateException("No diffuse texture 2")
        this.lerpFactor = data.lerpFactor

        setUniformMatrix4fv("mvp", screen * mvp)
        setUniform1i("diff1", 0)
        setUniform1i("diff2", 1)
        setUniform1f("lerpFactor", lerpFactor)

        diff1.bind(0)
        diff2.bind(1)
    }

    override fun unbind(t: Transform, data: RenderData) {
        diff1.unbind()
        diff2.unbind()
    }
}

class TextShaderUniforms : Uniforms() {
    private lateinit var mvp: Matrix4f
    private lateinit var diffuse: Texture

    override fun defineUniforms(shader: Int) {
        addUniform("mvp", shader)
        addUniform("diffuse", shader)
        addUniform("diffuseColor", shader)
    }

    override fun setUniforms(t: Transform, data: RenderData, camera: Camera, screen: Matrix4f, lights: List<Light>) {
        mvp = t.transformationMatrix
        diffuse = data.getTexture(0) ?: throw IllegalStateException("No diffuse texture")

        setUniformMatrix4fv("mvp", camera.modelViewProjection * mvp)
        setUniform1i("diffuse", 0)
        setUniform3f("diffuseColor", data.tint)

        diffuse.bind()
    }

    override fun unbind(t: Transform, data: RenderData) {
        diffuse.unbind()
    }
}

class TintShaderUniforms : Uniforms() {
    private lateinit var mvp: Matrix4f
    private lateinit var diffuse: Texture

    override fun defineUniforms(shader: Int) {
        addUniform("mvp", shader)
        addUniform("diffuse", shader)
        addUniform("diffuseColor", shader)
    }

    override fun setUniforms(t: Transform, data: RenderData, camera: Camera, screen: Matrix4f, lights: List<Light>) {
        mvp = t.transformationMatrix
        diffuse = data.getTexture(0) ?: throw IllegalStateException("No diffuse texture")

        setUniformMatrix4fv("mvp", camera.modelViewProjection * mvp)
        setUniform1i("diffuse", 0)
        setUniform4f("diffuseColor", data.tint)

        diffuse.bind()
    }

    override fun unbind(t: Transform, data: RenderData) {
        diffuse.unbind()
    }
}

class TransitionShaderUniforms : Uniforms() {
    private lateinit var mvp: Matrix4f
    private lateinit var diff1: Texture
    private lateinit var diff2: Texture
    private var lerpFactor: Float = 0f

    override fun defineUniforms(shader: Int) {
        addUniform("mvp", shader)
        addUniform("diff1", shader)
        addUniform("diff2", shader)
        addUniform("lerpFactor", shader)
    }

    override fun setUniforms(t: Transform, data: RenderData, camera: Camera, screen: Matrix4f, lights: List<Light>) {
        this.mvp = t.transformationMatrix
        this.diff1 = data.getTexture(0) ?: throw IllegalStateException("No diffuse texture 1")
        this.diff2 = data.getTexture(1) ?: throw IllegalStateException("No diffuse texture 2")
        this.lerpFactor = data.lerpFactor

        setUniformMatrix4fv("mvp", camera.modelViewProjection * mvp)
        setUniform1i("diff1", 0)
        setUniform1i("diff2", 1)
        setUniform1f("lerpFactor", lerpFactor)

        diff1.bind(0)
        diff2.bind(1)
    }

    override fun unbind(t: Transform, data: RenderData) {
        diff1.unbind()
        diff2.unbind()
    }
}

class BasicLightShaderUniforms : Uniforms() {
    private lateinit var mvp: Matrix4f
    private lateinit var diffuse: Texture
    private var normal: Texture? = null

    override fun defineUniforms(shader: Int) {
        addUniform("mvp", shader)
        addUniform("world", shader)
        addUniform("diffuse", shader)
        addUniform("normal", shader)
        addUniform("diffuseColor", shader)
        addUniform("lightPosition", shader)

        for (i in 0..9) {
            addUniform("lights[$i].brightness", shader)
            addUniform("lights[$i].color", shader)
            addUniform("lights[$i].position", shader)
        }
    }

    override fun setUniforms(t: Transform, data: RenderData, camera: Camera, screen: Matrix4f, lights: List<Light>) {
        mvp = t.transformationMatrix
        diffuse = data.getTexture(0) ?: throw IllegalStateException("No diffuse texture")
        normal = data.getTexture(1)

        setUniformMatrix4fv("mvp", camera.modelViewProjection * mvp)
        setUniformMatrix4fv("world", camera.transformationMatrix * mvp)
        setUniform1i("diffuse", 0)
        setUniform1i("normal", 1)
        setUniform4f("diffuseColor", data.tint)

        lights.take(10).mapIndexed { index, light ->
            setUniform1f("lights[$index].brightness", light.brightness)
            setUniform3f("lights[$index].color", light.color)
            if (light is SceneLight) setUniform3f("lights[$index].position", camera.transformationMatrix * light.localPosition)
        }

        diffuse.bind(0)
        normal?.bind(1)
    }

    override fun unbind(t: Transform, data: RenderData) {
        diffuse.unbind()
        normal?.unbind()
    }
}

class InstancedShaderUniforms : Uniforms() {
    private lateinit var diffuse: Texture

    override fun defineUniforms(shader: Int) {
        addUniform("diffuse", shader)
    }

    override fun setUniforms(t: Transform, data: RenderData, camera: Camera, screen: Matrix4f, lights: List<Light>) {
        diffuse = data.getTexture(0) ?: throw IllegalStateException("No diffuse texture")

        setUniform1i("diffuse", 0)

        diffuse.bind()
    }

    fun setUniforms(emitter : Emitter) {
        diffuse = emitter.texture
        setUniform1i("diffuse", 0)
        diffuse.bind()
    }

    override fun unbind(t: Transform, data: RenderData) {
        diffuse.unbind()
    }

    fun unbind() {
        diffuse.unbind()
    }
}

class FramebufferShaderUniforms : Uniforms() {
    private lateinit var diffuse: Texture

    override fun defineUniforms(shader: Int) {
        addUniform("diffuse", shader)
    }

    override fun setUniforms(t: Transform, data: RenderData, camera: Camera, screen: Matrix4f, lights: List<Light>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unbind(t: Transform, data: RenderData) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun setUniforms(framebuffer : Framebuffer) {
        setUniform1i("diffuse", 0)
        framebuffer.bindTexture(0)
    }

    fun unbind(framebuffer : Framebuffer) {
        framebuffer.unbindTextures()
    }
}

