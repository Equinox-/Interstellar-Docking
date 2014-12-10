package com.pi.phys;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import com.pi.Main;
import com.pi.gl.Shaders;
import com.pi.math.Matrix4;
import com.pi.math.Vector3;

public class AccretionDisk {
	private static final float EVENT_HORIZON_RAD_2 = 1;
	private static final float PARTICLE_MAX_VEL_2 = 100;
	private static final int PARTICLE_COUNT = 10000;
	private static final int PARTICLE_BINS = 100;

	private final float centralMass, spawnRadius, binDR;
	private final Particle[] particles = new Particle[PARTICLE_COUNT];
	private final Matrix4 transformation;

	private static final Vector3 MAX_TEMP_INTENSITY = new Vector3(.75f, .75f,
			.75f);

	private static final float PARTICLE_SIZE = 3;
	private static final int PARTICLE_RESOLUTION = 10;
	private static FloatBuffer CIRCLE_VERTICES;
	static {
		CIRCLE_VERTICES = BufferUtils
				.createFloatBuffer(PARTICLE_RESOLUTION * 2 + 2);
		CIRCLE_VERTICES.put(0);
		CIRCLE_VERTICES.put(0);
		for (int i = 0; i < PARTICLE_RESOLUTION; i++) {
			float th = (float) (Math.PI * 2 * i / (PARTICLE_RESOLUTION - 1));
			CIRCLE_VERTICES.put(PARTICLE_SIZE * (float) Math.cos(th));
			CIRCLE_VERTICES.put(PARTICLE_SIZE * (float) Math.sin(th));
		}
		CIRCLE_VERTICES.flip();
	}

	public AccretionDisk(final float centralMass, final float spawnRadius,
			final Vector3 center, Vector3 normal) {
		this.centralMass = centralMass;
		this.spawnRadius = spawnRadius;
		this.binDR = spawnRadius * 2.0f / PARTICLE_BINS;

		normal = Vector3.normalize(normal); // Don't trust the user
		Vector3 cotangent = Vector3.normalize(Vector3.cross(normal,
				new Vector3(13, 89, 6)));
		Vector3 tangent = Vector3.cross(cotangent, normal);

		Matrix4 rotation = Matrix4.identity();
		rotation.data.put(0, tangent.x);
		rotation.data.put(4, tangent.y);
		rotation.data.put(8, tangent.z);

		rotation.data.put(1, cotangent.x);
		rotation.data.put(5, cotangent.y);
		rotation.data.put(9, cotangent.z);

		rotation.data.put(2, normal.x);
		rotation.data.put(6, normal.y);
		rotation.data.put(10, normal.z);

		this.transformation = Matrix4.multiply(rotation,
				Matrix4.translation(center));

		for (int i = 0; i < particles.length; i++) {
			particles[i] = new Particle();
		}

		update();
	}

	private float[] frontBins = new float[PARTICLE_BINS],
			backBins = new float[PARTICLE_BINS];

	public void update() {
		for (int i = 0; i < PARTICLE_BINS; i++)
			backBins[i] = 0;

		final float dt = (float) Main.getDelta();
		for (int i = 0; i < particles.length; i++) {
			float pos2 = Vector3.mag2(particles[i].pos);
			float vel2 = Vector3.mag2(particles[i].vel);
			if (pos2 < EVENT_HORIZON_RAD_2 * centralMass
					|| vel2 > PARTICLE_MAX_VEL_2) {
				// Respawn
				float angleVariance = (float) Math.random();
				float spawnAngle = (float) (angleVariance * Math.PI * 2);
				float radiusVariance = (float) Math.random();
				float spawnDistance = spawnRadius
						* (radiusVariance * 0.2f + 0.9f);
				float cos = (float) Math.cos(spawnAngle);
				float sin = (float) Math.sin(spawnAngle);
				particles[i].pos = new Vector3(cos * spawnDistance, sin
						* spawnDistance, spawnRadius * 0.025f
						* ((float) Math.random() * 2 - 1));
				pos2 = spawnDistance * spawnDistance;
				float vel = (float) (Math.sqrt(spawnDistance * centralMass
						/ pos2) * (Math.random() * 0.2f + 0.9f));
				particles[i].vel = new Vector3(-sin * vel, cos * vel, 0);
			}
			float pos = (float) Math.sqrt(pos2);

			// Add to density bins
			float binValue = 0;
			float bin = pos / binDR;
			int lBin = (int) Math.floor(bin);
			if (lBin < PARTICLE_BINS - 1 && lBin >= 0) {
				backBins[lBin] += (bin - lBin);
				backBins[lBin + 1] += 1 - (bin - lBin);
				binValue = frontBins[lBin] * (1 - (bin - lBin))
						/ (lBin * binDR) + frontBins[lBin + 1] * (bin - lBin)
						/ (lBin * binDR + binDR);
			}

			// Integrate particle
			float effectiveDt = dt;// dt / spawnRadius * pos;
			Vector3.addto(particles[i].vel, particles[i].pos, -dt * centralMass
					/ pos2 / pos);
			Vector3.addto(particles[i].pos, particles[i].vel, effectiveDt);

			float mix = (float) Math.pow(binValue * 1E3 / PARTICLE_COUNT, 0.5);
			particles[i].color = Vector3.lincom(new Vector3(1, 0, 0), 1 - mix,
					MAX_TEMP_INTENSITY, mix);
		}

		float[] tmp = frontBins;
		frontBins = backBins;
		backBins = tmp;
	}

	private int vboHandle = -1;

	public void render() {
		if (vboHandle == -1) {
			vboHandle = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboHandle);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, CIRCLE_VERTICES,
					GL15.GL_STATIC_DRAW);
		}

		GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT);
		GL11.glPushMatrix();
		GL11.glMultMatrix(transformation.data);
		Shaders.ACCRETION_DISK.use();
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		Matrix4 iRot = new Matrix4();
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, iRot.data);
		iRot = Matrix4.transpose(Matrix4.mat3(iRot));

		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboHandle);
		GL11.glVertexPointer(2, GL11.GL_FLOAT, 0, 0);

		// First render with the depth mask disabled, but color mask enabled
		GL11.glDepthMask(false);
		renderParticles(iRot);
		GL11.glDepthMask(true);
		GL11.glColorMask(false, false, false, false);
		renderParticles(iRot);

		GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glPopMatrix();
		GL11.glPopAttrib();
	}

	private void renderParticles(Matrix4 iRot) {
		for (int i = 0; i < particles.length; i++) {
			GL11.glColor4f(particles[i].color.x, particles[i].color.y,
					particles[i].color.z, 0.1f);
			GL11.glPushMatrix();
			GL11.glTranslatef(particles[i].pos.x, particles[i].pos.y,
					particles[i].pos.z);
			GL11.glMultMatrix(iRot.data);

			GL11.glDrawArrays(GL11.GL_TRIANGLE_FAN, 0, PARTICLE_RESOLUTION + 1);
			GL11.glPopMatrix();
		}
	}
}
