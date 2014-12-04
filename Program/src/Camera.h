#ifndef CAMERA_H_
#define CAMERA_H_
#include "math/matmath.h"


struct GLFWwindow;
class Camera {
private:
	float offset;
	mat4 pose;

	double cursorX, cursorY;

	bool scrollRegistered;

	static void callback(GLFWwindow*,double,double);
	static Camera *active;
public:
	Camera();
	void process(GLFWwindow *win);
	void glApply();

	virtual ~Camera();
};

#endif /* CAMERA_H_ */
