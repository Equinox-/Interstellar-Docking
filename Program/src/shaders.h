#ifndef __SHADERS_H
#define __SHADERS_H
#include <stdio.h>
#include <stdlib.h>
#include <GL/glew.h>
#define GL_GLEXT_PROTOTYPES 1

char *textFileRead(char *fn) {
	FILE *fp;
	char *content = NULL;

	int count;
	fp = fopen(fn, "rb");
	fseek(fp, 0, SEEK_END);
	count = ftell(fp);
	fseek(fp, 0L, SEEK_SET);

	content = (char *) malloc(sizeof(char) * (count + 1));
	count = fread(content, sizeof(char), count, fp);
	content[count] = '\0';
	fclose(fp);
	return content;
}

int shaders = 0;

void useProgram() {
	if (shaders == 0) {
		char *vs, *fs;

		int v = glCreateShaderObjectARB(GL_VERTEX_SHADER_ARB);
		int f = glCreateShaderObjectARB(GL_FRAGMENT_SHADER_ARB);

		vs = textFileRead("data/shade_vert.glsl");
		fs = textFileRead("data/shade_frag.glsl");

		int state;
		glShaderSourceARB(v, 1, (const GLcharARB**) &vs, NULL);
		glShaderSourceARB(f, 1, (const GLcharARB**) &fs, NULL);

		free(vs);
		free(fs);

		glCompileShaderARB(v);
		glCompileShaderARB(f);

		shaders = glCreateProgramObjectARB();
		glAttachObjectARB(shaders, f);
		glAttachObjectARB(shaders, v);

		glLinkProgramARB(shaders);
	}
	glUseProgramObjectARB(shaders);
}

#endif
