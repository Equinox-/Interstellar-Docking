#include "Camera.h"
#include <GL/gl.h>
#include <stdio.h>
#include <GLFW/glfw3.h>

Camera::Camera() {
	offset = -5.0f;
	pose = mat4_identity();
	cursorX = cursorY = 0;
}

Camera::~Camera() {
}

void Camera::process(GLFWwindow *win) {
	double currX, currY;
	glfwGetCursorPos(win, &currX, &currY);
	const float deltaX = (float) (currX - cursorX);
	const float deltaY = (float) (currY - cursorY);

	if (glfwGetKey(win, GLFW_KEY_F4)
			|| (glfwGetMouseButton(win, GLFW_MOUSE_BUTTON_MIDDLE)
					&& glfwGetKey(win, GLFW_KEY_LEFT_SHIFT))
			|| (glfwGetMouseButton(win, GLFW_MOUSE_BUTTON_RIGHT))) {
		// orbit
		vec3 axis;
		axis.x = deltaY;
		axis.y = deltaX;
		axis.z = 0;
		float mag;
		axis = vec3_normalize(axis, &mag);
		if (mag > 0)
			pose = mat4_multiply(pose, mat4_axis_angle(mag / 180.0f, axis));
	} else if (glfwGetKey(win, GLFW_KEY_F3)) {
		// fine zoom
		offset += deltaY / 25;
	} else if (glfwGetKey(win, GLFW_KEY_F2)
			|| glfwGetMouseButton(win, GLFW_MOUSE_BUTTON_MIDDLE)) {
		// Move
		vec3 trans;
		trans.x = deltaX / 100.0f;
		trans.y = -deltaY / 100.0f;
		trans.z = 0;
		if (deltaY || deltaX)
			pose = mat4_multiply(pose, mat4_translation(trans));
	}
	cursorX = currX;
	cursorY = currY;
}

void Camera::glApply() {
	glTranslatef(0, 0, offset);
	glMultMatrixf(pose.data);
}

