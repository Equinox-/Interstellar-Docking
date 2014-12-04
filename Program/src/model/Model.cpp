#include "Model.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "Mesh.h"
#include "Texture.h"
#include "Node.h"

Model::Model(const char *fname) {
	const char *term = rindex(fname, '/');
	int dirName = term > fname ? 1 + strlen(fname) - strlen(term) : 0;

	FILE *f = fopen(fname, "r");

	fread(&textureCount, sizeof(uint32_t), 1, f);
	textureTable = new Texture*[textureCount];
	for (uint32_t i = 0; i < textureCount; i++) {
		uint32_t len = 0;
		fread(&len, sizeof(uint32_t), 1, f);
		char *data = new char[dirName + len + 1];
		memcpy(data, fname, dirName);
		fread(data + dirName, sizeof(char), len, f);
		data[len + dirName] = 0;
		textureTable[i] = new Texture(data);
		printf("%d\t%s\n", i, data);
		delete[] data;
	}

	fread(&materialCount, sizeof(uint32_t), 1, f);
	materialTable = new Material[materialCount];
	fread(materialTable, sizeof(Material), materialCount, f);

	fread(&meshCount, sizeof(uint32_t), 1, f);
	meshTable = new Mesh*[meshCount];
	for (uint32_t i = 0; i < meshCount; i++) {
		meshTable[i] = new Mesh(f);
		meshTable[i]->updateMaterialRef(materialTable, textureTable);
	}

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
	for (uint32_t i = 0; i < textureCount; i++)
		delete textureTable[i];
	delete[] textureTable;
	delete[] materialTable;
	for (uint32_t i = 0; i < nodeCount; i++)
		delete nodeTable[i];
	delete[] nodeTable;
	for (uint32_t i = 0; i < meshCount; i++)
		delete meshTable[i];
	delete[] meshTable;
}

void Model::render() {
	root->render();

//	glEnable(GL_TEXTURE_2D);
//	float x = 0;
//	for (uint32_t i = 0; i < textureCount; i++) {
//		textureTable[i]->bind();
//
//		glBegin(GL_QUADS);
//		glColor3f(1, 1, 1);
//		glTexCoord2f(0, 0);
//		glVertex3f(x / 100.0f, 0, 0);
//		glTexCoord2f(1, 0);
//		glVertex3f((x + textureTable[i]->getWidth()) / 100.0f, 0, 0);
//		glTexCoord2f(1, 1);
//		glVertex3f((x + textureTable[i]->getWidth()) / 100.0f,
//				textureTable[i]->getHeight() / 100.0f, 0);
//		glTexCoord2f(0, 1);
//		glVertex3f(x / 100.0f, textureTable[i]->getHeight() / 100.0f, 0);
//		glEnd();
//		x += textureTable[i]->getWidth();
//	}
//	glDisable(GL_TEXTURE_2D);
}
