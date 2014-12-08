#version 120

varying vec4 varyingVertex;

uniform sampler2D color;
uniform sampler2D depth;
uniform vec3 blackHole;
uniform vec4 projParams;
uniform vec2 screenSize;
uniform float powerRadius;
uniform float powerStrength;


vec3 reconstructPosition(vec2 frag)
{
	float dv = texture2D(depth, frag / screenSize).x * 2.0f - 1.0f;
    float viewDepth = projParams.w / (dv - projParams.z);

    return vec3((((frag / screenSize) * 2 - 1) * viewDepth) / projParams.xy, viewDepth);
}

void main() {
	if (blackHole.z < 0) {
		gl_FragColor = texture2D(color, gl_TexCoord[0].st);
		gl_FragColor.a = 1;
		return;
	}
	vec2 pos = gl_TexCoord[0].st * screenSize;
	vec3 vertexPosition = reconstructPosition(pos);

	vec2 blackHoleScreen = (blackHole.xy + 1) * (screenSize / 2);

	vec2 balanced = gl_FragCoord.xy - blackHoleScreen;
	vec2 normalized = normalize(balanced);
	float distance = length(balanced);

//	if (1-reconstructPosition(pos).z > 5.5+blackHole.z) {
//		gl_FragColor = texture2D(color, gl_TexCoord[0].st);
//		gl_FragColor.a = 1;
//		return;
//	}
	
	float scaled = length(balanced);
	float strength = powerRadius / (scaled * scaled);
	vec3 rayDirection = vec3(0, 0, 1);
	vec3 surfaceNormal = normalize(vec3(normalized, 1.0 / strength));
	vec3 newBeam = refract(rayDirection, surfaceNormal, 2.6);
	vec2 newPos = pos + vec2(newBeam.x, newBeam.y) * powerStrength;

	gl_FragColor = texture2D(color, newPos / screenSize);
	gl_FragColor *= length(newBeam);
	gl_FragColor.a = 1.0f;
}
