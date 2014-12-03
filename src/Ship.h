/*
 * Ship.h
 *
 *  Created on: Dec 2, 2014
 *      Author: localadmin
 */

#ifndef SHIP_H_
#define SHIP_H_

#include <stdint.h>
#include <stddef.h>
#include "vecmath.h"
#include "matmath.h"
#include "particle.h"
#include <stdio.h>

class Ship {
private:
	static const int CONTROL_GROUP_COUNT = 12;
	static const int PARTICLE_COUNT = 10000;

	mat4 rotMatrix, rotInverse;
	vec3 rot, radVel;
	vec3 pos, vel;

	// Physics params
	vec3 centerOfMen;
	float *radialThrustCoeff;
	float mass;

	// Thurster params
	uint32_t thrusterCount;
	vec3 *thrusterAxis;
	vec3 *thrusterPos;
	vec3 *thrusterDir;
	float *thrusterPower;

	// Particle system
	Particle particles[PARTICLE_COUNT];
	int particleHead;

	// Control groups
	uint32_t *controlGroups[CONTROL_GROUP_COUNT];

	uint32_t trisCount;
	vec3 *tris;

	void loadThrusters(const char *fname);
	void computePhysParams();
public:
	Ship(const char *fname);
	virtual ~Ship();

	void render();
	void update();

	inline void setThruster(const int t, const float power) {
		thrusterPower[t] = power;
	}

	inline void setGroup(const uint32_t group, const float power) {
		uint32_t* ctl = controlGroups[group];
		if (ctl == NULL)
			return;
		for (uint32_t t = 0; t < ctl[0]; t++)
			thrusterPower[ctl[t + 1]] = power;
	}

	inline void worldThrust(vec3 power) {
		// World -> local
		vec3 lv = mat4_multiply(rotInverse, power);
		for (uint8_t i = 0; i < 3; i++) {
			setGroup(i * 2, (lv.comp[i] > 0) * lv.comp[i]);
			setGroup(i * 2 + 1, (lv.comp[i] < 0) * -lv.comp[i]);
		}
	}

	inline void zeroThrusters() {
		for (uint32_t t = 0; t < thrusterCount; t++)
			thrusterPower[t] = 0;
	}

	inline void pose(const vec3 pos, const vec3 rot) {
		this->pos = pos;
		this->rot = rot;
	}
};

#endif /* SHIP_H_ */
