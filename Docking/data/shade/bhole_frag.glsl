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
	float bHoleDepth = projParams.w / (blackHole.z + projParams.z);
	float dv = texture2D(depth, frag / screenSize).x;
	float cDepth = projParams.w / ((2 * dv - 1) + projParams.z);
	float scale = (cDepth - bHoleDepth) / 100;
	return (scale < 0 ? 0 : (scale > 1 ? 1 : scale));
}

void main() {
//	if (blackHole.z > 0) {
//		gl_FragColor = texture2D(color, gl_TexCoord[0].st);
//		gl_FragColor.a = 1;
//		return;
//	}
	vec2 pos = gl_TexCoord[0].st * screenSize;
	//	(float) (0.5 * (proj.data.get(10) * f[2] + proj.data.get(14))
	// 	/ -f[2] + 0.5);
	float bHoleDV = 0.5 * blackHole.z + 0.5;
	float bHoleDepth = projParams.w / (blackHole.z + projParams.z);
	float dv = texture2D(depth, pos / screenSize).x;
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
	
	if (dv < bHoleDV) {
		gl_FragColor = vec4(0);
		return;
	}

	gl_FragColor = texture2D(color, newPos / screenSize);
	gl_FragColor *= length(newBeam);
//	gl_FragColor = vec4(affectMultipler(pos));
	gl_FragColor.a = 1.0f;
}
