package com.pi.model;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import com.pi.math.Matrix4;
import com.pi.math.Vector3;
import com.pi.util.LEInputStream;
import com.pi.util.SizeOf;

public class Mesh {
	private static final int VERTEX_STRIDE = 2 + 3 + 3 + 3 + 3;
	private static final float AREA_DENSITY = 0.1f;

	private FloatBuffer vertData;
	private IntBuffer indexData;
	private int indexCount, vertexCount;

	private int material;
	private Material materialRef;
	private Texture texTable[];

	float mass;
	Vector3 com;
	Matrix4 inertiaTensor;

	public Mesh(LEInputStream in) throws IOException {
		vertexCount = in.readIntLE();

		vertData = BufferUtils.createFloatBuffer(VERTEX_STRIDE * vertexCount);
		in.readFloatLE(vertData, 0, vertData.capacity());

		indexCount = in.readIntLE();
		indexData = BufferUtils.createIntBuffer(indexCount);
		in.readIntLE(indexData, 0, indexData.capacity());

		material = in.readIntLE();
		materialRef = null;
		computePhysics();
	}

	private int indexBuffer = -1, vertexBuffer = -1;

	private void genBuffers() {
		indexBuffer = GL15.glGenBuffers();
		vertexBuffer = GL15.glGenBuffers();

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBuffer);
		vertData.position(0);
		vertData.limit(vertexCount * VERTEX_STRIDE);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertData, GL15.GL_STATIC_DRAW);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, indexBuffer);
		indexData.position(0);
		indexData.limit(indexCount);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, indexData, GL15.GL_STATIC_DRAW);
	}

	public void render() {
		if (indexBuffer < 0)
			genBuffers();

		if (materialRef != null) {
			materialRef.glBindMaterial();
			if (materialRef.diffuse.imageID > 0) {
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL13.glActiveTexture(GL13.GL_TEXTURE0);
				texTable[materialRef.diffuse.imageID - 1].bind();
			}
			if (materialRef.specular.imageID > 0) {
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL13.glActiveTexture(GL13.GL_TEXTURE1);
				texTable[materialRef.specular.imageID - 1].bind();
			}
			if (materialRef.reflective.imageID > 0) {
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL13.glActiveTexture(GL13.GL_TEXTURE2);
				texTable[materialRef.reflective.imageID - 1].bind();
			}
		}
		GL11.glEnable(GL11.GL_VERTEX_ARRAY | GL11.GL_NORMAL_ARRAY
				| GL11.GL_TEXTURE_COORD_ARRAY
				| GL20.GL_VERTEX_ATTRIB_ARRAY_POINTER);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBuffer);
		GL11.glInterleavedArrays(GL11.GL_T2F_N3F_V3F, SizeOf.FLOAT
				* VERTEX_STRIDE, 0);
		GL20.glVertexAttribPointer(3, 3, GL11.GL_FLOAT, true, SizeOf.FLOAT
				* VERTEX_STRIDE, 8 * SizeOf.FLOAT);
		GL20.glVertexAttribPointer(4, 3, GL11.GL_FLOAT, true, SizeOf.FLOAT
				* VERTEX_STRIDE, 11 * SizeOf.FLOAT);

		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer);

		GL11.glDrawElements(GL11.GL_TRIANGLES, indexCount,
				GL11.GL_UNSIGNED_INT, 0);
		GL11.glDisable(GL11.GL_VERTEX_ARRAY | GL11.GL_NORMAL_ARRAY
				| GL11.GL_TEXTURE_COORD_ARRAY
				| GL20.GL_VERTEX_ATTRIB_ARRAY_POINTER);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}

	public void updateMaterialRef(Material[] matTable, Texture[] texTable) {
		this.texTable = texTable;
		if (material > 0)
			materialRef = matTable[material - 1];
		else
			materialRef = null;
	}

	void computePhysics() {
		mass = 0;
		com = new Vector3();
		for (int t = 0; t < indexCount; t += 3) {
			Vector3 a = new Vector3(vertData, indexData.get(t) * VERTEX_STRIDE
					+ 5);
			Vector3 b = new Vector3(vertData, indexData.get(t + 1)
					* VERTEX_STRIDE + 5);
			Vector3 c = new Vector3(vertData, indexData.get(t + 2)
					* VERTEX_STRIDE + 5);

			final float partialMass = Vector3.tris_area(a, b, c) * AREA_DENSITY;
			final Vector3 partialCentroid = Vector3.lincom(a, 1.0f / 3.0f, b,
					1.0f / 3.0f, c, 1.0f / 3.0f);

			mass += partialMass;
			Vector3.addto(com, partialCentroid, partialMass);
		}
		com = Vector3.multiply(com, 1.0f / mass);

		// Compute the inertia tensor
		inertiaTensor = Matrix4.identity();
		inertiaTensor.data.put(0, 0);
		inertiaTensor.data.put(5, 0);
		inertiaTensor.data.put(10, 0);

		for (int t = 0; t < indexCount; t += 3) {
			Vector3 a = new Vector3(vertData, indexData.get(t) * VERTEX_STRIDE
					+ 5);
			Vector3 b = new Vector3(vertData, indexData.get(t + 1)
					* VERTEX_STRIDE + 5);
			Vector3 c = new Vector3(vertData, indexData.get(t + 2)
					* VERTEX_STRIDE + 5);

			final float partialMass = Vector3.tris_area(a, b, c) * AREA_DENSITY;
			final Vector3 comOff = Vector3.lincom(Vector3.lincom(a,
					1.0f / 3.0f, b, 1.0f / 3.0f, c, 1.0f / 3.0f), 1, com, -1);

			Matrix4.add_inertia_tensor(inertiaTensor, partialMass, comOff);
		}
	}
}
