package com.pi.phys;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

import org.lwjgl.opengl.GL11;

import com.pi.Main;
import com.pi.gl.MatrixStack;
import com.pi.gl.Shaders;
import com.pi.math.Matrix4;
import com.pi.math.Quaternion;
import com.pi.math.Vector3;
import com.pi.model.Model;

public class Ship {
	public static final int CONTROL_GROUP_COUNT = 12;
	private static final int PARTICLE_COUNT = 10000;
	private static final float PARTICLE_MASS = 10.0f;
	private static final float PARTICLE_LIFE = 5;

	Quaternion rot;
	private Matrix4 rotMatrix, rotInverse;

	Vector3 angularMomentum;
	Vector3 pos, linearMomentum;

	// Thurster params
	private Vector3[] thrusterCross;
	private Vector3[] thrusterPos;
	private Vector3[] thrusterDir;
	private float[] thrusterPower;

	// Particle system
	private final Particle[] particles = new Particle[PARTICLE_COUNT];
	private int particleHead;

	// Control groups
	private final int[][] controlGroups = new int[CONTROL_GROUP_COUNT][];

	private Model model;

	private void loadThrusters(File thr) throws IOException {
		Scanner f = new Scanner(new FileInputStream(thr));

		thrusterPower = new float[f.nextInt()];
		thrusterPos = new Vector3[thrusterPower.length];
		thrusterDir = new Vector3[thrusterPower.length];
		thrusterCross = new Vector3[thrusterPower.length];

		for (int t = 0; t < thrusterPower.length; t++) {
			thrusterPower[t] = 0;
			thrusterPos[t] = new Vector3(f.nextFloat(), f.nextFloat(),
					f.nextFloat());
			// Makes the THR file simpler
			Vector3.addto(thrusterPos[t], model.getCOM(), 1);

			thrusterDir[t] = new Vector3(f.nextFloat(), f.nextFloat(),
					f.nextFloat());
			thrusterCross[t] = Vector3.cross(thrusterDir[t],
					Vector3.lincom(thrusterPos[t], 1, model.getCOM(), -1));
		}

		for (int i = 0; i < CONTROL_GROUP_COUNT; i++) {
			controlGroups[i] = new int[f.nextInt()];
			for (int j = 0; j < controlGroups[i].length; j++)
				controlGroups[i][j] = f.nextInt();
		}

		f.close();
	}

	public Ship(File f) throws IOException {
		pos = new Vector3();
		linearMomentum = new Vector3();
		rot = new Quaternion();
		rot.v = new Vector3();
		rot.w = 1;
		angularMomentum = new Vector3();
		rotMatrix = Matrix4.from_quat(rot);
		rotInverse = Matrix4.invert(rotMatrix);

		for (int i = 0; i < particles.length; i++) {
			particles[i] = new Particle();
		}

		model = new Model(f.getAbsolutePath());

		loadThrusters(new File(f.getAbsolutePath() + ".thr"));
	}

	public void render() {
		MatrixStack.glPushMatrix();
		MatrixStack.glTranslatef(pos.x, pos.y, pos.z);
		MatrixStack.glMultMatrix(rotMatrix);
		MatrixStack.glTranslatef(-model.getCOM().x, -model.getCOM().y,
				-model.getCOM().z);

		Shaders.SHIP.use();
		MatrixStack.commitProjection();
		model.render();
		MatrixStack.commitModelview(); // Make sure we backed out

		Shaders.noProgram();

		// Render thrusters
//		GL11.glDisable(GL11.GL_LIGHTING);
//		GL11.glBegin(GL11.GL_LINES);
//		for (int t = 0; t < thrusterPower.length; t++) {
//			GL11.glColor3f(0, 1, 0);
//			GL11.glVertex3f(thrusterPos[t].x, thrusterPos[t].y,
//					thrusterPos[t].z);
//			GL11.glColor3f(1, 0, 0);
//			final float drawMag = thrusterPower[t] / 100.0f;
//			GL11.glVertex3f(thrusterPos[t].x + drawMag * thrusterDir[t].x,
//					thrusterPos[t].y + drawMag * thrusterDir[t].y,
//					thrusterPos[t].z + drawMag * thrusterDir[t].z);
//		}
//		GL11.glEnd();
		MatrixStack.glPopMatrix();

//		GL11.glPointSize(3);
//		GL11.glBegin(GL11.GL_POINTS);
//		final float ctime = (float) Main.getTime();
//		for (int i = 0; i < PARTICLE_COUNT; i++)
//			if (particles[i].end > ctime)
//				particles[i].render();
//		GL11.glEnd();
//		GL11.glPointSize(1);
//		GL11.glEnable(GL11.GL_LIGHTING);
	}

	public void update() {
		final float delta = (float) Main.getDelta();
		final float ctime = (float) Main.getTime();

		Quaternion omega = new Quaternion();
		omega.w = 0;
		omega.v = Matrix4.multiply(model.getInertiaTensorInverse(),
				angularMomentum);

		// All da others
		for (int t = 0; t < thrusterPower.length; t++) {
			if (thrusterPower[t] > 0) {
				final Vector3 patchDir = Matrix4.multiply(rotMatrix,
						thrusterDir[t]);
				Vector3.addto(linearMomentum, patchDir, delta
						* thrusterPower[t]);
				Vector3.addto(angularMomentum,
						Matrix4.multiply(rotMatrix, thrusterCross[t]), delta
								* thrusterPower[t]);

				final Vector3 patchPos = Matrix4.multiply(rotMatrix,
						Vector3.lincom(thrusterPos[t], 1, model.getCOM(), -1));
				particles[particleHead].pos = Vector3.lincom(pos, 1, patchPos,
						1);
				particles[particleHead].vel = Vector3.lincom(linearMomentum,
						1.0f / model.getMass(), patchDir, 0.1f
								* thrusterPower[t] / PARTICLE_MASS,
						Vector3.cross(omega.v, patchPos), 1);
				particles[particleHead].begin = ctime;
				particles[particleHead].end = ctime + PARTICLE_LIFE;
				particles[particleHead].color.x = particles[particleHead].color.y = particles[particleHead].color.z = 1;
				particleHead++;
				if (particleHead >= PARTICLE_COUNT)
					particleHead = 0;
			}
		}

		Vector3.addto(pos, linearMomentum, 1.0f / model.getMass());

		Quaternion.addto(rot, Quaternion.multiply(omega, rot), 0.5f * delta);
		Quaternion.normalizeHere(rot);

		rotMatrix = Matrix4.from_quat(rot);
		rotInverse = Matrix4.invert(rotMatrix);
	}

	public final void setThruster(final int t, final float power) {
		thrusterPower[t] = power;
	}

	public final void addGroup(final int group, final float power) {
		int[] ctl = controlGroups[group];
		if (ctl == null)
			return;
		for (int t = 0; t < ctl.length; t++)
			thrusterPower[ctl[t]] += power / ctl.length;
	}

	public final void addWorldThrust(Vector3 power) {
		// World -> local
		Vector3 lv = Matrix4.multiply(rotInverse, power);

		addGroup(lv.x > 0 ? 2 : 3, Math.abs(lv.x));
		addGroup(lv.y > 0 ? 4 : 5, Math.abs(lv.y));
		addGroup(lv.z > 0 ? 6 : 7, Math.abs(lv.z));
	}

	public final void zeroThrusters() {
		for (int t = 0; t < thrusterPower.length; t++)
			thrusterPower[t] = 0;
	}
}
