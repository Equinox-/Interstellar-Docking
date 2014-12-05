#include <GL/glew.h>
#include <GL/gl.h>
#include <GLFW/glfw3.h>
#include <math.h>
#include <stdint.h>
#include <stdio.h>

#include "Camera.h"
#include "math/vecmath.h"
#include "model/Texture.h"
#include "model/Mesh.h"
#include "shaders.h"
#include "Ship.h"

#define HORIZ_FOV (45)

void windowResized(GLFWwindow* win, int width, int height) {
	glViewport(0, 0, width, height);
	glMatrixMode(GL_PROJECTION);
	glLoadIdentity();
	const float tanV = (float) tan(HORIZ_FOV * M_PI / 180.0);
	const float aspect = height / (float) width;
	glFrustum(-tanV, tanV, -tanV * aspect, tanV * aspect, 1, 1000);
}

double delta;

const int groupCTL[] = { GLFW_KEY_D, GLFW_KEY_A, GLFW_KEY_W, GLFW_KEY_S,
GLFW_KEY_Q, GLFW_KEY_E, GLFW_KEY_J, GLFW_KEY_L, GLFW_KEY_I, GLFW_KEY_K,
GLFW_KEY_U, GLFW_KEY_O };

const float lightDir[] = { -1, -1, -1 };
const float lightColor[] = { 1, 1, 1, 1 };
const float ambientColor[] = { 0.1f, 0.1f, 0.1f, 1 };

void sphere(const float radius) {
	const float drad = (M_PI / 2.0f / 200.0f);
	glColor3f(1, 1, 1);
	glBegin(GL_QUADS);
	for (float slir = -M_PI / 2; slir < M_PI / 2; slir += drad) {
		const float cosHere = cos(slir);
		const float cosAbove = cos(slir + drad);
		const float sinHere = sin(slir);
		const float sinAbove = sin(slir + drad);
		for (float rad = 0; rad < 2 * M_PI; rad += drad) {
			const float cc = cos(rad);
			const float ss = sin(rad);
			const float cn = cos(rad + drad);
			const float sn = sin(rad + drad);

			glTexCoord2f(rad / (M_PI * 2), 0.5f + slir / M_PI);
			glNormal3f(cc * cosHere, ss * cosHere, sinHere);
			glVertex3f(radius * cosHere * cc, radius * cosHere * ss,
					radius * sinHere);

			glTexCoord2f((rad + drad) / (M_PI * 2), 0.5f + slir / M_PI);
			glNormal3f(cn * cosHere, sn * cosHere, sinHere);
			glVertex3f(radius * cosHere * cn, radius * cosHere * sn,
					radius * sinHere);

			glTexCoord2f((rad + drad) / (M_PI * 2),
					0.5f + (slir + drad) / M_PI);
			glNormal3f(cn * cosAbove, sn * cosAbove, sinAbove);
			glVertex3f(radius * cosAbove * cn, radius * cosAbove * sn,
					radius * sinAbove);

			glTexCoord2f(rad / (M_PI * 2), 0.5f + (slir + drad) / M_PI);
			glNormal3f(cc * cosAbove, ss * cosAbove, sinAbove);
			glVertex3f(radius * cosAbove * cc, radius * cosAbove * ss,
					radius * sinAbove);
		}
	}
	glEnd();
}

int main(int argc, char ** argv) {

	delta = 0;
	glfwInit();
	glfwWindowHint(GLFW_SAMPLES, 4);
	GLFWwindow *win = glfwCreateWindow(800, 800, "Docking", NULL, NULL);
	glfwMakeContextCurrent(win);
	glfwSetWindowSizeCallback(win, &windowResized);
	windowResized(win, 800, 800);
	Camera cam;

	glEnable(GL_DEPTH_TEST);
	glEnable(GL_LIGHTING);
	glEnable(GL_LIGHT0);
	glEnable(GL_MULTISAMPLE);
	glCullFace(GL_BACK);
	glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

	Mesh sphere = Mesh(fopen("data/sphere.lpack", "r"));
	Texture icePlanet("data/tex/ice_planet.png");
	Texture icePlanetSpec("data/tex/ice_planet.spec.png");

	Ship endurance = Ship("data/endurance.pack");

//	Ship ranger = Ship("data/ranger.pack");
//	ranger.pos = vec3_make(0, 0, 10);

	glfwSetTime(0);
	double prevLoop = 0;
	glewInit();
	while (!glfwWindowShouldClose(win)) {
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		cam.glApply();
		glLightfv(GL_LIGHT0, GL_POSITION, lightDir);
		glLightfv(GL_LIGHT0, GL_DIFFUSE, lightColor);
		glLightModelfv(GL_AMBIENT, ambientColor);

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		endurance.render();
//		ranger.render();

		glPushMatrix();
		glTranslatef(-200, -200, -200);
		glRotatef(glfwGetTime() / 10, 0, 1, 0);
		{
			useProgram(SHADER_PLANET);
			glEnable(GL_TEXTURE_2D);
			glActiveTexture(GL_TEXTURE0);
			icePlanet.bind();
			glActiveTexture(GL_TEXTURE1);
			icePlanetSpec.bind();
			glScalef(300, 300, 300);
			sphere.render();
			nouseProgram();
			glDisable(GL_LIGHTING);
			glEnable(GL_BLEND);
			useProgram(SHADER_ATM);
			glScalef(325.0 / 300.0, 325.0 / 300.0, 325.0 / 300.0);
			sphere.render();
			glDisable(GL_BLEND);
			glEnable(GL_LIGHTING);
		}
		glPopMatrix();
		glFlush();
		glfwPollEvents();
		cam.process(win);
		glfwSwapBuffers(win);

		delta = (glfwGetTime() - prevLoop);
		prevLoop = glfwGetTime();

		endurance.zeroThrusters();
		for (uint32_t c = 0; c < 12; c++) {
			if (glfwGetKey(win, groupCTL[c])) {
				endurance.addGroup(c, 0.05f);
			}
		}
		if (glfwGetKey(win, GLFW_KEY_SPACE)) {
			endurance.addWorldThrust(vec3_make(0.5f, 0, 0));
		} else if (glfwGetKey(win, GLFW_KEY_RIGHT_ALT)) {
			endurance.addWorldThrust(vec3_make(-0.5f, 0, 0));
		}

		endurance.update();
//		ranger.update();
	}

	glfwDestroyWindow(win);
	glfwTerminate();
	return 0;
}

double getDelta() {
	return delta;
}
