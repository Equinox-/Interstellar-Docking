package com.pi;

import java.io.File;
import java.io.IOException;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.PixelFormat;

import com.pi.gl.BlackHoleEffect;
import com.pi.gl.Camera;
import com.pi.gl.RenderTexture;
import com.pi.gl.Shaders;
import com.pi.math.Vector3;

public class GravDistort {
	public static final File dataDir = new File("data");

	private static final float HORIZ_FOV = 120;

	public GravDistort() throws LWJGLException, IOException {
		Display.setDisplayMode(new DisplayMode(1280, 720));
		Display.setTitle("Docking");
		Display.create(new PixelFormat(8, 8, 0, 8));

		load();
		init();
		run();
	}

	private void windowResized(int width, int height) {
		GL11.glViewport(0, 0, width, height);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		final float tanV = (float) Math.tan(HORIZ_FOV * Math.PI / 360.0);
		final float aspect = height / (float) width;
		GL11.glFrustum(-tanV, tanV, -tanV * aspect, tanV * aspect, 1, 1000);
	}

	private Camera camera;

	private void load() throws IOException {
		camera = new Camera();
		camera.offset = -3;
	}

	private void init() {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		// GL11.glEnable(GL11.GL_BLEND);
		// GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}

	private BlackHoleEffect effect = new BlackHoleEffect(new Vector3(), 10000,
			10000);

	private void run() {
		while (!Display.isCloseRequested()) {
			windowResized(Display.getWidth(), Display.getHeight());

			effect.preRender();

			Shaders.noProgram();
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glLoadIdentity();
			camera.glApply();

			GL11.glClearColor(0, 0, 0, 1);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glBegin(GL11.GL_QUADS);
			for (float z = -1; z <= 1; z += 2) {
				GL11.glColor3f((z + 1) / 2, 1 - ((z + 1) / 2), 0);
				for (float x = -1; x < 1; x += 0.2) {
					for (float y = -1; y < 1; y += 0.2) {
						GL11.glVertex3f(x, y, z);
						GL11.glVertex3f(x + .1f, y, z);
						GL11.glVertex3f(x + .1f, y + .1f, z);
						GL11.glVertex3f(x, y + .1f, z);
					}
				}
			}
			GL11.glEnd();
			GL11.glFlush();

			effect.postRender();

			Display.update();
			camera.process();
			Display.sync(60);
		}
	}

	public static void main(String[] args) throws LWJGLException, IOException {
		new GravDistort();
	}
}
