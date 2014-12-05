package com.pi.math;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

public class Matrix4 {
	public final FloatBuffer data;

	public Matrix4() {
		data = BufferUtils.createFloatBuffer(16);
	}

	public static Matrix4 identity() {
		Matrix4 res = new Matrix4();
		for (int i = 0; i < 16; i++)
			res.data.put(i, 0);

		res.data.put(0, 1);
		res.data.put(5, 1);
		res.data.put(10, 1);
		res.data.put(15, 1);
		return res;
	}

	public static Matrix4 multiply(final Matrix4 a, final Matrix4 b) {
		Matrix4 res = new Matrix4();
		for (int i = 0; i < 16; i++)
			res.data.put(i, 0);
		for (int k = 0; k <= 12; k += 4) {
			for (int i = 0; i < 4; i++) {
				for (int j = 0, bCount = 0; j < 4; j++, bCount += 4) {
					res.data.put(k + i,
							res.data.get(k + i) + a.data.get(k + j % 4)
									* b.data.get(bCount + i % 4));
				}
			}
		}
		return res;
	}

	public static Vector3 multiply(final Matrix4 a, final Vector3 v) {
		Vector3 res = new Vector3();
		res.x = a.data.get(0) * v.x + a.data.get(4) * v.y + a.data.get(8) * v.z
				+ a.data.get(12);
		res.y = a.data.get(1) * v.x + a.data.get(5) * v.y + a.data.get(9) * v.z
				+ a.data.get(13);
		res.z = a.data.get(2) * v.x + a.data.get(6) * v.y + a.data.get(10)
				* v.z + a.data.get(14);
		return res;
	}

	// Assumes axis is unit
	public static Matrix4 axis_angle(final float angle, final Vector3 a) {
		Matrix4 res = new Matrix4();
		final float c = (float) Math.cos(angle);
		final float s = (float) Math.sin(angle);
		final float c1 = 1 - c;

		res.data.put(0, c + a.x * a.x * c1);
		res.data.put(1, a.y * a.x * c1 + a.z * s);
		res.data.put(2, a.z * a.x * c1 - a.y * s);
		res.data.put(3, 0);

		res.data.put(4, a.x * a.y * c1 - a.z * s);
		res.data.put(5, c + a.y * a.y * c1);
		res.data.put(6, a.z * a.y * c1 + a.x * s);
		res.data.put(7, 0);

		res.data.put(8, a.x * a.z * c1 + a.y * s);
		res.data.put(9, a.y * a.z * c1 - a.x * s);
		res.data.put(10, c + a.z * a.z * c1);
		res.data.put(11, 0);

		res.data.put(12, 0);
		res.data.put(13, 0);
		res.data.put(14, 0);
		res.data.put(15, 1);
		return res;
	}

	// Assumes axis is unit
	public static Matrix4 translation(final Vector3 a) {
		Matrix4 res = identity();
		res.data.put(12, a.x);
		res.data.put(13, a.y);
		res.data.put(14, a.z);
		return res;
	}

