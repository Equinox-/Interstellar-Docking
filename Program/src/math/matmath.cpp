#include "matmath.h"
#include <stdio.h>
#include "vecmath.h"
#include <math.h>
#include <string.h>
#include <stdlib.h>

mat4 mat4_identity() {
	mat4 res;
	for (uint8_t i = 0; i < 16; i++)
		res.data[i] = 0;

	res.data[0] = res.data[5] = res.data[10] = res.data[15] = 1.0f;
	return res;
}

mat4 mat4_multiply(const mat4 &a, const mat4 &b) {
	mat4 res;
	for (uint8_t i = 0; i < 16; i++)
		res.data[i] = 0;
	for (int k = 0; k <= 12; k += 4) {
		for (int i = 0; i < 4; i++) {
			for (int j = 0, bCount = 0; j < 4; j++, bCount += 4) {
				res.data[k + i] += a.data[k + j % 4] * b.data[bCount + i % 4];
			}
		}
	}
	return res;
}

vec3 mat4_multiply(const mat4 &a, const vec3 &v) {
	vec3 res;
	res.x = a.data[0] * v.x + a.data[4] * v.y + a.data[8] * v.z + a.data[12];
	res.y = a.data[1] * v.x + a.data[5] * v.y + a.data[9] * v.z + a.data[13];
	res.z = a.data[2] * v.x + a.data[6] * v.y + a.data[10] * v.z + a.data[14];
	return res;
}

// Assumes axis is unit
mat4 mat4_axis_angle(const float &angle, const vec3 &a) {
	mat4 res;
	const float c = (float) cos(angle);
	const float s = (float) sin(angle);
	const float c1 = 1 - c;

	res.data[0] = c + a.x * a.x * c1;
	res.data[1] = a.y * a.x * c1 + a.z * s;
	res.data[2] = a.z * a.x * c1 - a.y * s;
	res.data[3] = 0;

	res.data[4] = a.x * a.y * c1 - a.z * s;
	res.data[5] = c + a.y * a.y * c1;
	res.data[6] = a.z * a.y * c1 + a.x * s;
	res.data[7] = 0;

	res.data[8] = a.x * a.z * c1 + a.y * s;
	res.data[9] = a.y * a.z * c1 - a.x * s;
	res.data[10] = c + a.z * a.z * c1;
	res.data[11] = 0;

	res.data[12] = res.data[13] = res.data[14] = 0;
	res.data[15] = 1;
	return res;
}

// Assumes axis is unit
mat4 mat4_translation(const vec3 &a) {
	mat4 res = mat4_identity();
	res.data[12] = a.x;
	res.data[13] = a.y;
	res.data[14] = a.z;
	return res;
}

