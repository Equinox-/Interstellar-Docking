/*
 * Model.h
 *
 *  Created on: Dec 4, 2014
 *      Author: localadmin
 */

#ifndef MODEL_H_
#define MODEL_H_

#include <stdint.h>
#include "../math/matmath.h"
#include "../math/vecmath.h"
#include "Node.h"

class Mesh;
class Model {
private:
	uint32_t nodeCount;
	Node **nodeTable;
	uint32_t meshCount;
	Mesh **meshTable;
	Node *root;
public:
	Model(const char *fname);
	virtual ~Model();
	void render();

	inline vec3 getCOM() {
		return root->com;
	}

	inline mat4 getInertiaTensor() {
		return root->inertiaTensor;
	}

	inline mat4 getInertiaTensorInverse() {
		return root->inertiaInverse;
	}

	inline float getMass() {
		return root->mass;
	}
};

#endif /* MODEL_H_ */
