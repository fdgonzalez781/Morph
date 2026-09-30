package com.morph.engine.graphics.components

import com.morph.engine.entities.Component
import com.morph.engine.graphics.Color
import com.morph.engine.graphics.Texture
import com.morph.engine.graphics.Vertex
import com.morph.engine.graphics.shaders.Shader
import com.morph.engine.math.Vector2f
import com.morph.engine.math.Vector3f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import java.util.*
import java.util.function.Consumer

// TODO: Generalize to platform agnostic version
open class RenderData : Component {
    var vertices: MutableList<Vertex>
        protected set
    var indices: MutableList<Int>
        protected set
    protected var textures: HashMap<Int, Texture>

    var shader: Shader<*>
        protected set
    var tint: Color = Color(1f, 1f, 1f)

    var lerpFactor: Float = 0f

    protected var vbo: Int = 0
    protected var cbo: Int = 0
    protected var tbo: Int = 0
    protected var ibo: Int = 0
    var vertexArrayObject: Int = 0

    constructor(shader: Shader<*>, texture: Texture) {
        this.vertices = ArrayList<Vertex>()
        this.indices = ArrayList<Int>()
        this.textures = HashMap<Int, Texture>()

        this.shader = shader

        this.textures[0] = texture

        shader.init()
    }

    constructor(shader: Shader<*>, texture: Texture, vertices: MutableList<Vertex>, indices: MutableList<Int>) {
        this.vertices = vertices
        this.indices = indices
        this.textures = HashMap<Int, Texture>()

        this.shader = shader
        this.textures[0] = texture

        shader.init()
    }

    constructor(
        vertices: MutableList<Vertex>,
        indices: MutableList<Int>,
        shader: Shader<*>,
        textures: HashMap<Int, Texture>,
        vao: Int
    ) {
        this.vertices = vertices
        this.indices = indices
        this.textures = textures

        this.shader = shader

        this.vertexArrayObject = vao
    }

