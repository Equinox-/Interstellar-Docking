#include <GL/gl.h>
#include <GLFW/glfw3.h>
#include <math.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

#include "vecmath.h"
#include "Camera.h"
#include "Ship.h"

#include "istime.h"

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

int main(int argc, char ** argv) {

	delta = 0;
	glfwInit();
	GLFWwindow *win = glfwCreateWindow(800, 800, "Docking", NULL, NULL);
	glfwMakeContextCurrent(win);
	glfwSetWindowSizeCallback(win, &windowResized);
	windowResized(win, 800, 800);
	Camera cam;

	Ship endurance = Ship("data/endurance_wheel.stl");
//	Ship ranger = Ship("data/endurance_ranger.stl");
//	ranger.pose(vec3_make(0, 0, -3.0f), vec3_make(-M_PI / 2.0f, 0, 0));

	glEnable(GL_DEPTH_TEST);
	glEnable(GL_COLOR_MATERIAL);
//	glEnable(GL_LIGHTING);
	glEnable(GL_LIGHT0);
	glEnable(GL_BLEND);
	glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

	glfwSetTime(0);
	double prevLoop = 0;

	while (!glfwWindowShouldClose(win)) {
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		cam.glApply();

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		endurance.render();
//		ranger.render();

		glFlush();
		glfwPollEvents();
		cam.process(win);
		glfwSwapBuffers(win);

		delta = glfwGetTime() - prevLoop;
		prevLoop = glfwGetTime();

		endurance.zeroThrusters();
		for (uint32_t c = 0; c < 12; c++) {
			if (glfwGetKey(win, groupCTL[c])) {
				endurance.setGroup(c, 1);
			}
		}
		if (glfwGetKey(win, GLFW_KEY_SPACE)) {
			endurance.worldThrust(vec3_make(1, 0, 0));
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
