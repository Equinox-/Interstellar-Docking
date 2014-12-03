#ifndef __STL_LOADER_H
#define __STL_LOADER_H

#include <stdio.h>

vec3 *loadSTL_File(const char* fname, uint32_t *trisCount) {
	FILE *f = fopen(fname, "r");
	char buff[80];
	fread(&buff, sizeof(uint8_t), 80, f);

	fread(trisCount, sizeof(uint32_t), 1, f);
	vec3 *data = (vec3*) malloc(sizeof(vec3) * 4 * (*trisCount));
	uint32_t head = 0;
	for (head = 0; head < *trisCount * 4; head += 4) {
		fread(&data[head].raw, sizeof(float), 3, f);	// Norm lol
		fread(&data[head + 1].raw, sizeof(float), 3, f);
		fread(&data[head + 2].raw, sizeof(float), 3, f);
		fread(&data[head + 3].raw, sizeof(float), 3, f);
		fread(&buff, sizeof(uint16_t), 1, f);
	}
	fclose(f);
	return data;
}

#endif
