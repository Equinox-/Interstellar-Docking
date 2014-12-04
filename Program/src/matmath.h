#ifndef MATMATH_H_
#define MATMATH_H_

typedef union {
	float data[16];
} mat4;

#include "vecmath.h"

mat4 mat4_identity();

mat4 mat4_multiply(const mat4 &a, const mat4 &b);

vec3 mat4_multiply(const mat4 &a, const vec3 &b);

// Assumes axis is unit
mat4 mat4_axis_angle(const float &angle, const vec3 &a);

mat4 mat4_translation(const vec3 &trans);

mat4 mat4_invert(const mat4 &mat);

void mat4_print(const mat4 &mat);


#endif