mat4 mat4_invert(const mat4 &m) {
	mat4 res;
	res.data[0] = m.data[5] * m.data[10] * m.data[15]
			- m.data[5] * m.data[11] * m.data[14]
			- m.data[9] * m.data[6] * m.data[15]
			+ m.data[9] * m.data[7] * m.data[14]
			+ m.data[13] * m.data[6] * m.data[11]
			- m.data[13] * m.data[7] * m.data[10];

	res.data[4] = -m.data[4] * m.data[10] * m.data[15]
			+ m.data[4] * m.data[11] * m.data[14]
			+ m.data[8] * m.data[6] * m.data[15]
			- m.data[8] * m.data[7] * m.data[14]
			- m.data[12] * m.data[6] * m.data[11]
			+ m.data[12] * m.data[7] * m.data[10];

	res.data[8] = m.data[4] * m.data[9] * m.data[15]
			- m.data[4] * m.data[11] * m.data[13]
			- m.data[8] * m.data[5] * m.data[15]
			+ m.data[8] * m.data[7] * m.data[13]
			+ m.data[12] * m.data[5] * m.data[11]
			- m.data[12] * m.data[7] * m.data[9];

	res.data[12] = -m.data[4] * m.data[9] * m.data[14]
			+ m.data[4] * m.data[10] * m.data[13]
			+ m.data[8] * m.data[5] * m.data[14]
			- m.data[8] * m.data[6] * m.data[13]
			- m.data[12] * m.data[5] * m.data[10]
			+ m.data[12] * m.data[6] * m.data[9];

	res.data[1] = -m.data[1] * m.data[10] * m.data[15]
			+ m.data[1] * m.data[11] * m.data[14]
			+ m.data[9] * m.data[2] * m.data[15]
			- m.data[9] * m.data[3] * m.data[14]
			- m.data[13] * m.data[2] * m.data[11]
			+ m.data[13] * m.data[3] * m.data[10];

	res.data[5] = m.data[0] * m.data[10] * m.data[15]
			- m.data[0] * m.data[11] * m.data[14]
			- m.data[8] * m.data[2] * m.data[15]
			+ m.data[8] * m.data[3] * m.data[14]
			+ m.data[12] * m.data[2] * m.data[11]
			- m.data[12] * m.data[3] * m.data[10];

	res.data[9] = -m.data[0] * m.data[9] * m.data[15]
			+ m.data[0] * m.data[11] * m.data[13]
			+ m.data[8] * m.data[1] * m.data[15]
			- m.data[8] * m.data[3] * m.data[13]
			- m.data[12] * m.data[1] * m.data[11]
			+ m.data[12] * m.data[3] * m.data[9];

	res.data[13] = m.data[0] * m.data[9] * m.data[14]
			- m.data[0] * m.data[10] * m.data[13]
			- m.data[8] * m.data[1] * m.data[14]
			+ m.data[8] * m.data[2] * m.data[13]
			+ m.data[12] * m.data[1] * m.data[10]
			- m.data[12] * m.data[2] * m.data[9];

	res.data[2] = m.data[1] * m.data[6] * m.data[15]
			- m.data[1] * m.data[7] * m.data[14]
			- m.data[5] * m.data[2] * m.data[15]
			+ m.data[5] * m.data[3] * m.data[14]
			+ m.data[13] * m.data[2] * m.data[7]
			- m.data[13] * m.data[3] * m.data[6];

	res.data[6] = -m.data[0] * m.data[6] * m.data[15]
			+ m.data[0] * m.data[7] * m.data[14]
			+ m.data[4] * m.data[2] * m.data[15]
			- m.data[4] * m.data[3] * m.data[14]
			- m.data[12] * m.data[2] * m.data[7]
			+ m.data[12] * m.data[3] * m.data[6];

	res.data[10] = m.data[0] * m.data[5] * m.data[15]
			- m.data[0] * m.data[7] * m.data[13]
			- m.data[4] * m.data[1] * m.data[15]
			+ m.data[4] * m.data[3] * m.data[13]
			+ m.data[12] * m.data[1] * m.data[7]
			- m.data[12] * m.data[3] * m.data[5];

	res.data[14] = -m.data[0] * m.data[5] * m.data[14]
			+ m.data[0] * m.data[6] * m.data[13]
			+ m.data[4] * m.data[1] * m.data[14]
			- m.data[4] * m.data[2] * m.data[13]
			- m.data[12] * m.data[1] * m.data[6]
			+ m.data[12] * m.data[2] * m.data[5];

	res.data[3] = -m.data[1] * m.data[6] * m.data[11]
			+ m.data[1] * m.data[7] * m.data[10]
			+ m.data[5] * m.data[2] * m.data[11]
			- m.data[5] * m.data[3] * m.data[10]
			- m.data[9] * m.data[2] * m.data[7]
			+ m.data[9] * m.data[3] * m.data[6];

	res.data[7] = m.data[0] * m.data[6] * m.data[11]
			- m.data[0] * m.data[7] * m.data[10]
			- m.data[4] * m.data[2] * m.data[11]
			+ m.data[4] * m.data[3] * m.data[10]
			+ m.data[8] * m.data[2] * m.data[7]
			- m.data[8] * m.data[3] * m.data[6];

	res.data[11] = -m.data[0] * m.data[5] * m.data[11]
			+ m.data[0] * m.data[7] * m.data[9]
			+ m.data[4] * m.data[1] * m.data[11]
			- m.data[4] * m.data[3] * m.data[9]
			- m.data[8] * m.data[1] * m.data[7]
			+ m.data[8] * m.data[3] * m.data[5];

	res.data[15] = m.data[0] * m.data[5] * m.data[10]
			- m.data[0] * m.data[6] * m.data[9]
			- m.data[4] * m.data[1] * m.data[10]
			+ m.data[4] * m.data[2] * m.data[9]
			+ m.data[8] * m.data[1] * m.data[6]
			- m.data[8] * m.data[2] * m.data[5];

	float det = m.data[0] * res.data[0] + m.data[1] * res.data[4]
			+ m.data[2] * res.data[8] + m.data[3] * res.data[12];

	if (det == 0) {
		printf("Invert det=0 mat\n");
		exit(1);
	}
	det = 1.0 / det;

	for (int i = 0; i < 16; i++)
		res.data[i] *= det;
	return res;
}

