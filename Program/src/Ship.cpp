/*
 * Ship.cpp
 *
 *  Created on: Dec 2, 2014
 *      Author: localadmin
 */

#include "Ship.h"

#include <GL/gl.h>
#include <GLFW/glfw3.h>
#include <stdlib.h>
#include <string.h>

#include "istime.h"
#include "daeLoader.h"

Ship::Ship(const char *fname) :
		model(fname) {
	rot.w = 1;
	rot.x = rot.y = rot.z = 0;

	angularMomentum.x = angularMomentum.y = angularMomentum.z = 0;

	pos.x = pos.y = pos.z = 0;
	linearMomentum.x = linearMomentum.y = linearMomentum.z = 0;

	// Zero Thrusters
	thrusterCount = 0;
	thrusterDir = NULL;
	thrusterPos = NULL;

	// Zero particles
	particleHead = 0;
	for (uint32_t i = 0; i < PARTICLE_COUNT; i++)
		particles[i].end = -1;

	for (uint32_t i = 0; i < CONTROL_GROUP_COUNT; i++)
		controlGroups[i] = NULL;

	char *thrName = "data/endurance_wheel.stl.thr"; //[strlen(fname) + 4];
//	memcpy(thrName, fname, strlen(fname) + 1);
//	strcat(thrName, ".thr");
	loadThrusters(thrName);
}

Ship::~Ship() {
	if (thrusterDir != NULL)
		free(thrusterDir);
	if (thrusterPos != NULL)
		free(thrusterPos);
	if (thrusterCross != NULL)
		free(thrusterCross);
	if (thrusterPower != NULL)
		free(thrusterPower);
	for (uint32_t i = 0; i < CONTROL_GROUP_COUNT; i++)
		if (controlGroups[i] != NULL)
			free(controlGroups[i]);
}

void Ship::loadThrusters(const char *thrName) {
	FILE *thr = fopen(thrName, "r");
	if (thr == NULL)
		return;
	fscanf(thr, "%u", &thrusterCount);
	thrusterPos = new vec3[thrusterCount];
	thrusterDir = new vec3[thrusterCount];
	thrusterPower = new float[thrusterCount];
	for (uint32_t t = 0; t < thrusterCount; t++) {
		thrusterPower[t] = 0;
		fscanf(thr, "%f %f %f\t%f %f %f\n", &thrusterPos[t].x,
				&thrusterPos[t].y, &thrusterPos[t].z, &thrusterDir[t].x,
				&thrusterDir[t].y, &thrusterDir[t].z);
	}

	for (uint32_t i = 0; i < CONTROL_GROUP_COUNT; i++) {
		uint32_t count;
		fscanf(thr, "%u", &count);
		controlGroups[i] = (uint32_t*) malloc(sizeof(uint32_t) * (count + 1));
		controlGroups[i][0] = count;
		for (uint32_t j = 0; j < count; j++)
			fscanf(thr, "%u", &controlGroups[i][1 + j]);
	}
	fclose(thr);

	// Compute thruster crosses
	thrusterCross = new vec3[thrusterCount];
	for (uint32_t t = 0; t < thrusterCount; t++) {
		thrusterCross[t] = vec3_cross(thrusterDir[t],
				vec3_lincom(thrusterPos[t], 1, model.getCOM(), -1));
	}
}

void Ship::render() {
	glPushMatrix();
	glTranslatef(pos.x, pos.y, pos.z);
	glMultMatrixf(rotMatrix.data);

//	glTranslatef(-centerOfMen.x, -centerOfMen.y, -centerOfMen.z);

	glColor3f(1, 1, 1);
	model.render();

	// Debug
	glPushAttrib(GL_LIGHTING);
	// Render thrusters
	glDisable(GL_LIGHTING);
	glBegin(GL_LINES);
	for (uint32_t t = 0; t < thrusterCount; t++) {
		glColor3f(0, 1, 0);
		glVertex3f(thrusterPos[t].x, thrusterPos[t].y, thrusterPos[t].z);
		glColor3f(1, 0, 0);
		const float drawMag = thrusterPower[t];
		glVertex3f(thrusterPos[t].x + drawMag * thrusterDir[t].x,
				thrusterPos[t].y + drawMag * thrusterDir[t].y,
				thrusterPos[t].z + drawMag * thrusterDir[t].z);
	}
	glEnd();
	glPopMatrix();

	glBegin(GL_POINTS);
	const float ctime = glfwGetTime();
	for (uint32_t i = 0; i < PARTICLE_COUNT; i++)
		if (particles[i].end > ctime)
			particle_render(particles[i]);
	glEnd();

	glPopAttrib();
}

void Ship::update() {
	const double delta = getDelta();
	const float ctime = glfwGetTime();

	quat omega;
	omega.w = 0;
	omega.v = mat4_multiply(model.getInertiaTensorInverse(), angularMomentum);

	// All da others
	for (uint32_t t = 0; t < thrusterCount; t++) {
		if (thrusterPower[t] > 0) {
			const vec3 patchDir = mat4_multiply(rotMatrix, thrusterDir[t]);
			vec3_addto(linearMomentum, patchDir, delta * thrusterPower[t]);
			vec3_addto(angularMomentum,
					mat4_multiply(rotMatrix, thrusterCross[t]),
					delta * thrusterPower[t]);

			const vec3 patchPos = mat4_multiply(rotMatrix, thrusterPos[t]);
			particles[particleHead].pos = vec3_lincom(pos, 1, patchPos, 1);
			particles[particleHead].vel = vec3_lincom(linearMomentum,
					1.0f / model.getMass(), patchDir,
					delta * thrusterPower[t] / PARTICLE_MASS,
					vec3_cross(omega.v, patchPos), 1);
			particles[particleHead].begin = ctime;
			particles[particleHead].end = ctime + PARTICLE_LIFE;
			particles[particleHead].color.x = 1;
			particles[particleHead].color.y = particles[particleHead].color.z =
					0;
			particleHead++;
			if (particleHead >= PARTICLE_COUNT)
				particleHead = 0;
		}
	}

	vec3_addto(pos, linearMomentum, 1.0f / model.getMass());

	quat_addto(rot, quat_multiply(omega, rot), 0.5f * delta);
	quat_normalize(rot);

	rotMatrix = mat4_from_quat(rot);
	rotInverse = mat4_invert(rotMatrix);
}