	public static Matrix4 invert(final Matrix4 m) {
		Matrix4 res = new Matrix4();
		res.data.put(0,
				m.data.get(5) * m.data.get(10) * m.data.get(15) - m.data.get(5)
						* m.data.get(11) * m.data.get(14) - m.data.get(9)
						* m.data.get(6) * m.data.get(15) + m.data.get(9)
						* m.data.get(7) * m.data.get(14) + m.data.get(13)
						* m.data.get(6) * m.data.get(11) - m.data.get(13)
						* m.data.get(7) * m.data.get(10));

		res.data.put(
				4,
				-m.data.get(4) * m.data.get(10) * m.data.get(15)
						+ m.data.get(4) * m.data.get(11) * m.data.get(14)
						+ m.data.get(8) * m.data.get(6) * m.data.get(15)
						- m.data.get(8) * m.data.get(7) * m.data.get(14)
						- m.data.get(12) * m.data.get(6) * m.data.get(11)
						+ m.data.get(12) * m.data.get(7) * m.data.get(10));

		res.data.put(8,
				m.data.get(4) * m.data.get(9) * m.data.get(15) - m.data.get(4)
						* m.data.get(11) * m.data.get(13) - m.data.get(8)
						* m.data.get(5) * m.data.get(15) + m.data.get(8)
						* m.data.get(7) * m.data.get(13) + m.data.get(12)
						* m.data.get(5) * m.data.get(11) - m.data.get(12)
						* m.data.get(7) * m.data.get(9));

		res.data.put(12,
				-m.data.get(4) * m.data.get(9) * m.data.get(14) + m.data.get(4)
						* m.data.get(10) * m.data.get(13) + m.data.get(8)
						* m.data.get(5) * m.data.get(14) - m.data.get(8)
						* m.data.get(6) * m.data.get(13) - m.data.get(12)
						* m.data.get(5) * m.data.get(10) + m.data.get(12)
						* m.data.get(6) * m.data.get(9));

		res.data.put(
				1,
				-m.data.get(1) * m.data.get(10) * m.data.get(15)
						+ m.data.get(1) * m.data.get(11) * m.data.get(14)
						+ m.data.get(9) * m.data.get(2) * m.data.get(15)
						- m.data.get(9) * m.data.get(3) * m.data.get(14)
						- m.data.get(13) * m.data.get(2) * m.data.get(11)
						+ m.data.get(13) * m.data.get(3) * m.data.get(10));

		res.data.put(5,
				m.data.get(0) * m.data.get(10) * m.data.get(15) - m.data.get(0)
						* m.data.get(11) * m.data.get(14) - m.data.get(8)
						* m.data.get(2) * m.data.get(15) + m.data.get(8)
						* m.data.get(3) * m.data.get(14) + m.data.get(12)
						* m.data.get(2) * m.data.get(11) - m.data.get(12)
						* m.data.get(3) * m.data.get(10));

		res.data.put(9,
				-m.data.get(0) * m.data.get(9) * m.data.get(15) + m.data.get(0)
						* m.data.get(11) * m.data.get(13) + m.data.get(8)
						* m.data.get(1) * m.data.get(15) - m.data.get(8)
						* m.data.get(3) * m.data.get(13) - m.data.get(12)
						* m.data.get(1) * m.data.get(11) + m.data.get(12)
						* m.data.get(3) * m.data.get(9));

		res.data.put(13,
				m.data.get(0) * m.data.get(9) * m.data.get(14) - m.data.get(0)
						* m.data.get(10) * m.data.get(13) - m.data.get(8)
						* m.data.get(1) * m.data.get(14) + m.data.get(8)
						* m.data.get(2) * m.data.get(13) + m.data.get(12)
						* m.data.get(1) * m.data.get(10) - m.data.get(12)
						* m.data.get(2) * m.data.get(9));

		res.data.put(2,
				m.data.get(1) * m.data.get(6) * m.data.get(15) - m.data.get(1)
						* m.data.get(7) * m.data.get(14) - m.data.get(5)
						* m.data.get(2) * m.data.get(15) + m.data.get(5)
						* m.data.get(3) * m.data.get(14) + m.data.get(13)
						* m.data.get(2) * m.data.get(7) - m.data.get(13)
						* m.data.get(3) * m.data.get(6));

		res.data.put(6,
				-m.data.get(0) * m.data.get(6) * m.data.get(15) + m.data.get(0)
						* m.data.get(7) * m.data.get(14) + m.data.get(4)
						* m.data.get(2) * m.data.get(15) - m.data.get(4)
						* m.data.get(3) * m.data.get(14) - m.data.get(12)
						* m.data.get(2) * m.data.get(7) + m.data.get(12)
						* m.data.get(3) * m.data.get(6));

		res.data.put(10,
				m.data.get(0) * m.data.get(5) * m.data.get(15) - m.data.get(0)
						* m.data.get(7) * m.data.get(13) - m.data.get(4)
						* m.data.get(1) * m.data.get(15) + m.data.get(4)
						* m.data.get(3) * m.data.get(13) + m.data.get(12)
						* m.data.get(1) * m.data.get(7) - m.data.get(12)
						* m.data.get(3) * m.data.get(5));

		res.data.put(14,
				-m.data.get(0) * m.data.get(5) * m.data.get(14) + m.data.get(0)
						* m.data.get(6) * m.data.get(13) + m.data.get(4)
						* m.data.get(1) * m.data.get(14) - m.data.get(4)
						* m.data.get(2) * m.data.get(13) - m.data.get(12)
						* m.data.get(1) * m.data.get(6) + m.data.get(12)
						* m.data.get(2) * m.data.get(5));

		res.data.put(3,
				-m.data.get(1) * m.data.get(6) * m.data.get(11) + m.data.get(1)
						* m.data.get(7) * m.data.get(10) + m.data.get(5)
						* m.data.get(2) * m.data.get(11) - m.data.get(5)
						* m.data.get(3) * m.data.get(10) - m.data.get(9)
						* m.data.get(2) * m.data.get(7) + m.data.get(9)
						* m.data.get(3) * m.data.get(6));

		res.data.put(7,
				m.data.get(0) * m.data.get(6) * m.data.get(11) - m.data.get(0)
						* m.data.get(7) * m.data.get(10) - m.data.get(4)
						* m.data.get(2) * m.data.get(11) + m.data.get(4)
						* m.data.get(3) * m.data.get(10) + m.data.get(8)
						* m.data.get(2) * m.data.get(7) - m.data.get(8)
						* m.data.get(3) * m.data.get(6));

		res.data.put(11,
				-m.data.get(0) * m.data.get(5) * m.data.get(11) + m.data.get(0)
						* m.data.get(7) * m.data.get(9) + m.data.get(4)
						* m.data.get(1) * m.data.get(11) - m.data.get(4)
						* m.data.get(3) * m.data.get(9) - m.data.get(8)
						* m.data.get(1) * m.data.get(7) + m.data.get(8)
						* m.data.get(3) * m.data.get(5));

		res.data.put(15,
				m.data.get(0) * m.data.get(5) * m.data.get(10) - m.data.get(0)
						* m.data.get(6) * m.data.get(9) - m.data.get(4)
						* m.data.get(1) * m.data.get(10) + m.data.get(4)
						* m.data.get(2) * m.data.get(9) + m.data.get(8)
						* m.data.get(1) * m.data.get(6) - m.data.get(8)
						* m.data.get(2) * m.data.get(5));

		float det = m.data.get(0) * res.data.get(0) + m.data.get(1)
				* res.data.get(4) + m.data.get(2) * res.data.get(8)
				+ m.data.get(3) * res.data.get(12);

		if (det == 0) {
			System.out.printf("Invert det=0 mat\n");
			System.exit(1);
		}
		det = 1.0f / det;

		for (int i = 0; i < 16; i++)
			res.data.put(i, res.data.get(i) * (det));
		return res;
	}

