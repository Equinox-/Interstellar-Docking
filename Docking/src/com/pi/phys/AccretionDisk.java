package com.pi.phys;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import com.pi.Main;
import com.pi.gl.MatrixStack;
import com.pi.gl.Shaders;
import com.pi.math.Matrix4;
import com.pi.math.Vector3;

public class AccretionDisk {
	private static final float EVENT_HORIZON_RAD_2 = 1;
	private static final float PARTICLE_MAX_VEL_2 = 100;
	private static final int PARTICLE_COUNT = 10000;
	private static final int PARTICLE_BINS = 100;

	private final float centralMass, spawnRadius, binDR;
	private final Matrix4 transformation;

	private static final Vector3 MAX_TEMP_INTENSITY = new Vector3(.75f, .75f,
			.75f);

	private static final float PARTICLE_SIZE = 3;
	private static final int PARTICLE_RESOLUTION = 10;// 10;
	private static final FloatBuffer circleTmp = BufferUtils
			.createFloatBuffer(PARTICLE_RESOLUTION * 3 + 3);
	private static final Vector3[] CIRCLE_DATA = new Vector3[PARTICLE_RESOLUTION + 1];

	static {
		int h = 0;
		CIRCLE_DATA[h++] = new Vector3(0, 0, 0);
		CIRCLE_DATA[h - 1].write(circleTmp, 0);
		for (int i = 0; i < PARTICLE_RESOLUTION; i++) {
			float th = (float) (Math.PI * 2 * i / PARTICLE_RESOLUTION);
			CIRCLE_DATA[h++] = new Vector3(
					PARTICLE_SIZE * (float) Math.cos(th), PARTICLE_SIZE
							* (float) Math.sin(th), 0);
			CIRCLE_DATA[h - 1].write(circleTmp, i * 3 + 3);
		}
		circleTmp.flip();
	}

	// Particle states
	private static final int VERTEX_BYTE_STRIDE = 4 + (3 * 4);
	private static final int VERTEX_FLOAT_STRIDE = VERTEX_BYTE_STRIDE / 4;
	private static final int PARTICLE_BYTE_STRIDE = (PARTICLE_RESOLUTION + 1)
			* VERTEX_BYTE_STRIDE;
	private static final int PARTICLE_FLOAT_STRIDE = (PARTICLE_RESOLUTION + 1)
			* VERTEX_FLOAT_STRIDE;
	private final Vector3[] particleVel;
	private final ByteBuffer particleState;
	private final FloatBuffer particleFloatState;
	private final IntBuffer particleIndices;

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

		particleVel = new Vector3[PARTICLE_COUNT];
		particleState = BufferUtils.createByteBuffer(PARTICLE_COUNT
				* PARTICLE_BYTE_STRIDE);
		particleState.limit(particleState.capacity());
		particleFloatState = particleState.asFloatBuffer();
		particleFloatState.limit(particleFloatState.capacity());

		particleIndices = BufferUtils.createIntBuffer(PARTICLE_RESOLUTION * 3
				* PARTICLE_COUNT);
		particleIndices.limit(particleIndices.capacity());

		final byte[] blank = new byte[PARTICLE_BYTE_STRIDE];
		for (int i = 0; i < PARTICLE_COUNT; i++) {
			particleVel[i] = new Vector3(0, 0, 0);
			particleState.put(blank);
			final int baseVertex = (PARTICLE_RESOLUTION + 1) * i;
			for (int j = 0; j < PARTICLE_RESOLUTION; j++) {
				particleIndices.put(baseVertex);
				if (j == 0)
					particleIndices.put(baseVertex + PARTICLE_RESOLUTION);
				else
					particleIndices.put(baseVertex + j);
				particleIndices.put(baseVertex + j + 1);
			}
		}
		particleIndices.flip();

