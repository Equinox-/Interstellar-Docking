package com.pi.gl;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.pi.math.Matrix4;
import com.pi.math.Vector3;

public class BlackHoleEffect {
	private final float powerStrength;
	private final float powerRadius;
	private Vector3 position;

	private RenderTexture renderTexture;

	public BlackHoleEffect(Vector3 position, float strength, float radius) {
		this.position = position;
		this.powerStrength = strength;
		this.powerRadius = radius;
	}

	public void preRender() {
		if (renderTexture == null) {
			renderTexture = new RenderTexture(2048, 2048);
			renderTexture.generate();

			Shaders.BLACK_HOLE.use();
			GL20.glUniform1f(Shaders.BLACK_HOLE.uniform("powerStrength"),
					powerStrength);
			GL20.glUniform1f(Shaders.BLACK_HOLE.uniform("powerRadius"),
					powerRadius);
			GL20.glUniform1i(Shaders.BLACK_HOLE.uniform("color"), 0);
			GL20.glUniform1i(Shaders.BLACK_HOLE.uniform("depth"), 1);
			GL20.glUniform2f(Shaders.BLACK_HOLE.uniform("screenSize"),
					Display.getWidth(), Display.getHeight());
			Shaders.noProgram();
		}
		renderTexture.bindRender();
	}

	private final FloatBuffer tmp = BufferUtils.createFloatBuffer(16);

	public float depth, depthBuffer;

	public void postRender() {
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, tmp);
		Matrix4 mv = new Matrix4(tmp);
		GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, tmp);
		Matrix4 proj = new Matrix4(tmp);
		float[] f = new float[4];
		f[0] = position.x;
		f[1] = position.y;
		f[2] = position.z;
		f[3] = 1;
		f = Matrix4.multiply(mv, f);
		depth = -f[2];
		depthBuffer = (float) (0.5
				* (proj.data.get(10) * f[2] + proj.data.get(14)) / depth + 0.5);
		f = Matrix4.multiply(proj, f);
		f[0] /= f[3];
		f[1] /= f[3];
		f[2] /= f[3];

		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, 1, 0, 1, -1, 1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();

		Shaders.BLACK_HOLE.use();
		GL20.glUniform3f(Shaders.BLACK_HOLE.uniform("blackHole"), f[0], f[1],
				f[2]);
		GL20.glUniform4f(Shaders.BLACK_HOLE.uniform("projParams"),
				proj.data.get(0), proj.data.get(5), proj.data.get(10),
				proj.data.get(14));
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, renderTexture.getColorTexture());
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, renderTexture.getDepthTexture());
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0, 0);
		GL11.glVertex3f(0, 0, 0);
		GL11.glTexCoord2f(1, 0);
		GL11.glVertex3f(1, 0, 0);
		GL11.glTexCoord2f(1, 1);
		GL11.glVertex3f(1, 1, 0);
		GL11.glTexCoord2f(0, 1);
		GL11.glVertex3f(0, 1, 0);
		GL11.glEnd();
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}
}
