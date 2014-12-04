#ifndef __PARTICLE_H
#define __PARTICLE_H

#include <GL/gl.h>
#include <GLFW/glfw3.h>
#include "math/vecmath.h"

typedef struct {
	vec3 color;
	vec3 pos;
	vec3 vel;
	double begin, end;
} Particle;

void particle_render(Particle &p);
#endif