		update();
	}

	private float[] frontBins = new float[PARTICLE_BINS],
			backBins = new float[PARTICLE_BINS];

	public void update() {
		for (int i = 0; i < PARTICLE_BINS; i++)
			backBins[i] = 0;

		final float dt = (float) Main.getDelta();
		for (int i = 0; i < PARTICLE_COUNT; i++) {
			Vector3 posVector = new Vector3(particleFloatState,
					PARTICLE_FLOAT_STRIDE * i + 1);

			float pos2 = Vector3.mag2(posVector);
			float vel2 = Vector3.mag2(particleVel[i]);
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
				posVector = new Vector3(cos * spawnDistance, sin
						* spawnDistance, spawnRadius * 0.025f
						* ((float) Math.random() * 2 - 1));
				pos2 = spawnDistance * spawnDistance;
				float vel = (float) (Math.sqrt(spawnDistance * centralMass
						/ pos2) * (Math.random() * 0.2f + 0.9f));
				particleVel[i] = new Vector3(-sin * vel, cos * vel, 0);
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
			Vector3.addto(particleVel[i], posVector, -dt * centralMass / pos2
					/ pos);
			Vector3.addto(posVector, particleVel[i], effectiveDt);
			posVector.write(particleFloatState, PARTICLE_FLOAT_STRIDE * i + 1);

			float mix = (float) Math.pow(binValue * 1E3 / PARTICLE_COUNT, 0.5);

			final int colorOffset = PARTICLE_BYTE_STRIDE * i;
			final Vector3 color = Vector3.lincom(new Vector3(1, 0, 0), 1 - mix,
					MAX_TEMP_INTENSITY, mix);
			particleState.put(colorOffset, (byte) (color.x * 255));
			particleState.put(colorOffset + 1, (byte) (color.y * 255));
			particleState.put(colorOffset + 2, (byte) (color.z * 255));
			particleState.put(colorOffset + 3, (byte) (0.1f * 255));
		}

		float[] tmp = frontBins;
		frontBins = backBins;
		backBins = tmp;
	}

	private void propogateInformation(Matrix4 billboard) {
		for (int i = 0; i < particleVel.length; i++) {
			Vector3 posVector = new Vector3(particleFloatState,
					PARTICLE_FLOAT_STRIDE * i + 1);
			byte[] color = new byte[4];
			particleState.position(PARTICLE_BYTE_STRIDE * i);
			particleState.get(color);
			for (int j = 0; j < PARTICLE_RESOLUTION + 1; j++) {
				Vector3.add(posVector,
						Matrix4.multiply(billboard, CIRCLE_DATA[j])).write(
						particleFloatState,
						1 + (PARTICLE_FLOAT_STRIDE * i)
								+ (VERTEX_FLOAT_STRIDE * j));
				particleState.position((PARTICLE_BYTE_STRIDE * i)
						+ (VERTEX_BYTE_STRIDE * j));
				particleState.put(color);
			}
		}
	}

	private int iboHandle = -1;

	public void render() {
		if (iboHandle == -1) {
			iboHandle = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, iboHandle);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, particleIndices,
					GL15.GL_STATIC_DRAW);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		}

		GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT);
		MatrixStack.glPushMatrix();
		MatrixStack.glMultMatrix(transformation);
		Shaders.ACCRETION_DISK.use();

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		Matrix4 iRot = MatrixStack.getModelView().copy();
		iRot = Matrix4.transpose(Matrix4.mat3(iRot));
		propogateInformation(iRot);

		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY | GL11.GL_COLOR_ARRAY);
		particleState.position(0);
		GL11.glInterleavedArrays(GL11.GL_C4UB_V3F, 0, particleState);

		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, iboHandle);
		MatrixStack.commit();
		// First render with the depth mask disabled, but color mask enabled
		GL11.glDepthMask(false);
		renderParticles();
		GL11.glDepthMask(true);
		GL11.glColorMask(false, false, false, false);
		renderParticles();

		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY | GL11.GL_COLOR_ARRAY);
		GL11.glDisable(GL11.GL_BLEND);
		MatrixStack.glPopMatrix();
		GL11.glPopAttrib();
	}

	private void renderParticles() {
		// for (int i = 0; i < particleVel.length; i++) {
		// MatrixStack.glPushMatrix();
		// Vector3 pos = new Vector3(particleFloatState, PARTICLE_FLOAT_STRIDE
		// * i + 1);
		// // MatrixStack.glTranslatef(pos.x, pos.y, pos.z);
		// MatrixStack.commitModelview();
		//
		// GL11.glDrawArrays(GL11.GL_TRIANGLE_FAN, 0, PARTICLE_RESOLUTION + 1);
		// MatrixStack.glPopMatrix();
		// }
		GL11.glDrawElements(GL11.GL_TRIANGLES, particleIndices.limit(),
				GL11.GL_UNSIGNED_INT, 0);
	}
}
