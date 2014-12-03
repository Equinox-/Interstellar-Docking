/*
 * Ship.cpp
 *
 *  Created on: Dec 2, 2014
 *      Author: localadmin
 */

#include "Ship.h"

#include <stddef.h>
#include <stdlib.h>
#include <GL/gl.h>
#include "stlLoader.h"
#include "istime.h"
#include <string.h>

Ship::Ship(const char *fname) {
	rot.x = rot.y = rot.z = 0;
	radVel.x = radVel.y = radVel.z = 0;

	pos.x = pos.y = pos.z = 0;
	vel.x = vel.y = vel.z = 0;

	centerOfMen.x = centerOfMen.y = centerOfMen.z = 0;
	thrustCoeff = NULL;
	mass = 0;

	// Zero Thrusters
	thrusterCount = 0;
	thrusterDir = NULL;
	thrusterPos = NULL;

	// Load Mesh
	tris = loadSTL_File(fname, &trisCount);

	char thrName[strlen(fname) + 4];
	memcpy(thrName, fname, strlen(fname) + 1);
	strcat(thrName, ".thr");
	loadThrusters(thrName);
	computePhysParams();
}

Ship::~Ship() {
	if (tris != NULL)
		free(tris);
	if (thrustCoeff != NULL)
		free(thrustCoeff);
	if (thrusterDir != NULL)
		free(thrusterDir);
	if (thrusterPos != NULL)
		free(thrusterPos);
}

void Ship::loadThrusters(const char *thrName) {
	if (thrusterDir != NULL)
		free(thrusterDir);
	if (thrusterPos != NULL)
		free(thrusterPos);
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

	fclose(thr);
}

void Ship::computePhysParams() {
	mass = 0;
	centerOfMen.x = centerOfMen.y = centerOfMen.z = 0;
	for (uint32_t t = 0; t < trisCount * 4; t += 4) {
		float partialMass = vec3_tris_area(tris[t + 1], tris[t + 2],
				tris[t + 3]);
		vec3 partialCentroid = vec3_lincom(tris[t + 1], 1.0f / 3.0f,
				tris[t + 2], 1.0f / 3.0f, tris[t + 3], 1.0f / 3.0f);

		mass += partialMass;
		centerOfMen = vec3_lincom(centerOfMen, 1, partialCentroid, partialMass);
	}
	centerOfMen = vec3_multiply(centerOfMen, 1.0f / mass);

	// Recording ALL DA MOMENTS
	thrustCoeff = new float[thrusterCount];
	thrusterAxis = new vec3[thrusterCount];
	for (uint32_t t = 0; t < thrusterCount; t++) {
		thrustCoeff[t] = 0;
		// Compute the thrust axis
		float torqueMag;
		thrusterAxis[t] = vec3_normalize(
				vec3_cross(thrusterDir[t],
						vec3_lincom(thrusterPos[t], 1, centerOfMen, -1)),
				&torqueMag);

		for (uint32_t b = 0; b < trisCount * 4; b += 4) {
			const float partialMass = vec3_tris_area(tris[b + 1], tris[b + 2],
					tris[b + 3]);
			const vec3 p = vec3_lincom(tris[b + 1], 1.0f / 3.0f, tris[b + 2],
					1.0f / 3.0f, tris[b + 3], 1.0f / 3.0f);
			const vec3 aMinP = vec3_lincom(centerOfMen, 1, p, -1);
			const float r2 = vec3_mag2(
					vec3_lincom(centerOfMen, 1, p, -1,
							vec3_multiply(thrusterAxis[t],
									vec3_dot(aMinP, thrusterAxis[t])), -1));
			thrustCoeff[t] += partialMass * r2;
		}
		thrustCoeff[t] = torqueMag / thrustCoeff[t];
	}
}

void Ship::render() {
	glPushMatrix();
	glTranslatef(pos.x, pos.y, pos.z);

	float mag;
	vec3 axis = vec3_normalize(rot, &mag);
	glRotatef(mag * 180.0f / M_PI, axis.x, axis.y, axis.z);

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

// Render thrusters
	glBegin(GL_LINES);
	for (uint32_t t = 0; t < thrusterCount; t++) {
		glColor3f(0, 1, 0);
		glVertex3f(thrusterPos[t].x, thrusterPos[t].y, thrusterPos[t].z);
		glColor3f(1, 0, 0);
		glVertex3f(thrusterPos[t].x + thrusterDir[t].x,
				thrusterPos[t].y + thrusterDir[t].y,
				thrusterPos[t].z + thrusterDir[t].z);
	}
	glEnd();
	glPopMatrix();
}

void Ship::update() {
	const double delta = getDelta();

	// All da others
	for (uint32_t t = 0; t < thrusterCount; t++) {
		vel = vec3_lincom(vel, 1, thrusterDir[t], thrusterPower[t] / mass);
		radVel = vec3_lincom(radVel, 1, thrusterAxis[t],
				thrusterPower[t] * thrustCoeff[t]);
	}

	pos = vec3_lincom(pos, 1, vel, delta);
	rot = vec3_lincom(rot, 1, radVel, delta);
}
