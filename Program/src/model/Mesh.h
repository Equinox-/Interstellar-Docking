#ifndef MESH_H_
#define MESH_H_

#include <stdint.h>
#include <stdio.h>

#include "../math/vecmath.h"
#include "../math/matmath.h"
#include "Material.h"

class Texture;
class Mesh {
	friend class Node;
private:
	static const uint32_t VERTEX_STRIDE = 2 + 3 + 3;
	static const float AREA_DENSITY = 0.1; // 0.1kg/m^2

	float *vertData;
	uint32_t *indexData;
	uint32_t indexCount, vertexCount;

	uint32_t material;
	Material *materialRef;
	Texture **texTable;

	float mass;
	vec3 com;
	mat4 inertiaTensor;
	void computePhysics();
public:
	Mesh(FILE *io);
	virtual ~Mesh();
	void render();
	void updateMaterialRef(Material *matTable, Texture **texTable);
};

#endif /* MESH_H_ */
