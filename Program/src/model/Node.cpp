#include "Node.h"

#include <stdlib.h>
#include "Mesh.h"
#include <GL/gl.h>

Node::Node(FILE *io) {
	fread(&childrenCount, sizeof(uint32_t), 1, io);
	children = (uint32_t*) malloc(sizeof(uint32_t) * childrenCount);
	childRef = (Node**) malloc(sizeof(Node*) * childrenCount);
	fread(children, sizeof(uint32_t), childrenCount, io);

	fread(&meshCount, sizeof(uint32_t), 1, io);
	meshes = (uint32_t*) malloc(sizeof(uint32_t) * meshCount);
	meshRef = (Mesh**) malloc(sizeof(Mesh*) * meshCount);
	fread(meshes, sizeof(uint32_t), meshCount, io);

	char flag;
	fread(&flag, 1, 1, io);
	if (flag) {
		fread(trans.data, sizeof(float) * 16, 1, io);
	} else {
		trans = mat4_identity();
	}
	inverse = mat4_invert(trans);

	parentRef = NULL;

	mass = 0;
	com.x = com.y = com.z = 0;
}

Node::~Node() {
	free(meshes);
	free(children);
}

void Node::updateRefs(Node **nodeTable, Mesh **meshTable) {
	for (uint32_t i = 0; i < childrenCount; i++) {
		childRef[i] = nodeTable[children[i]];
		nodeTable[children[i]]->parentRef = this;
	}

	for (uint32_t i = 0; i < meshCount; i++)
		meshRef[i] = meshTable[meshes[i]];
}

void Node::render() {
	glPushMatrix();
	glMultMatrixf(trans.data);
	for (uint32_t i = 0; i < meshCount; i++)
		meshRef[i]->render();
	for (uint32_t i = 0; i < childrenCount; i++)
		childRef[i]->render();
	glPopMatrix();
}

void Node::computePhysics() {
	mass = 0;
	com.x = com.y = com.z;
	for (uint32_t i = 0; i < meshCount; i++) {
		mass += meshRef[i]->mass;
		vec3_addto(com, meshRef[i]->com, meshRef[i]->mass);
	}

	for (uint32_t i = 0; i < childrenCount; i++) {
		childRef[i]->computePhysics();
		mass += childRef[i]->mass;
		vec3_addto(com, childRef[i]->com, childRef[i]->mass);
	}
	com = vec3_multiply(com, 1.0f / mass);

	// Now the inertia tensor
	inertiaTensor = mat4_identity();
	inertiaTensor.data[0] = inertiaTensor.data[5] = inertiaTensor.data[10] = 0;

	// Add in other values
	for (uint32_t i = 0; i < meshCount; i++) {
		mat4_addto(inertiaTensor, meshRef[i]->inertiaTensor);
		mat4_add_inertia_tensor(inertiaTensor, meshRef[i]->mass,
				vec3_lincom(meshRef[i]->com, 1, com, -1));
	}
	for (uint32_t i = 0; i < childrenCount; i++) {
		mat4_addto(inertiaTensor, childRef[i]->inertiaTensor);
		mat4_add_inertia_tensor(inertiaTensor, childRef[i]->mass,
				vec3_lincom(childRef[i]->com, 1, com, -1));
	}

	// Now convert out of body space into parent space
	com = mat4_multiply(trans, com);

	const mat4 rotMat = mat4_mat3(trans);
	inertiaTensor = mat4_inertia_tensor_multiply(inertiaTensor, rotMat);
	inertiaInverse = mat4_invert(inertiaTensor);
}