void mat4_print(const mat4 &mat) {
	for (int i = 0; i < 4; i++) {
		printf("%0.10f %0.10f %0.10f %0.10f\n", mat.data[i], mat.data[i + 4],
				mat.data[i + 8], mat.data[i + 12]);
	}
}

void mat4_add_inertia_tensor(mat4 &mat, const float mass, const vec3 &com) {
	mat.data[0] += mass * (com.y * com.y + com.z * com.z);
	mat.data[5] += mass * (com.x * com.x + com.z * com.z);
	mat.data[10] += mass * (com.x * com.x + com.y * com.y);
	mat.data[4] += -mass * com.x * com.y;
	mat.data[8] += -mass * com.x * com.z;
	mat.data[9] += -mass * com.y * com.z;

	// Keep it symmetric
	mat.data[1] = mat.data[4];
	mat.data[2] = mat.data[8];
	mat.data[6] = mat.data[9];
}

mat4 mat4_transpose(const mat4 &mat) {
	mat4 res = mat;
	res.data[1] = mat.data[4];
	res.data[2] = mat.data[8];
	res.data[3] = mat.data[12];

	res.data[6] = mat.data[9];
	res.data[7] = mat.data[13];

	res.data[11] = mat.data[14];

	res.data[4] = mat.data[1];
	res.data[8] = mat.data[2];
	res.data[12] = mat.data[3];

	res.data[9] = mat.data[6];
	res.data[13] = mat.data[7];

	res.data[14] = mat.data[11];
	return res;
}

mat4 mat4_skewsym(const vec3 &v) {
	mat4 res;
	for (uint8_t i = 0; i < 16; i++)
		res.data[i] = 0;
	res.data[15] = 1;
	res.data[1] = v.z;
	res.data[2] = -v.y;
	res.data[4] = -v.z;
	res.data[6] = v.x;
	res.data[8] = v.y;
	res.data[9] = -v.x;
	return res;
}

mat4 mat4_from_quat(const quat &q) {
	mat4 mat = mat4_identity();
	mat.data[0] = 1 - 2 * q.y * q.y - 2 * q.z * q.z;
	mat.data[4] = 2 * q.x * q.y - 2 * q.z * q.w;
	mat.data[8] = 2 * q.x * q.z + 2 * q.y * q.w;
	mat.data[1] = 2 * q.x * q.y + 2 * q.z * q.w;
	mat.data[5] = 1 - 2 * q.x * q.x - 2 * q.z * q.z;
	mat.data[9] = 2 * q.y * q.z - 2 * q.x * q.w;
	mat.data[2] = 2 * q.x * q.z - 2 * q.y * q.w;
	mat.data[6] = 2 * q.y * q.z + 2 * q.x * q.w;
	mat.data[10] = 1 - 2 * q.x * q.x - 2 * q.y * q.y;
	return mat;
}
