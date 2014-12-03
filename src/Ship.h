/*
 * Ship.h
 *
 *  Created on: Dec 2, 2014
 *      Author: localadmin
 */

#ifndef SHIP_H_
#define SHIP_H_

#include <stdint.h>
#include "vecmath.h"

class Ship {
private:
	vec3 rot, radVel;
	vec3 pos, vel;

	// Physics params
	vec3 centerOfMen;
	float *thrustCoeff;
	float mass;

	// Thurster params
	uint32_t thrusterCount;
	vec3 *thrusterAxis;
	vec3 *thrusterPos;
	vec3 *thrusterDir;
	float *thrusterPower;

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

	inline void zeroThrusters() {
		for (uint32_t t = 0; t < thrusterCount; t++)
			thrusterPower[t] = 0;
	}
};

#endif /* SHIP_H_ */
