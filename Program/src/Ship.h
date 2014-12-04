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
#include "math/vecmath.h"
#include "math/matmath.h"
#include "math/quatmath.h"
#include "particle.h"
#include <stdio.h>
#include "model/Model.h"

class Ship {
private:
	static const int CONTROL_GROUP_COUNT = 12;
	static const int PARTICLE_COUNT = 10000;
	static const float PARTICLE_MASS = 100.0f; //kg
	static const float PARTICLE_LIFE = 5;

	quat rot;
	mat4 rotMatrix, rotInverse;

	vec3 angularMomentum;
	vec3 pos, linearMomentum;

	// Thurster params
	uint32_t thrusterCount;
	vec3 *thrusterCross;
	vec3 *thrusterPos;
	vec3 *thrusterDir;
	float *thrusterPower;

	// Particle system
	Particle particles[PARTICLE_COUNT];
	int particleHead;

	// Control groups
	uint32_t *controlGroups[CONTROL_GROUP_COUNT];

	Model model;

	void loadThrusters(const char *fname);
public:
	Ship(const char *fname);
	virtual ~Ship();

	void render();
	void update();

	inline void setThruster(const int t, const float power) {
		thrusterPower[t] = power;
	}

	inline void addGroup(const uint32_t group, const float power) {
		uint32_t* ctl = controlGroups[group];
		if (ctl == NULL)
			return;
		for (uint32_t t = 0; t < ctl[0]; t++)
			thrusterPower[ctl[t + 1]] += power;
	}

	inline void addWorldThrust(vec3 power) {
//		 World -> local
		vec3 lv = mat4_multiply(rotInverse, power);
		for (uint8_t i = 0; i < 3; i++) {
			addGroup(i * 2, (lv.comp[i] > 0) * lv.comp[i]);
			addGroup(i * 2 + 1, (lv.comp[i] < 0) * -lv.comp[i]);
		}
	}

	inline void zeroThrusters() {
		for (uint32_t t = 0; t < thrusterCount; t++)
			thrusterPower[t] = 0;
	}
};

#endif /* SHIP_H_ */
