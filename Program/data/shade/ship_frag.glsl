#version 120
#define LIGHT_SOURCES 1

varying vec4 varyingColour;
varying vec3 varyingNormal;
varying vec4 varyingVertex;

uniform sampler2D diffuseMap;
uniform sampler2D specularMap;
uniform sampler2D normalMap;

void main() {
	vec4 diffuseColor = texture2D(diffuseMap, gl_TexCoord[0].st);
	vec4 specularColor = texture2D(specularMap, gl_TexCoord[0].st);
	vec4 bumpColor = texture2D(normalMap, gl_TexCoord[0].st);
	
	vec3 vertexPosition = (gl_ModelViewMatrix * varyingVertex).xyz;
	vec3 surfaceNormal = normalize((gl_NormalMatrix * varyingNormal).xyz);
	gl_FragColor.rgb = vec3(0,0,0);
	gl_FragColor += gl_LightModel.ambient * diffuseColor;
	for (int i = 0; i < LIGHT_SOURCES; i++) {
		vec3 lightDirection = normalize(gl_LightSource[i].position.xyz - vertexPosition);
		float diffuseLightIntensity = max(0, dot(surfaceNormal, lightDirection));
		gl_FragColor.rgb += diffuseLightIntensity * diffuseColor.rgb;
		vec3 reflectionDirection = normalize(reflect(-lightDirection, surfaceNormal));
		float specular = max(0.0, dot(surfaceNormal, reflectionDirection));
		if (diffuseLightIntensity != 0) {
			float fspecular = pow(specular, gl_FrontMaterial.shininess) * specularColor.a;
			gl_FragColor.rgb += fspecular * specularColor.rgb;
		}
	}
}