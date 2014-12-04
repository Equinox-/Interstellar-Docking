#ifndef __MATERIAL_H
#define __MATERIAL_H

typedef struct {
	uint32_t imageID;
	union {
		struct {
			float p1, p2, p3, p4;
		};
		float args[4];
	};
} SourceData;

typedef struct {
	SourceData ambient;
	SourceData diffuse;
	SourceData emission;
	int indexOfRefraction;
	SourceData reflective;
	float reflectivity;
	float shininess;
	SourceData specular;
} Material;

#include <GL/gl.h>
inline void glBindMaterial(const Material *mat) {
	if (mat->ambient.imageID == 0) {
		glMaterialfv(GL_FRONT_AND_BACK, GL_AMBIENT, mat->ambient.args);
	}
	if (mat->diffuse.imageID == 0) {
		glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, mat->diffuse.args);
	}
	if (mat->emission.imageID == 0) {
		glMaterialfv(GL_FRONT_AND_BACK, GL_EMISSION, mat->diffuse.args);
	}
	glMaterialf(GL_FRONT_AND_BACK, GL_SHININESS, mat->shininess);
}
#endif