    public override fun init() {
        vbo = GL15.glGenBuffers()
        cbo = GL15.glGenBuffers()
        tbo = GL15.glGenBuffers()
        ibo = GL15.glGenBuffers()

        this.vertexArrayObject = GL30.glGenVertexArrays()

        val pointData = this.pointDataArray
        val colorData = this.colorDataArray
        val texCoordData = this.texCoordDataArray
        val indexData = this.indexDataArray

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, pointData, GL15.GL_STATIC_DRAW)

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, cbo)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorData, GL15.GL_STATIC_DRAW)

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, tbo)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, texCoordData, GL15.GL_STATIC_DRAW)

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo)
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexData, GL15.GL_STATIC_DRAW)

        GL30.glBindVertexArray(this.vertexArrayObject)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo)
        GL20.glEnableVertexAttribArray(0)
        GL20.glVertexAttribPointer(0, 3, GL11.GL_DOUBLE, false, 0, 0)

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, cbo)
        GL20.glEnableVertexAttribArray(1)
        GL20.glVertexAttribPointer(1, 4, GL11.GL_DOUBLE, false, 0, 0)

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, tbo)
        GL20.glEnableVertexAttribArray(2)
        GL20.glVertexAttribPointer(2, 2, GL11.GL_DOUBLE, false, 0, 0)

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo)
        GL30.glBindVertexArray(0)

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0)
    }

    fun refreshVertices() {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, this.pointDataArray, GL15.GL_STATIC_DRAW)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
    }

    fun refreshColors() {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, cbo)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, this.colorDataArray, GL15.GL_STATIC_DRAW)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
    }

    fun refreshTexCoords() {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, tbo)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, this.texCoordDataArray, GL15.GL_STATIC_DRAW)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
    }

    fun refreshIndices() {
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo)
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, this.indexDataArray, GL15.GL_STATIC_DRAW)
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0)
    }

    fun refreshData() {
        refreshVertices()
        refreshColors()
        refreshTexCoords()
        refreshIndices()
    }

    public override fun destroy() {
        shader.removeReference()
    }

    private val pointDataArray: DoubleArray
        get() = vertices.flatMap { it.position.toList().map(Float::toDouble) }.toDoubleArray()

    private val colorDataArray: DoubleArray
        get() = vertices.flatMap { it.color.toList().map(Float::toDouble) }.toDoubleArray()

    private val texCoordDataArray: DoubleArray
        get() = vertices.flatMap { it.texCoord.toList().map(Float::toDouble) }.toDoubleArray()

    private val indexDataArray: IntArray
        get() = indices.stream().mapToInt { obj: Int? -> obj!! }.toArray()

    fun addVertices(vararg vs: Vertex) = updateAll { vertices.addAll(vs) }

    fun addIndices(vararg ids: Int) = updateIndices { loadIndices(*ids) }

    fun loadIndices(vararg ids: Int) {
        indices.addAll(ids.toList())
    }

    fun addVertex(v: Vertex, index: Int) {
        vertices.add(v)
        indices.add(index)
    }

    fun addVertex(v: Vertex) {
        vertices.add(v)
    }

    fun addVertex(position: Vector2f, color: Color, texCoord: Vector2f) {
        vertices.add(Vertex(position, color, texCoord))
    }

    fun addVertex(position: Vector2f, color: Color, index: Int) {
        vertices.add(Vertex(position, color))
        indices.add(index)
    }

    fun addVertex(position: Vector2f, texCoord: Vector2f) {
        vertices.add(Vertex(position, texCoord))
    }

    fun addVertex(position: Vector2f, texCoord: Vector2f, index: Int) {
        vertices.add(Vertex(position, texCoord))
        indices.add(index)
    }

    fun addVertex(position: Vector3f, color: Color) {
        vertices.add(Vertex(position, color))
    }

    fun addVertex(position: Vector2f, color: Color) {
        vertices.add(Vertex(position, color))
    }

    fun addVertex(position: Vector3f, index: Int) {
        vertices.add(Vertex(position, Color(1f, 1f, 1f)))
        indices.add(index)
    }

    fun addVertex(position: Vector2f, index: Int) {
        vertices.add(Vertex(position, Color(1f, 1f, 1f)))
        indices.add(index)
    }

    fun addVertex(position: Vector3f) {
        vertices.add(Vertex(position, Color(1f, 1f, 1f)))
    }

    fun addVertex(position: Vector2f) {
        vertices.add(Vertex(position, Color(1f, 1f, 1f)))
    }

    fun removeVertexAtPosition(i: Int) {
        vertices.removeAt(i)
    }

    fun setVertex(index: Int, v: Vertex) {
        vertices[index] = v
    }

    fun setColor(index: Int, c: Color) {
        vertices[index].color = c
    }

    fun setPosition(index: Int, v: Vector3f) {
        vertices[index].position = v
    }

    fun addIndex(index: Int) {
        indices.add(index)
    }

    fun removeIndexAtPosition(i: Int) {
        indices.removeAt(i)
    }

    fun getTexture(index: Int): Texture? {
        return textures[index]
    }

    fun setTexture(texture: Texture, index: Int) {
        textures[index] = texture
    }

    fun resetAllColors(c: Color) {
        vertices.stream().map { it?.color }.forEach { color: Color? -> color!!.setRGB(c) }
        init()
    }

    fun updateAll(body: RenderData.() -> Unit) = with(this) {
        body()
        refreshData()
    }

    @JvmName("updateAll")
    fun updateAllForJvm(body: Consumer<in RenderData?>) = updateAll {
        body.accept(this)
    }

    fun updateVertices(body: RenderData.() -> Unit) = with(this) {
        body()
        refreshVertices()
    }

    @JvmName("updateVertices")
    fun updateVerticesForJvm(body: Consumer<in RenderData?>) = updateVertices {
        body.accept(this)
    }

    fun updateColors(body: RenderData.() -> Unit) = with(this) {
        body()
        refreshColors()
    }

    @JvmName("updateColors")
    fun updateColorsForJvm(body: Consumer<in RenderData?>) = updateColors {
        body.accept(this)
    }

    fun updateTexCoords(body: RenderData.() -> Unit) = with(this) {
        body()
        refreshTexCoords()
    }

    @JvmName("updateTexCoords")
    fun updateTexCoordsForJvm(body: Consumer<in RenderData?>) = updateTexCoords {
        body.accept(this)
    }

    fun updateIndices(body: RenderData.() -> Unit) = with(this) {
        body()
        refreshIndices()
    }

    @JvmName("updateIndices")
    fun updateIndicesForJvm(body: Consumer<in RenderData?>) = updateIndices {
        body.accept(this)
    }
}
