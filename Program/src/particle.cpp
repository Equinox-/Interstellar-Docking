#include "particle.h"
#include "istime.h"

void particle_render(Particle &p) {
	glColor4f(p.color.x, p.color.y, p.color.z,
			1.0f - (glfwGetTime() - p.begin) / (p.end - p.begin));
	glVertex3f(p.pos.x, p.pos.y, p.pos.z);
	vec3_addto(p.pos, p.vel, getDelta());
}
