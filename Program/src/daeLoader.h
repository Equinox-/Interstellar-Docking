#ifndef __DAE_LOADER_H
#define __DAE_LOADER_H

#include <stdio.h>

vec3 *loadDAE_File(const char* fname, uint32_t *trisCount) {
	char partName[strlen(fname) + 8 + 1];
	memcpy(partName, fname, strlen(fname) + 1);
	strcat(partName, "-idx.bin");
	FILE *idx = fopen(partName, "r");
	memcpy(partName, fname, strlen(fname) + 1);
	strcat(partName, "-vtx.bin");
	FILE *vtx = fopen(partName, "r");
	printf("%s\n", partName);

	float bonus[3];
	vec3 vertRaw[26266 * 2];
	for (int i = 0; i < 26266; i++) {
		fread(vertRaw[i * 2].raw, sizeof(float), 3, vtx);
		fread(vertRaw[i * 2 + 1].raw, sizeof(float), 3, vtx);
		fread(bonus, sizeof(float), 2, vtx);
		fread(bonus, sizeof(float), 3, vtx);
		fread(bonus, sizeof(float), 3, vtx);
	}
	fclose(vtx);
	printf("Loaded vertex data\n");
	vec3 *verts = new vec3[39084 * 2];
	uint32_t v;
	*trisCount = 39084/3;
	for (int i = 0; i < 39084; i++) {
		if (!fread(&v, sizeof(uint32_t), 1, idx))
			printf("Fail\n");
		verts[i * 2+1] = vertRaw[v * 2];
		verts[i * 2] = vertRaw[v * 2 + 1];
//		printf("%f %f %f\t", verts[i * 2].x, verts[i * 2].y, verts[i * 2].z);
	}
	fclose(idx);
	return verts;
}

#endif
