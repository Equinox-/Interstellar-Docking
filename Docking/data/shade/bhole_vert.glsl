#version 120
varying vec4 varyingVertex;

uniform mat4 modelview;
uniform mat4 projection;

void main() {
	gl_TexCoord[0] = gl_MultiTexCoord0;
	varyingVertex = modelview * gl_Vertex;
	gl_Position = projection * varyingVertex;
}
