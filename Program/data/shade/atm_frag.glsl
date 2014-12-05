#version 120
#define LIGHT_SOURCES 1

varying vec4 varyingColour;
varying vec3 varyingNormal;
varying vec4 varyingVertex;

void main() {
	vec3 vertexPosition = (gl_ModelViewMatrix * varyingVertex).xyz;
	vec3 surfaceNormal = normalize((gl_NormalMatrix * varyingNormal).xyz);
	if (dot(surfaceNormal, vertexPosition) > 0) {
		gl_FragColor = vec4(0,0,0,0);
		return;
    }
	float atmPassed = length(cross(normalize(vertexPosition), surfaceNormal));
	
	const float edge  = 0.9;
	float flare = 0;
	if (atmPassed > edge) {
		flare = (atmPassed - edge) / (1.0f-edge);
		flare *= flare;
		atmPassed = edge - flare * edge;
	} else {
		atmPassed *= atmPassed;
	}
	flare *= flare;
	flare /= 2.0f;
	gl_FragColor = vec4(0.2+ flare, 0.2 + flare, 0.4+flare, 0.8) * atmPassed;
}