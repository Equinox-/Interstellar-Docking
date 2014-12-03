#ifndef CAMERA_H_
#define CAMERA_H_
#include "matmath.h"


struct GLFWwindow;
class Camera {
private:
	float offset;
	mat4 pose;

	double cursorX, cursorY;
public:
	Camera();
	void process(GLFWwindow *win);
	void glApply();

	virtual ~Camera();
};

#endif /* CAMERA_H_ */
