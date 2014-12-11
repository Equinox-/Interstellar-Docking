#version 120

varying vec3 varyingNormal;
varying vec4 varyingVertex;

uniform mat4 modelview, projection;
uniform mat4 nrmMatrix;

void main() {
	mat3 normalMatrix = mat3(nrmMatrix);
	
	gl_TexCoord[0] = gl_MultiTexCoord0;
	varyingNormal = normalMatrix * gl_Normal;
	varyingVertex = modelview * gl_Vertex;
	gl_Position = projection * varyingVertex;
}
