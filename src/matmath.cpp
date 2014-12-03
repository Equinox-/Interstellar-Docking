#include "matmath.h"
#include <stdio.h>
#include "vecmath.h"
#include <math.h>
#include <string.h>

mat4 mat4_identity() {
	mat4 res;
	for (int i = 0; i < 16; i++)
		res.data[i] = 0.0f;

	res.data[0] = 1.0f;
	res.data[5] = 1.0f;
	res.data[10] = 1.0f;
	res.data[15] = 1.0f;
	return res;
}

mat4 mat4_multiply(const mat4 &a, const mat4 &b) {
	mat4 res;
	memset(res.data, 0, sizeof(float) * 16);
	for (int k = 0; k <= 12; k += 4) {
		for (int i = 0; i < 4; i++) {
			for (int j = 0, bCount = 0; j < 4; j++, bCount += 4) {
				res.data[k + i] += a.data[k + j % 4] * b.data[bCount + i % 4];
			}
		}
	}
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

void mat4_print(const mat4 &mat) {
	for (int i = 0; i < 4; i++) {
		printf("%0.5f %0.5f %0.5f %0.5f\n", mat.data[i], mat.data[i + 4],
				mat.data[i + 8], mat.data[i + 12]);
	}
}