	public static void print(final Matrix4 mat) {
		for (int i = 0; i < 4; i++) {
			System.out.printf("%0.10f %0.10f %0.10f %0.10f\n", mat.data.get(i),
					mat.data.get(i + 4), mat.data.get(i + 8),
					mat.data.get(i + 12));
		}
	}

	public static void add_inertia_tensor(Matrix4 mat, final float mass,
			final Vector3 com) {
		mat.data.put(0, mat.data.get(0)
				+ (mass * (com.y * com.y + com.z * com.z)));
		mat.data.put(5, mat.data.get(5)
				+ (mass * (com.x * com.x + com.z * com.z)));
		mat.data.put(10, mat.data.get(10)
				+ (mass * (com.x * com.x + com.y * com.y)));
		mat.data.put(4, mat.data.get(4) + (-mass * com.x * com.y));
		mat.data.put(8, mat.data.get(8) + (-mass * com.x * com.z));
		mat.data.put(9, mat.data.get(9) + (-mass * com.y * com.z));

		// Keep it symmetric
		mat.data.put(1, mat.data.get(4));
		mat.data.put(2, mat.data.get(8));
		mat.data.put(6, mat.data.get(9));
	}

	public static Matrix4 transpose(final Matrix4 mat) {
		Matrix4 res = mat.copy();
		res.data.put(1, mat.data.get(4));
		res.data.put(2, mat.data.get(8));
		res.data.put(3, mat.data.get(12));

		res.data.put(6, mat.data.get(9));
		res.data.put(7, mat.data.get(13));

		res.data.put(11, mat.data.get(14));

		res.data.put(4, mat.data.get(1));
		res.data.put(8, mat.data.get(2));
		res.data.put(12, mat.data.get(3));

		res.data.put(9, mat.data.get(6));
		res.data.put(13, mat.data.get(7));

		res.data.put(14, mat.data.get(11));
		return res;
	}

