package com.morph.engine.graphics;

import com.morph.engine.core.Camera;
import com.morph.engine.core.GameSystem;
import com.morph.engine.core.World;
import com.morph.engine.entities.Component;
import com.morph.engine.entities.Entity;
import com.morph.engine.graphics.components.Emitter;
import com.morph.engine.graphics.components.RenderData;
import com.morph.engine.graphics.components.light.Light;
import com.morph.engine.math.Matrix4f;
import com.morph.engine.math.MatrixUtils;
import com.morph.engine.newgui.Element;
import com.morph.engine.physics.components.Transform;
import com.morph.engine.physics.components.Transform2D;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;
import static org.lwjgl.system.MemoryUtil.NULL;

// TODO: Generalize to platform agnostic version
// TODO: Migrate to Kotlin

public class GLRenderingEngine extends GameSystem {
	private Matrix4f screenProjection;
	private Camera camera;
	private List<Light> lights;
	private RenderBatcher batcher;
	private List<Emitter> emitters;
	private Framebuffer activeFramebuffer;

	public GLRenderingEngine(World world, int width, int height) {
		super(world);
		this.lights = new ArrayList<>();
		this.emitters = new ArrayList<>();
		this.batcher = new RenderBatcher();
		this.screenProjection = MatrixUtils.getOrthographicProjectionMatrix(height, 0, 0, width, -1, 1);
	}

	private void render(RenderData data, Transform transform) {
		if (data == null || transform == null)
			return;

		data.getShader().bind();
		data.getShader().getUniforms().setUniforms(transform, data, camera, screenProjection, lights); // TODO: Generate UBO instead of setting uniforms

		glBindVertexArray(data.getVertexArrayObject());
		glDrawElements(GL_TRIANGLES, data.getIndices().size(), GL_UNSIGNED_INT, NULL);
		glBindVertexArray(0);

		data.getShader().getUniforms().unbind(transform, data);
		data.getShader().unbind();
	}

	private void render(Emitter emitter) {
		double[] colors = emitter.stream().flatMapToDouble(particle -> Arrays.stream(particle.getColor().toDoubleArray())).toArray();
		double[] transforms = emitter.stream().map(particle -> camera.getProjectionMatrix().mul(particle.getParent().getComponent(Transform2D.class).getTransformationMatrix()).getTranspose())
				.flatMapToDouble(matrix -> Arrays.stream(matrix.toDoubleArray())).toArray();

		glBindBuffer(GL_ARRAY_BUFFER, emitter.getColorBuffer());
		glBufferData(GL_ARRAY_BUFFER, colors, GL_DYNAMIC_DRAW);

		glBindBuffer(GL_ARRAY_BUFFER, emitter.getTransformBuffer());
		glBufferData(GL_ARRAY_BUFFER, transforms, GL_DYNAMIC_DRAW);

		glBindBuffer(GL_ARRAY_BUFFER, 0);

		emitter.getShader().bind();
		emitter.getShader().getUniforms().setUniforms(emitter);

		glBindVertexArray(emitter.getVao());
		glDrawElementsInstanced(GL_TRIANGLES, 6, GL_UNSIGNED_INT, NULL, emitter.size());
		glBindVertexArray(0);

		emitter.getShader().getUniforms().unbind();
		emitter.getShader().unbind();
	}

	private void render(Entity e) {
		render(e.getComponent(RenderData.class), e.getComponent(Transform.class));
	}

	private void render(Element e) {
		render(e.getRenderData(), e.getTransform());
	}

	public void register(Entity e) {
		if (e.hasComponents(RenderData.class, Transform2D.class)) {
			Renderable.REntity renderable = new Renderable.REntity(e);
			batcher.add(renderable);
		}

		if (e.hasComponent(Emitter.class)) {
			emitters.add(e.getComponent(Emitter.class));
		}
	}

	public void unregister(Entity e) {
		batcher.remove(e);

		if (e.hasComponent(Emitter.class)) {
			emitters.remove(e.getComponent(Emitter.class));
		}
	}

	public void register(Element e) {
		Renderable.RElement renderable = new Renderable.RElement(e);
		batcher.add(renderable);
	}

	public void unregister(Element e) {
		batcher.remove(e);
	}

	public void addLight(Light l) {
		lights.add(l);
	}
	public void removeLight(Light l) {
		lights.remove(l);
	}

	public void render(GLDisplay display) {
		if (activeFramebuffer != null) {
			activeFramebuffer.bindRenderTargets();
		}

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		batcher.forEach((shader, renderBucket) -> {
			shader.bind();
			renderBucket.stream().filter(r -> r instanceof Renderable.REntity).forEach(this::render);
			shader.unbind();
		});

		batcher.forEach((shader, renderBucket) -> {
			shader.bind();
			renderBucket.stream().filter(r -> r instanceof Renderable.RElement).forEach(this::render);
			shader.unbind();
		});

		glBlendFunc(GL_SRC_ALPHA, GL_ONE);
		emitters.forEach(this::render);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		if (activeFramebuffer != null) {
			activeFramebuffer.unbindRenderTargets();

			glClear(GL_COLOR_BUFFER_BIT);

			activeFramebuffer.getShader().bind();
			activeFramebuffer.getShader().getUniforms().setUniforms(activeFramebuffer);

			glBindVertexArray(activeFramebuffer.getVao());
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, NULL);
			glBindVertexArray(0);

			activeFramebuffer.getShader().getUniforms().unbind(activeFramebuffer);
			activeFramebuffer.getShader().unbind();
		}

		display.update();
	}

	private void render(Renderable renderable) {
		RenderData data = renderable.getRenderData();
		Transform transform = renderable.getTransform();

		data.getShader().getUniforms().setUniforms(transform, data, camera, screenProjection, lights); // TODO: Generate UBO instead of setting uniforms

		glBindVertexArray(data.getVertexArrayObject());
		glDrawElements(GL_TRIANGLES, data.getIndices().size(), GL_UNSIGNED_INT, NULL);
		glBindVertexArray(0);

		data.getShader().getUniforms().unbind(transform, data);
	}

	public void setClearColor(Color clearColor) {
		glClearColor(clearColor.getRed(), clearColor.getGreen(), clearColor.getBlue(), clearColor.getAlpha());
	}

	@Override
	public void initSystem() {
		glActiveTexture(GL_TEXTURE0);
		glEnable(GL_TEXTURE_2D);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	public void setClearColor(float r, float g, float b, float a) {
		glClearColor(r, g, b, a);
	}

	@Override
	protected boolean acceptEntity(Entity e) {
		return e.hasComponents(RenderData.class, Transform.class);
	}

	public void setActiveFramebuffer(Framebuffer activeFramebuffer) {
		this.activeFramebuffer = activeFramebuffer;
	}

	@Override
	public @NotNull List<@NotNull Class<? extends @NotNull Component>> getRequiredComponents() {
		return Arrays.asList(Transform.class, RenderData.class);
	}
}
