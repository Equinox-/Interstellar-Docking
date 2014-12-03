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

int main(int argc, char ** argv) {
	delta = 0;
	glfwInit();
	GLFWwindow *win = glfwCreateWindow(800, 800, "Docking", NULL, NULL);
	glfwMakeContextCurrent(win);
	glfwSetWindowSizeCallback(win, &windowResized);
	windowResized(win, 800, 800);
	Camera cam;

	Ship endurance = Ship("data/endurance_wheel.stl");
	//enduranceRANGER.stl

	glEnable(GL_DEPTH_TEST);
	glEnable(GL_COLOR_MATERIAL);
//	glEnable(GL_LIGHTING);
	glEnable(GL_LIGHT0);
	glEnable(GL_BLEND);
	glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

	while (!glfwWindowShouldClose(win)) {
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		cam.glApply();

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		endurance.render();

		glFlush();
		glfwPollEvents();
		cam.process(win);
		glfwSwapBuffers(win);

		delta = glfwGetTime();
		glfwSetTime(0);
		if (glfwGetKey(win, GLFW_KEY_A)) {
			endurance.zeroThrusters();
			endurance.setThruster(0, 1);
			endurance.setThruster(5, 1);
			endurance.setThruster(12, 1);
			endurance.setThruster(9, 1);
		} else if (glfwGetKey(win, GLFW_KEY_D)) {
			endurance.zeroThrusters();
			endurance.setThruster(1, 1);
			endurance.setThruster(4, 1);
			endurance.setThruster(13, 1);
			endurance.setThruster(8, 1);
		} else {
			endurance.zeroThrusters();
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
