package com.pi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.PixelFormat;

import com.pi.gl.Camera;
import com.pi.gl.Shaders;
import com.pi.math.Vector3;
import com.pi.model.Mesh;
import com.pi.model.Texture;
import com.pi.phys.Ship;
import com.pi.util.LEInputStream;

public class Main {
	public static final File dataDir = new File("data");

	private static double physDelta = 0;

	private static final long initTime = System.currentTimeMillis();

	public static double getTime() {
		return 4 * (System.currentTimeMillis() - initTime) / 1000.0;
	}

	public static double getDelta() {
		return physDelta;
	}

	private static final float HORIZ_FOV = 120;

	private static final float PLANET_SIZE = 1000;
	private static final float ATMOSPHERE_SCALE = 1.075f;
	private static final Vector3 PLANET_PLACE = new Vector3(-PLANET_SIZE
			* ATMOSPHERE_SCALE * .7f, -PLANET_SIZE * ATMOSPHERE_SCALE * .7f,
			-PLANET_SIZE * ATMOSPHERE_SCALE * .7f);

	public Main() throws LWJGLException, IOException {
		Display.setDisplayMode(new DisplayMode(1920, 1080));
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

	private Texture planetTexture;
	private Texture planetSpecular;
	private Mesh planet;

	private Ship endurance, ranger;

	private Camera camera;

	private void load() throws IOException {
		camera = new Camera();

		{
			LEInputStream in = new LEInputStream(new FileInputStream(new File(
					dataDir, "sphere.lpack")));
			planet = new Mesh(in);
			in.close();
			planetTexture = new Texture(new File(dataDir, "tex/ice_planet.png"));
			planetSpecular = new Texture(new File(dataDir,
					"tex/ice_planet.spec.png"));
		}

		endurance = new Ship(new File(dataDir, "endurance.pack"));
		ranger = new Ship(new File(dataDir, "ranger.pack"));
	}

	private void init() {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_LIGHT0);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glCullFace(GL11.GL_BACK);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, LIGHT0_DIFFUSE);
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_SPECULAR, LIGHT0_SPECULAR);
		GL11.glLightModel(GL11.GL_AMBIENT, LIGHT_AMBIENT);
		// GL11.glClearColor(1, 1, 1, 1);
	}

	private void renderPlanet() {
		GL11.glPushMatrix();
		GL11.glTranslatef(PLANET_PLACE.x, PLANET_PLACE.y, PLANET_PLACE.z);
		GL11.glRotatef((float) getTime() / 10.0f, 0, 1, 0);
		{
			Shaders.PLANET.use();
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			planetTexture.bind();
			GL13.glActiveTexture(GL13.GL_TEXTURE1);
			planetSpecular.bind();
			GL11.glScalef(PLANET_SIZE, PLANET_SIZE, PLANET_SIZE);
			planet.render();
			Shaders.noProgram();
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_LIGHTING);
			Shaders.ATMOSPHERE.use();
			GL11.glScalef(ATMOSPHERE_SCALE, ATMOSPHERE_SCALE, ATMOSPHERE_SCALE);
			planet.render();
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_BLEND);
		}
		GL11.glPopMatrix();
	}

	private static final int[] GROUP_CTL = { Keyboard.KEY_D, Keyboard.KEY_A,
			Keyboard.KEY_W, Keyboard.KEY_S, Keyboard.KEY_Q, Keyboard.KEY_E,
			Keyboard.KEY_J, Keyboard.KEY_L, Keyboard.KEY_I, Keyboard.KEY_K,
			Keyboard.KEY_U, Keyboard.KEY_O };

	private static final FloatBuffer LIGHT0_POSITION = (FloatBuffer) BufferUtils
			.createFloatBuffer(4).put(new float[] { 100, 100, 100, 1 })
			.rewind();
	private static final FloatBuffer LIGHT0_DIFFUSE = (FloatBuffer) BufferUtils
			.createFloatBuffer(4).put(new float[] { 1, 1, 1, 1 }).rewind();
	private static final FloatBuffer LIGHT0_SPECULAR = (FloatBuffer) BufferUtils
			.createFloatBuffer(4).put(new float[] { 1, 1, 1, 1 }).rewind();
	private static final FloatBuffer LIGHT_AMBIENT = (FloatBuffer) BufferUtils
			.createFloatBuffer(4).put(new float[] { .1f, .1f, .1f, 1 })
			.rewind();

	private void run() {
		double lastLoop = getTime();
		while (!Display.isCloseRequested()) {
			windowResized(Display.getWidth(), Display.getHeight());

			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glLoadIdentity();
			camera.glApply();

			GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, LIGHT0_POSITION);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

			Shaders.SHIP.use();
			endurance.render();
			ranger.render();

			renderPlanet();

			Display.update();
			physDelta = getTime() - lastLoop;
			lastLoop = getTime();

			physics();
			Display.sync(60);
		}
	}

	private float thrusterBasePower = 10;
	private boolean swapLastState;
	private Ship control;

	private void physics() {
		camera.process();

		if (control == null) {
			thrusterBasePower = 10;
			control = ranger;
		}

		control.zeroThrusters();
		for (int i = 0; i < GROUP_CTL.length; i++)
			if (Keyboard.isKeyDown(GROUP_CTL[i]))
				control.addGroup(i, thrusterBasePower);

		if (Keyboard.isKeyDown(Keyboard.KEY_B)) {
			control.addWorldThrust(new Vector3(thrusterBasePower, 0, 0));
		} else if (Keyboard.isKeyDown(Keyboard.KEY_N)) {
			control.addWorldThrust(new Vector3(-thrusterBasePower, 0, 0));
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_TAB)) {
			if (!swapLastState) {
				if (control == endurance) {
					control = ranger;
					thrusterBasePower = 10;
				} else if (control == ranger) {
					control = endurance;
					thrusterBasePower = 1000;
				}
			}
			swapLastState = true;
		} else
			swapLastState = false;

		endurance.update();
		ranger.update();
	}

	public static void main(String[] args) throws LWJGLException, IOException {
		new Main();
	}
}
