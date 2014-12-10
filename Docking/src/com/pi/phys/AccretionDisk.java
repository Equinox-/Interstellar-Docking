package com.pi.phys;

import org.lwjgl.opengl.GL11;

import com.pi.Main;
import com.pi.gl.Camera;
import com.pi.gl.Shaders;
import com.pi.math.Matrix4;
import com.pi.math.Vector3;

public class AccretionDisk {
	private static final float EVENT_HORIZON_RAD_2 = 1;
	private static final float PARTICLE_MAX_VEL_2 = 100;

	private final float centralMass, spawnRadius;
	private final Particle[] particles = new Particle[10000];
	private final Vector3 center;
	private final Vector3 normal;

	public AccretionDisk(final float centralMass, final float spawnRadius,
			final Vector3 center, final Vector3 normal) {
		this.centralMass = centralMass;
		this.spawnRadius = spawnRadius;
		this.center = center;
		this.normal = normal;

		for (int i = 0; i < particles.length; i++) {
			particles[i] = new Particle();
		}

		update();
	}

	public void update() {
		final float dt = (float) Main.getDelta();
		float p2 = Float.MAX_VALUE;
		for (int i = 0; i < particles.length; i++) {
			float pos2 = Vector3.mag2(particles[i].pos);
			p2 = Math.min(p2, pos2);
			if (pos2 < EVENT_HORIZON_RAD_2 * centralMass
					|| Vector3.mag2(particles[i].vel) > PARTICLE_MAX_VEL_2) {
				// Respawn
				float spawnAngle = (float) (Math.random() * Math.PI * 2);
				float spawnDistance = (float) (spawnRadius * (Math.random() * 0.2f + 0.9f));
				float cos = (float) Math.cos(spawnAngle);
				float sin = (float) Math.sin(spawnAngle);
				particles[i].pos = new Vector3(cos * spawnDistance, sin
						* spawnDistance, 0);
				pos2 = spawnDistance * spawnDistance;
				float vel = (float) (Math.sqrt(spawnDistance * centralMass
						/ pos2) * (Math.random() * 0.2f + 0.9f));
				particles[i].vel = new Vector3(-sin * vel, cos * vel, 0);
			}
			// Integrate particle
			float pos = (float) Math.sqrt(pos2);
			float effectiveDt = dt;// dt / spawnRadius * pos;
			Vector3.addto(particles[i].vel, particles[i].pos, -dt * centralMass
					/ pos2 / pos);
			Vector3.addto(particles[i].pos, particles[i].vel, effectiveDt);
		}
	}

	public void render() {
		GL11.glPushMatrix();
		GL11.glTranslatef(center.x, center.y, center.z);
		Shaders.ACCRETION_DISK.use();
		GL11.glPointSize(25);
		GL11.glEnable(GL11.GL_BLEND);
//		Matrix4 iRot = Matrix4.transpose(Matrix4.mat3(Camera.curr.pose));
		GL11.glBegin(GL11.GL_POINTS);
		GL11.glColor4f(1, 0, 0, 0.1f);
		final float psize = 25f;
		for (int i = 0; i < particles.length; i++) {
			GL11.glPushMatrix();
//			GL11.glTranslatef(particles[i].pos.x, particles[i].pos.y,
//					particles[i].pos.z);
//			GL11.glMultMatrix(iRot.data);
//			GL11.glVertex2f(-psize, -psize);
			GL11.glVertex2f(0, 0);
//			GL11.glVertex2f(psize, -psize);
//			GL11.glVertex2f(psize, psize);
//			GL11.glVertex2f(-psize, psize);
			GL11.glVertex3f(particles[i].pos.x, particles[i].pos.y,
					particles[i].pos.z);
			GL11.glPopMatrix();
		}
		GL11.glEnd();
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glPopMatrix();
	}
}
