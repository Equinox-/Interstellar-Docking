#version 120

varying vec4 varyingVertex;

uniform sampler2D color;
uniform sampler2D depth;
uniform vec3 blackHole;
uniform vec4 projParams;
uniform vec2 screenSize;
uniform float powerRadius;

void main() {
	if (0) {
		gl_FragColor = texture2D(color, gl_TexCoord[0].st);
		return;
	}
	vec2 pos = gl_TexCoord[0].st * screenSize;
	float bHoleDV = blackHole.z;
	float bHoleDepth = projParams.w / ((blackHole.z * 2 - 1) + projParams.z);
	float dv = texture2D(depth, gl_TexCoord[0].st).x + .5;
	float realDepth = projParams.w / ((dv * 2 - 1) + projParams.z);

	float screenRadius = (powerRadius * 10 * -projParams.w);

	vec2 blackHoleScreen = (blackHole.xy + 1) * (screenSize / 2);

	vec2 balanced = (gl_FragCoord.xy - blackHoleScreen) * bHoleDepth;
	vec2 normalized = normalize(balanced);

	gl_FragColor = vec4(0, 0, 0, 1);

	float scaled = length(balanced) / screenRadius;
	float strength = 0.4 / (scaled * scaled);
	vec3 rayDirection = vec3(0, 0, 1);
	vec3 surfaceNormal = normalize(vec3(normalized, 1.0 / strength));
	vec3 newBeam = refract(rayDirection, surfaceNormal, 2.6);

	// Something dealing with things in front of the blackhole.
	if (bHoleDV > dv) {
		gl_FragColor = texture2D(color, gl_TexCoord[0].st)
				* min(1,
						pow((bHoleDV - dv), 2) * 0.1
								* length(
										vec3(balanced,
												screenRadius
														* (realDepth
																- bHoleDepth)))
								/ screenRadius);
	}
	vec2 newPos = pos + vec2(newBeam.x, newBeam.y) * powerRadius * 100;

	gl_FragColor += texture2D(color, newPos / screenSize) * length(newBeam);
	gl_FragColor.a = 1.0f;
}
