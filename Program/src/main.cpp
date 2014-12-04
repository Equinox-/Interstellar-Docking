#include "shaders.h"
#include <GL/gl.h>
#include <GLFW/glfw3.h>
#include <math.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

#include "math/vecmath.h"
#include "Camera.h"
#include "Ship.h"

#include "istime.h"
#include "model/Model.h"

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
const float ambientColor[] = { 0.1f, 0.1f, 0.1f };

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
	glEnable(GL_COLOR_MATERIAL);
//	glEnable(GL_LIGHTING);
	glEnable(GL_LIGHT0);
//	glEnable(GL_BLEND);
	glEnable(GL_MULTISAMPLE);
	glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

	Ship endurance = Ship(
			"/home/localadmin/Downloads/endurance-rip/endurance.pack");

	glfwSetTime(0);
	double prevLoop = 0;
	glewInit();
	useProgram();
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

		glFlush();
		glfwPollEvents();
		cam.process(win);
		glfwSwapBuffers(win);

		delta = (glfwGetTime() - prevLoop) * 1E3;
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
	}

	glfwDestroyWindow(win);
	glfwTerminate();
	return 0;
}

double getDelta() {
	return delta;
}
