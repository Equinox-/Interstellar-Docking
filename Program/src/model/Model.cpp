#include "Model.h"

#include <stdio.h>
#include <stdlib.h>

#include "Mesh.h"
#include "Node.h"

Model::Model(const char *fname) {
	FILE *f = fopen(fname, "r");
	fread(&meshCount, sizeof(uint32_t), 1, f);
	meshTable = new Mesh*[meshCount];
	for (uint32_t i = 0; i < meshCount; i++)
		meshTable[i] = new Mesh(f);
	fread(&nodeCount, sizeof(uint32_t), 1, f);
	nodeTable = new Node*[nodeCount];
	for (uint32_t i = 0; i < nodeCount; i++)
		nodeTable[i] = new Node(f);
	fclose(f);

	for (uint32_t i = 0; i < nodeCount; i++)
		nodeTable[i]->updateRefs(nodeTable, meshTable);
	uint32_t rootCount = 0;
	for (uint32_t i = 0; i < nodeCount; i++)
		if (nodeTable[i]->getParent() == NULL) {
			rootCount++;
			root = nodeTable[i];
		}
	if (rootCount > 1) {
		printf("Multiple root node\n");
		exit(1);
	}
	root->computePhysics();

	printf("Model Physics: (%s)\n", fname);
	printf("Mass: %f\n", root->mass);
	printf("COM: %f %f %f\n", root->com.x, root->com.y, root->com.z);
	printf("I:\t%.5f %.5f %.5f\n", root->inertiaTensor.data[0],
			root->inertiaTensor.data[4], root->inertiaTensor.data[8]);
	printf("\t%.5f %.5f %.5f\n", root->inertiaTensor.data[1],
			root->inertiaTensor.data[5], root->inertiaTensor.data[9]);
	printf("\t%.5f %.5f %.5f\n", root->inertiaTensor.data[2],
			root->inertiaTensor.data[6], root->inertiaTensor.data[10]);
}

Model::~Model() {
	for (uint32_t i = 0; i < nodeCount; i++)
		delete nodeTable[i];
	delete[] nodeTable;
	for (uint32_t i = 0; i < meshCount; i++)
		delete meshTable[i];
	delete[] meshTable;
}

void Model::render() {
	root->render();
}
