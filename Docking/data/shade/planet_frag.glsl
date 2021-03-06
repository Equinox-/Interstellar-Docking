#version 120
#define LIGHT_SOURCES 1

varying vec3 varyingNormal;
varying vec4 varyingVertex;

uniform sampler2D diffuseMap;
uniform sampler2D specularMap;

float surface3(vec3 v) {
	return cos(dot(sin(v) + tan(v), v));
}

void main() {
	vec3 vertexPosition = varyingVertex.xyz;
	vec3 surfaceNormal = normalize(varyingNormal);

	vec3 atmVec = cross(vertexPosition, surfaceNormal);
	float atmPassed = length(atmVec);

	vec2 noiseVector = pow(atmPassed, 0.25) * 1E-6
			* vec2(surface3(vertexPosition / 2), surface3(-vertexPosition / 2));

	vec4 diffuseColor = texture2D(diffuseMap, gl_TexCoord[0].st + noiseVector);
	vec4 specularColor = texture2D(specularMap,
			gl_TexCoord[0].st + noiseVector);

	gl_FragColor.rgb = vec3(0, 0, 0);
	gl_FragColor += gl_LightModel.ambient * diffuseColor;
	for (int i = 0; i < LIGHT_SOURCES; i++) {
		vec3 lightDirection = normalize(
				gl_LightSource[i].position.xyz - vertexPosition);
		float diffuseLightIntensity = max(0,
				dot(surfaceNormal, lightDirection));
		gl_FragColor.rgb += diffuseLightIntensity * diffuseColor.rgb;
		vec3 reflectionDirection = normalize(
				reflect(-lightDirection, surfaceNormal));
		float specular = max(0.0, dot(surfaceNormal, reflectionDirection));
		if (diffuseLightIntensity != 0) {
			float fspecular = pow(specular, gl_FrontMaterial.shininess)
					* specularColor.r;
			gl_FragColor.rgb += specularColor.r * fspecular
					* gl_LightSource[i].specular.rgb;
		}
	}
}
