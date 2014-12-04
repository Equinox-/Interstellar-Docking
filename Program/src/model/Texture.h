/*
 * Texture.h
 *
 *  Created on: Dec 4, 2014
 *      Author: localadmin
 */

#ifndef TEXTURE_H_
#define TEXTURE_H_

#include <stdio.h>

class Texture {
private:
	static const int TEXTURE_LOAD_DEBUGGING = 1;
	static const int TEXTURE_LOAD_SUCCESS = 0;
	static const int TEXTURE_LOAD_ERROR = 1;
	typedef unsigned int texid_t;

	texid_t textureID;
	void *rawData;
	unsigned int width;
	unsigned int height;
	unsigned int pixFMT;
	// Information to access the data
	unsigned int rowBytes;
	unsigned int bitsPerPixel;

	int loadPNG(FILE *fp);
public:
	Texture(const char *fname);
	virtual ~Texture();
	void freeRawData();
	void freeTexture();
	void loadToVRAM();
	void bind();

	inline int getWidth() {
		return width;
	}
	inline int getHeight() {
		return height;
	}
};

#endif /* TEXTURE_H_ */
