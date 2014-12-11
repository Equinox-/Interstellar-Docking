package com.pi.gl;

import org.lwjgl.opengl.GL20;

import com.pi.math.Matrix4;
import com.pi.math.Vector3;

public class MatrixStack {
	public static Matrix4 projection = Matrix4.identity();

	private static Matrix4 tmpScale = Matrix4.identity(),
			tmpRotation = Matrix4.identity();

	private static int modelViewStackHead = 0;
	private static Matrix4[] modelView = new Matrix4[10];
	static {
		modelView[modelViewStackHead] = Matrix4.identity();
	}

	public static void glLoadIdentity() {
		modelView[modelViewStackHead].makeIdentity();
	}

	public static void glMultMatrix(Matrix4 m) {
		modelView[modelViewStackHead] = Matrix4.multiply(m,
				modelView[modelViewStackHead]);
	}

	public static void glPushMatrix() {
		modelViewStackHead++;
		modelView[modelViewStackHead] = modelView[modelViewStackHead - 1]
				.copy();
	}

	public static void glPopMatrix() {
		modelViewStackHead--;
	}

	public static void glTranslatef(float x, float y, float z) {
		Matrix4 m = modelView[modelViewStackHead];
		m.data.put(12, m.data.get(0) * x + m.data.get(4) * y + m.data.get(8)
				* z + m.data.get(12));
		m.data.put(13, m.data.get(1) * x + m.data.get(5) * y + m.data.get(9)
				* z + m.data.get(13));
		m.data.put(14, m.data.get(2) * x + m.data.get(6) * y + m.data.get(10)
				* z + m.data.get(14));
		m.data.put(15, m.data.get(3) * x + m.data.get(7) * y + m.data.get(11)
				* z + m.data.get(15));
	}
	
	public static void glScalef(float x, float y, float z) {
		tmpScale.data.put(0,x);
		tmpScale.data.put(5,y);
		tmpScale.data.put(10,z);
		glMultMatrix(tmpScale);
	}

	public static void glRotatef(float angle, float x, float y, float z) {
		glMultMatrix(tmpRotation.setAxisAngle(angle * (float) Math.PI / 180.0f,
				new Vector3(x, y, z)));
	}

	public static void commitModelview() {
		GL20.glUniformMatrix4(Shaders.current().getModelviewAddr(), false,
				modelView[modelViewStackHead].data);
		if (Shaders.current().getNormalAddr() >= 0)
			GL20.glUniformMatrix4(Shaders.current().getNormalAddr(), true,
					Matrix4.invert(modelView[modelViewStackHead]).data);
	}

	public static Matrix4 getModelView() {
		return modelView[modelViewStackHead].copy();
	}

	// Projection operations
	public static void glFrustum(float left, float right, float bottom,
			float top, float near, float far) {
		final float near2 = 2 * near;
		final float width = right - left, height = top - bottom, length = far
				- near;

		projection.zero();
		projection.data.put(0, near2 / width);
		projection.data.put(5, near2 / height);
		projection.data.put(8, (right + left) / width);
		projection.data.put(9, (top + bottom) / height);
		projection.data.put(10, -(far + near) / length);
		projection.data.put(11, -1);
		projection.data.put(14, -near2 * far / length);
	}

	public static void glOrtho(float left, float right, float bottom,
			float top, float near, float far) {
		final float width = right - left, height = top - bottom, length = far
				- near;

		projection.zero();
		projection.data.put(0, 2 / width);
		projection.data.put(5, 2 / height);
		projection.data.put(10, -2 / length);
		projection.data.put(12, -(right + left) / width);
		projection.data.put(13, -(top + bottom) / height);
		projection.data.put(14, -(far + near) / length);
		projection.data.put(15, 1);
	}

	public static void commitProjection() {
		GL20.glUniformMatrix4(Shaders.current().getProjectionAddr(), false,
				projection.data);
	}

	public static Matrix4 getProjection() {
		return projection.copy();
	}

	// Conjoined
	public static void commit() {
		commitProjection();
		commitModelview();
	}
}
