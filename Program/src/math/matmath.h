#ifndef MATMATH_H_
#define MATMATH_H_

typedef union {
	float data[16];
} mat4;

#include "vecmath.h"
#include "quatmath.h"


mat4 mat4_identity();

mat4 mat4_multiply(const mat4 &a, const mat4 &b);

vec3 mat4_multiply(const mat4 &a, const vec3 &b);

// Assumes axis is unit
mat4 mat4_axis_angle(const float &angle, const vec3 &a);

mat4 mat4_translation(const vec3 &trans);

mat4 mat4_invert(const mat4 &mat);

void mat4_print(const mat4 &mat);

inline mat4 mat4_mat3(const mat4 &mat) {
	mat4 res = mat;
	res.data[3] = res.data[7] = res.data[11] = res.data[12] = res.data[13] =
			res.data[14] = 0;
	res.data[15] = 1;
	return res;
}

inline void mat4_addto(mat4 &mat, const mat4 &from) {
	for (uint32_t i = 0; i < 16; i++)
		mat.data[i] += from.data[i];
}

inline void mat4_addto(mat4 &mat, const mat4 &from, const float f) {
	for (uint32_t i = 0; i < 16; i++)
		mat.data[i] += from.data[i] * f;
}

mat4 mat4_transpose(const mat4 &mat);

void mat4_add_inertia_tensor(mat4 &mat, const float mass,
		const vec3 &posRelCOM);

inline mat4 mat4_inertia_tensor_multiply(const mat4 &tensor, const mat4 &rot) {
	return mat4_multiply(mat4_multiply(rot, tensor), mat4_transpose(rot));
}

mat4 mat4_skewsym(const vec3 &vec);

mat4 mat4_from_quat(const quat &quat);

#endif
