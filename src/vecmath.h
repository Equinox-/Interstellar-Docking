#ifndef VECMATH_H_
#define VECMATH_H_

typedef union {
	struct {
		float x, y, z;
	};
	float comp[3];
	char raw[12];
} vec3;

#include <math.h>

inline vec3 vec3_make(const float x, const float y, const float z) {
	vec3 v;
	v.x = x;
	v.y = y;
	v.z = z;
	return v;
}

inline vec3 vec3_add(vec3 a, const vec3 &b) {
	a.x += b.x;
	a.y += b.y;
	a.z += b.z;
	return a;
}

inline vec3 vec3_multiply(vec3 v, const float f) {
	v.x *= f;
	v.y *= f;
	v.z *= f;
	return v;
}

inline vec3 vec3_lincom(const vec3 &a, const float aV, const vec3 &b,
		const float bV) {
	vec3 v;
	v.x = a.x * aV + b.x * bV;
	v.y = a.y * aV + b.y * bV;
	v.z = a.z * aV + b.z * bV;
	return v;
}

inline vec3 vec3_lincom(const vec3 &a, const float aV, const vec3 &b,
		const float bV, const vec3 &c, const float cV) {
	vec3 v;
	v.x = a.x * aV + b.x * bV + c.x * cV;
	v.y = a.y * aV + b.y * bV + c.y * cV;
	v.z = a.z * aV + b.z * bV + c.z * cV;
	return v;
}

inline float vec3_mag2(const vec3 &v) {
	return (v.x * v.x + v.y * v.y + v.z * v.z);
}

inline float vec3_mag(const vec3 &v) {
	return sqrt(vec3_mag2(v));
}

inline vec3 vec3_normalize(vec3 v, float *outMag) {
	*outMag = vec3_mag(v);
	v.x /= *outMag;
	v.y /= *outMag;
	v.z /= *outMag;
	return v;
}

inline vec3 vec3_cross(const vec3 &a, const vec3 &b) {
	vec3 res;
	res.x = a.y * b.z - a.z * b.y;
	res.y = a.z * b.x - a.x * b.z;
	res.z = a.x * b.y - a.y * b.x;
	return res;
}

inline float vec3_dot(const vec3 &a, const vec3 &b) {
	return a.x * b.x + a.y * b.y + a.z * b.z;
}

inline float vec3_tris_area(const vec3 &a, const vec3 &b, const vec3 &c) {
	return vec3_mag(
			vec3_cross(vec3_lincom(a, 1, b, -1), vec3_lincom(c, 1, b, -1)))
			/ 2.0f;
}

#endif
