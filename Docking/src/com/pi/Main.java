package com.pi;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;

import com.pi.gl.BlackHoleEffect;
import com.pi.gl.Camera;
import com.pi.gl.MatrixStack;
import com.pi.gl.Shaders;
import com.pi.math.Vector3;
import com.pi.model.Texture;
import com.pi.phys.AccretionDisk;
import com.pi.phys.CelestialBody;
import com.pi.phys.Ship;

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

	public Main() throws LWJGLException, IOException {
		Display.setDisplayMode(new DisplayMode(1280, 720));
		Display.setTitle("Docking");
		Display.create(new PixelFormat(8, 8, 0, 8));

		load();
		init();
		run();
	}

	private void windowResized(int width, int height, float near) {
		GL11.glViewport(0, 0, width, height);
		final float tanV = (float) Math.tan(HORIZ_FOV * Math.PI / 360.0);
		final float aspect = height / (float) width;
		MatrixStack.glFrustum(-tanV * near, tanV * near, -tanV * aspect * near,
				tanV * aspect * near, near, near + 10000);
	}

	private CelestialBody planet;

	private Ship endurance, ranger;

	private Camera camera;

	private Vector3 bhole = new Vector3(1000, 1000, -1000);
	private BlackHoleEffect effect = new BlackHoleEffect(bhole, 100);
	private AccretionDisk disk = new AccretionDisk(5000, 100, bhole,
			new Vector3(1, 0, 0));

	private void load() throws IOException {
		camera = new Camera();
		camera.offset = -3;

		{
			planet = new CelestialBody(1000, 1.075f, 1200, new Vector3(-1000,
					-1000, -1000), new Texture(new File(dataDir,
					"tex/ice_planet.png")), new Texture(new File(dataDir,
					"tex/ice_planet.spec.png")));
		}

		endurance = new Ship(new File(dataDir, "endurance.pack"));
		ranger = new Ship(new File(dataDir, "lander.pack"));
	}

	private void init() {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_LIGHT0);
		// GL11.glCullFace(GL11.GL_BACK);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, LIGHT0_DIFFUSE);
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_SPECULAR, LIGHT0_SPECULAR);
		GL11.glLightModel(GL11.GL_AMBIENT, LIGHT_AMBIENT);
		// GL11.glClearColor(1, 1, 1, 1);
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
		long lastSecond = System.currentTimeMillis();
		int frames = 0;
		while (!Display.isCloseRequested()) {

			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			{// Accretion disk rendering
				windowResized(Display.getWidth(), Display.getHeight(),
						Math.max(10, effect.depth / 2 - disk.spawnRadius));
				effect.preRender();
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT
						| GL11.GL_DEPTH_BUFFER_BIT);
				MatrixStack.glLoadIdentity();
				camera.glApply();
				MatrixStack.glPushMatrix();
				disk.render();
				MatrixStack.glPopMatrix();
				effect.postRender();
			}
			{// Old stuff
				GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
				windowResized(Display.getWidth(), Display.getHeight(), 1);
				doRender();
			}

			Display.update();

			physDelta = getTime() - lastLoop;
			lastLoop = getTime();
			physics();
			Display.sync(60);
			frames++;
			if (lastSecond + 1000 < System.currentTimeMillis()) {
				Display.setTitle((frames * 1000 / (System.currentTimeMillis() - lastSecond))
						+ " FPS");
				lastSecond = System.currentTimeMillis();
				frames = 0;
			}
		}
	}

	private void doRender() {
		MatrixStack.glLoadIdentity();
		camera.glApply();
		MatrixStack.glPushMatrix();
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, LIGHT0_POSITION);

		endurance.render();
		ranger.render();

		planet.render();
		MatrixStack.glPopMatrix();
	}

	private float thrusterBasePower = 10;
	private boolean swapLastState;
	private Ship control;

	private void physics() {
		disk.update();
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
