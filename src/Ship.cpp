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
#include "stlLoader.h"

Ship::Ship(const char *fname) {
	rotMatrix = mat4_identity();
	rotInverse = mat4_identity();
	radVel.x = radVel.y = radVel.z = 0;

	pos.x = pos.y = pos.z = 0;
	vel.x = vel.y = vel.z = 0;

	centerOfMen.x = centerOfMen.y = centerOfMen.z = 0;
	mass = 0;

	// Zero Thrusters
	thrusterCount = 0;
	thrusterDir = NULL;
	thrusterPos = NULL;
	radialThrustCoeff = NULL;

	// Zero particles
	particleHead = 0;
	for (uint32_t i = 0; i < PARTICLE_COUNT; i++)
		particles[i].end = -1;

	for (uint32_t i = 0; i < CONTROL_GROUP_COUNT; i++)
		controlGroups[i] = NULL;

	// Load Mesh
	tris = loadSTL_File(fname, &trisCount);

	char thrName[strlen(fname) + 4];
	memcpy(thrName, fname, strlen(fname) + 1);
	strcat(thrName, ".thr");
	loadThrusters(thrName);
	computePhysParams();
	printf("%s\tCOM: %f,%f,%f\tMass: %f\n", fname, centerOfMen.x, centerOfMen.y,
			centerOfMen.z, mass);
}

Ship::~Ship() {
	if (tris != NULL)
		free(tris);
	if (radialThrustCoeff != NULL)
		free(radialThrustCoeff);
	if (thrusterDir != NULL)
		free(thrusterDir);
	if (thrusterPos != NULL)
		free(thrusterPos);
	if (thrusterAxis != NULL)
		free(thrusterAxis);
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
}

void Ship::computePhysParams() {
	mass = 0;
	centerOfMen.x = centerOfMen.y = centerOfMen.z = 0;
	for (uint32_t t = 0; t < trisCount * 4; t += 4) {
		const float partialMass = vec3_tris_area(tris[t + 1], tris[t + 2],
				tris[t + 3]) * AREA_DENSITY;
		const vec3 partialCentroid = vec3_lincom(tris[t + 1], 1.0f / 3.0f,
				tris[t + 2], 1.0f / 3.0f, tris[t + 3], 1.0f / 3.0f);

		mass += partialMass;
		vec3_addto(centerOfMen, partialCentroid, partialMass);
	}
	centerOfMen = vec3_multiply(centerOfMen, 1.0f / mass);

	// Recording ALL DA MOMENTS
	radialThrustCoeff = new float[thrusterCount];
	thrusterAxis = new vec3[thrusterCount];
	for (uint32_t t = 0; t < thrusterCount; t++) {
		radialThrustCoeff[t] = 0;
		// Compute the thrust axis
		float torqueMag;
		thrusterAxis[t] = vec3_normalize(
				vec3_cross(thrusterDir[t],
						vec3_lincom(thrusterPos[t], 1, centerOfMen, -1)),
				&torqueMag);

		for (uint32_t b = 0; b < trisCount * 4; b += 4) {
			const float partialMass = vec3_tris_area(tris[b + 1], tris[b + 2],
					tris[b + 3]) * AREA_DENSITY;
			const vec3 partialCentroid = vec3_lincom(tris[b + 1], 1.0f / 3.0f,
					tris[b + 2], 1.0f / 3.0f, tris[b + 3], 1.0f / 3.0f);
			const vec3 aMinP = vec3_lincom(centerOfMen, 1, partialCentroid, -1);
			const float r2 = vec3_mag2(
					vec3_lincom(centerOfMen, 1, partialCentroid, -1,
							vec3_multiply(thrusterAxis[t],
									vec3_dot(aMinP, thrusterAxis[t])), -1));
			radialThrustCoeff[t] += partialMass * r2;
		}
		radialThrustCoeff[t] = torqueMag / radialThrustCoeff[t];
	}
}

void Ship::render() {
	glPushMatrix();
	glTranslatef(pos.x, pos.y, pos.z);
	glMultMatrixf(rotMatrix.data);

	glTranslatef(-centerOfMen.x, -centerOfMen.y, -centerOfMen.z);

	glColor3f(1, 1, 1);
	glBegin(GL_TRIANGLES);
	for (uint32_t t = 0; t < 4 * trisCount; t += 4) {
		glNormal3f(tris[t].x, tris[t].y, tris[t].z);
		glVertex3f(tris[t + 1].x, tris[t + 1].y, tris[t + 1].z);
		glVertex3f(tris[t + 2].x, tris[t + 2].y, tris[t + 2].z);
		glVertex3f(tris[t + 3].x, tris[t + 3].y, tris[t + 3].z);
	}
	glEnd();

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

	glBegin(GL_LINES);
	glColor3f(0, 1, 0);
	glVertex3f(0, 0, 0);
	glColor3f(1, 0, 0);
	glVertex3f(radVel);
	glEnd();
	glPopAttrib();
}

void Ship::update() {
	const double delta = getDelta();
	const float ctime = glfwGetTime();

	// All da others
	for (uint32_t t = 0; t < thrusterCount; t++) {
		const vec3 patchDir = mat4_multiply(rotMatrix, thrusterDir[t]);
		vec3_addto(vel, patchDir, delta * thrusterPower[t] / mass);
		vec3_addto(radVel, mat4_multiply(rotMatrix, thrusterAxis[t]),
				delta * thrusterPower[t] * radialThrustCoeff[t]);

		if (thrusterPower[t] > 0) {
			particles[particleHead].pos = vec3_lincom(pos, 1,
					mat4_multiply(rotMatrix,
							vec3_lincom(thrusterPos[t], 1, centerOfMen, -1)),
					1);
			particles[particleHead].vel = vec3_lincom(vel, 1, patchDir,
					delta * thrusterPower[t] / PARTICLE_MASS);
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

	vec3_addto(pos, vel, delta);

	float mag;
	vec3 axis = vec3_normalize(radVel, &mag);
	if (mag != 0) {
		rotMatrix = mat4_multiply(rotMatrix,
				mat4_axis_angle(mag * delta, axis));
		rotInverse = mat4_invert(rotMatrix);
	}
}
