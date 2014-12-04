#include "Mesh.h"

#include <GL/gl.h>
#include "Material.h"
#include "Texture.h"
#include <stdlib.h>

Mesh::Mesh(FILE *io) {
	// All da little indians!
	fread(&vertexCount, sizeof(uint32_t), 1, io);
	vertData = (float*) malloc(sizeof(float) * VERTEX_STRIDE * vertexCount);
	fread(vertData, sizeof(float), VERTEX_STRIDE * vertexCount, io);

	fread(&indexCount, sizeof(uint32_t), 1, io);
	indexData = (uint32_t*) malloc(sizeof(uint32_t) * indexCount);
	fread(indexData, sizeof(uint32_t), indexCount, io);

	fread(&material, sizeof(uint32_t), 1, io);
	materialRef = NULL;

	computePhysics();
}

Mesh::~Mesh() {
	free(vertData);
	free(indexData);
}
void Mesh::updateMaterialRef(Material *matTable, Texture **texTable) {
	this->texTable = texTable;
	if (material > 0)
		materialRef = &matTable[material - 1];
	else
		materialRef = NULL;
}

void Mesh::render() {
	if (materialRef != NULL) {
		glBindMaterial(materialRef);
		if (materialRef->diffuse.imageID > 0) {
			glEnable(GL_TEXTURE_2D);
			texTable[materialRef->diffuse.imageID - 1]->bind();
		}
	}
	glEnable(GL_VERTEX_ARRAY | GL_NORMAL_ARRAY | GL_TEXTURE_COORD_ARRAY);
	glInterleavedArrays(GL_T2F_N3F_V3F, sizeof(float) * VERTEX_STRIDE,
			vertData);
	glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, indexData);
	glDisable(GL_VERTEX_ARRAY | GL_NORMAL_ARRAY | GL_TEXTURE_COORD_ARRAY);
	glDisable(GL_TEXTURE_2D);
}

void Mesh::computePhysics() {
	mass = 0;
	com.x = com.y = com.z = 0;
	for (uint32_t t = 0; t < indexCount; t += 3) {
		vec3 *a = (vec3*) (vertData + indexData[t] * VERTEX_STRIDE + 5);
		vec3 *b = (vec3*) (vertData + indexData[t + 1] * VERTEX_STRIDE + 5);
		vec3 *c = (vec3*) (vertData + indexData[t + 2] * VERTEX_STRIDE + 5);

		const float partialMass = vec3_tris_area(*a, *b, *c) * AREA_DENSITY;
		const vec3 partialCentroid = vec3_lincom(*a, 1.0f / 3.0f, *b,
				1.0f / 3.0f, *c, 1.0f / 3.0f);

		mass += partialMass;
		vec3_addto(com, partialCentroid, partialMass);
	}
	com = vec3_multiply(com, 1.0f / mass);

	// Compute the inertia tensor
	inertiaTensor = mat4_identity();
	inertiaTensor.data[0] = inertiaTensor.data[5] = inertiaTensor.data[10] = 0;

	for (uint32_t t = 0; t < indexCount; t += 3) {
		vec3 *a = (vec3*) (vertData + indexData[t] * VERTEX_STRIDE + 5);
		vec3 *b = (vec3*) (vertData + indexData[t + 1] * VERTEX_STRIDE + 5);
		vec3 *c = (vec3*) (vertData + indexData[t + 2] * VERTEX_STRIDE + 5);

		const float partialMass = vec3_tris_area(*a, *b, *c) * AREA_DENSITY;
		const vec3 comOff = vec3_lincom(
				vec3_lincom(*a, 1.0f / 3.0f, *b, 1.0f / 3.0f, *c, 1.0f / 3.0f),
				1, com, -1);

		mat4_add_inertia_tensor(inertiaTensor, partialMass, comOff);
	}
}
