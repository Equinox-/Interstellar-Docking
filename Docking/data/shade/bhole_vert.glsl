#version 120
varying vec4 varyingVertex;

void main() {
	gl_TexCoord[0] = gl_MultiTexCoord0;
	varyingVertex = gl_Vertex;
	gl_Position = ftransform();
}
