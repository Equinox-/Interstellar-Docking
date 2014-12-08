#version 120

varying vec4 varyingVertex;

uniform sampler2D color;
uniform sampler2D depth;
uniform vec3 blackHole;
uniform vec4 projParams;
uniform vec2 screenSize;
uniform float powerRadius;
uniform float powerStrength;

float affectMultipler(vec2 frag) {
	float bHoleDepth = blackHole.z;
	float dv = texture2D(depth, frag / screenSize).x + .5;
	float scale = (dv - bHoleDepth);
	return (scale < 0 ? 0 : (scale > 1 ? 1 : scale));
}

void main() {
	vec2 pos = gl_TexCoord[0].st * screenSize;
	float bHoleDV = blackHole.z;
	float bHoleDepth = projParams.w / (blackHole.z + projParams.z);
	float dv = texture2D(depth, gl_TexCoord[0].st).x + .5;
	float cDepth = projParams.w / ((2 * dv - 1) + projParams.z);

	vec2 blackHoleScreen = (blackHole.xy + 1) * (screenSize / 2);

	vec2 balanced = (gl_FragCoord.xy - blackHoleScreen) * bHoleDepth;
	vec2 normalized = normalize(balanced);

	float scaled = length(balanced) / (powerRadius * 10 * -projParams.w);
	float strength = 1 / (scaled * scaled);
	vec3 rayDirection = vec3(0, 0, 1);
	vec3 surfaceNormal = normalize(vec3(normalized, 1.0 / strength));
	vec3 newBeam = refract(rayDirection, surfaceNormal, 2.6);
	vec2 newPos = pos
			+ vec2(newBeam.x, newBeam.y) * powerRadius * powerStrength
					* affectMultipler(pos);

	gl_FragColor = texture2D(color, newPos / screenSize);
	gl_FragColor *= length(newBeam);
	gl_FragColor.a = 1.0f;
}
