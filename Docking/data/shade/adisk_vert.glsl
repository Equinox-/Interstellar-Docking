#version 120
varying vec4 varyingVertex;
varying vec4 varyingColor;

uniform mat4 modelview;
uniform mat4 projection;

void main() {
	varyingVertex = modelview * gl_Vertex;
	varyingColor = gl_Color;
	gl_Position = projection * varyingVertex;
}
