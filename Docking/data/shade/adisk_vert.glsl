#version 120
varying vec4 varyingVertex;
varying vec4 varyingColor;

void main() {
	varyingVertex = gl_Vertex;
	varyingColor = gl_Color;
	gl_Position = ftransform();
}
