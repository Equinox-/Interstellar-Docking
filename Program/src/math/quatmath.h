#ifndef __QUATMATH_H
#define __QUATMATH_H

#include "vecmath.h"
#include <math.h>

typedef union {
	struct {
		float w;
		union {
			struct {
				float x, y, z;
			};
			vec3 v;
		};
	};
	float raw[4];
} quat;

inline quat quat_multiply(const quat &a, const quat &b) {
	quat res;
	res.w = a.w * b.w - vec3_dot(a.v, b.v);
	res.v = vec3_lincom(a.v, b.w, b.v, a.w, vec3_cross(a.v, b.v), 1);
	return res;
}

inline quat quat_multiply(const quat &a, const vec3 &b) {
	quat res;
	res.w = -vec3_dot(a.v, b);
	res.v = vec3_lincom(b, a.w, vec3_cross(a.v, b), 1);
	return res;
}

inline float quat_mag2(const quat &a) {
	return a.w * a.w + a.x * a.x + a.y * a.y + a.z * a.z;
}

inline float quat_mag(const quat &a) {
	return sqrt(quat_mag2(a));
}

inline quat quat_normalize(const quat &a, float *mag) {
	*mag = quat_mag(a);
	quat res = a;
	res.w /= *mag;
	res.x /= *mag;
	res.y /= *mag;
	res.z /= *mag;
	return res;
}

inline void quat_normalize(quat &a) {
	float mag = 1.0f / quat_mag(a);
	a.w *= mag;
	a.x *= mag;
	a.y *= mag;
	a.z *= mag;
}

inline void quat_addto(quat &a, const quat &b, const float f) {
	a.w += b.w * f;
	a.x += b.x * f;
	a.y += b.y * f;
	a.z += b.z * f;
}

#endif
