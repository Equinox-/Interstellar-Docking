#include "shaders.h"
#include <stdio.h>
#include <stdlib.h>
#include <GL/glew.h>
#include <string.h>

char *textFileRead(char *fn) {
	printf("Read shader from %s\n", fn);
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

static const char * const shader_names[] = { "ship", "planet", "atm" };

int shaderStorage[2];

void useProgram(int id) {
	if (shaderStorage[id] == 0) {
		char *vs, *fs;

		int v = glCreateShaderObjectARB(GL_VERTEX_SHADER_ARB);
		int f = glCreateShaderObjectARB(GL_FRAGMENT_SHADER_ARB);

		char fileBuff[5 + 6 + strlen(shader_names[id]) + 11];
		fileBuff[0] = 0;
		strcat(fileBuff, "data/shade/");
		strcat(fileBuff, shader_names[id]);
		strcat(fileBuff, "_vert.glsl");
		vs = textFileRead(fileBuff);
		fileBuff[0] = 0;
		strcat(fileBuff, "data/shade/");
		strcat(fileBuff, shader_names[id]);
		strcat(fileBuff, "_frag.glsl");
		fs = textFileRead(fileBuff);

		int state;
		glShaderSourceARB(v, 1, (const GLcharARB**) &vs, NULL);
		glShaderSourceARB(f, 1, (const GLcharARB**) &fs, NULL);

		free(vs);
		free(fs);

		glCompileShaderARB(v);
		glCompileShaderARB(f);

		shaderStorage[id] = glCreateProgramObjectARB();
		glAttachObjectARB(shaderStorage[id], f);
		glAttachObjectARB(shaderStorage[id], v);

		glLinkProgramARB(shaderStorage[id]);
	}
	glUseProgramObjectARB(shaderStorage[id]);
}

void nouseProgram() {
	glUseProgramObjectARB(0);
}
