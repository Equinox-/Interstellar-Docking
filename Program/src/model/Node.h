#ifndef NODE_H_
#define NODE_H_

#include <stdio.h>
#include "../math/matmath.h"
#include "../math/vecmath.h"
#include <stdint.h>

class Mesh;

class Node {
	friend class Model;
private:
	mat4 trans;
	mat4 inverse;
	uint32_t *meshes;
	uint32_t meshCount;
	Mesh **meshRef;

	uint32_t *children;
	uint32_t childrenCount;
	Node **childRef;

	Node *parentRef;

	vec3 com;
	mat4 inertiaTensor, inertiaInverse;
	float mass;
public:
	Node(FILE *io);
	virtual ~Node();
	void updateRefs(Node **nodeTable, Mesh **meshTable);
	void render();

	inline Node* getParent() {
		return parentRef;
	}

	void computePhysics();

	inline float getMass() {
		return mass;
	}

	inline vec3 getCOM() {
		return com;
	}
};

#endif /* NODE_H_ */
