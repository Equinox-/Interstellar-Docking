#version 120

attribute vec3 tangent;
attribute vec3 binormal;

varying vec3 varyingNormal;
varying vec3 varyingTangent;
varying vec3 varyingBinormal;
varying vec4 varyingVertex;

uniform mat4 projection, modelview;
uniform mat4 nrmMatrix;

void main() {
	mat3 normalMatrix = mat3(nrmMatrix);
	
	gl_TexCoord[0] = gl_MultiTexCoord0;
	varyingNormal = normalMatrix * gl_Normal;
	varyingTangent = normalMatrix * tangent;
	varyingBinormal = normalMatrix * binormal;
	varyingVertex = modelview * gl_Vertex;
	gl_Position = projection * varyingVertex;
}

