#include "particle.h"
#include "istime.h"

void particle_render(Particle &p) {
//	glColor4f(p.color.x, p.color.y, p.color.z,
//			(glfwGetTime() - p.begin) / (p.end - p.begin));
	glColor3f(1,0,0);
	glVertex3f(p.pos.x, p.pos.y, p.pos.z);
	p.pos = vec3_lincom(p.pos, 1, p.vel, getDelta());
}
