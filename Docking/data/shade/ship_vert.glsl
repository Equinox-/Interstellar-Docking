#version 120

attribute vec3 tangent;
attribute vec3 binormal;

varying vec3 varyingNormal;
varying vec3 varyingTangent;
varying vec3 varyingBinormal;
varying vec4 varyingVertex;

void main() {
	gl_TexCoord[0] = gl_MultiTexCoord0;
	varyingNormal = gl_NormalMatrix * gl_Normal;
	varyingTangent = gl_NormalMatrix * tangent;
	varyingBinormal = gl_NormalMatrix * binormal;
	varyingVertex = gl_ModelViewMatrix * gl_Vertex;
	gl_Position = ftransform();
}