	public static Matrix4 skewsym(final Vector3 v) {
		Matrix4 res = new Matrix4();
		for (int i = 0; i < 16; i++)
			res.data.put(i, 0);
		res.data.put(15, 1);
		res.data.put(1, v.z);
		res.data.put(2, -v.y);
		res.data.put(4, -v.z);
		res.data.put(6, v.x);
		res.data.put(8, v.y);
		res.data.put(9, -v.x);
		return res;
	}

	public static Matrix4 from_quat(final Quaternion q) {
		Matrix4 mat = identity();
		mat.data.put(0, 1 - 2 * q.v.y * q.v.y - 2 * q.v.z * q.v.z);
		mat.data.put(4, 2 * q.v.x * q.v.y - 2 * q.v.z * q.w);
		mat.data.put(8, 2 * q.v.x * q.v.z + 2 * q.v.y * q.w);
		mat.data.put(1, 2 * q.v.x * q.v.y + 2 * q.v.z * q.w);
		mat.data.put(5, 1 - 2 * q.v.x * q.v.x - 2 * q.v.z * q.v.z);
		mat.data.put(9, 2 * q.v.y * q.v.z - 2 * q.v.x * q.w);
		mat.data.put(2, 2 * q.v.x * q.v.z - 2 * q.v.y * q.w);
		mat.data.put(6, 2 * q.v.y * q.v.z + 2 * q.v.x * q.w);
		mat.data.put(10, 1 - 2 * q.v.x * q.v.x - 2 * q.v.y * q.v.y);
		return mat;
	}

	public Matrix4 copy() {
		Matrix4 res = new Matrix4();
		for (int i = 0; i < 16; i++)
			res.data.put(i, data.get(i));
		return res;
	}

	public static Matrix4 mat3(Matrix4 mat) {
		Matrix4 res = mat.copy();
		res.data.put(3, 0);
		res.data.put(7, 0);
		res.data.put(11, 0);
		res.data.put(12, 0);
		res.data.put(13, 0);
		res.data.put(14, 0);
		res.data.put(15, 1);
		return res;
	}

	public static void addto(Matrix4 mat, Matrix4 from) {
		for (int i = 0; i < 16; i++)
			mat.data.put(i, mat.data.get(i) + from.data.get(i));
	}

	public static void addto(Matrix4 mat, Matrix4 from, float f) {
		for (int i = 0; i < 16; i++)
			mat.data.put(i, mat.data.get(i) + from.data.get(i) * f);
	}

	public static Matrix4 inertia_tensor_multiply(Matrix4 tensor, Matrix4 rot) {
		return multiply(multiply(rot, tensor), transpose(rot));
	}
}
