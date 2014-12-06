#version 120
uniform sampler2D diffuseTexture;
uniform sampler2D specularTexture;
uniform sampler2D normalTexture;

varying vec3 vertPos;
varying vec3 lightVec;
varying vec3 halfVec;
varying vec3 eyeVec;

float rand(vec2 co) {
	return fract(sin(dot(co.xy, vec2(12.9898, 78.233))) * 43754.53);
}

void main() {
	vec3 normal = texture2D(normalTexture, gl_TexCoord[0].st).rgb;
	normal.x = normal.x * 2 - 1;
	normal.y = normal.y * 2 - 1;
	normal = normalize(normal);

	vec4 specularMaterial = texture2D(specularTexture, gl_TexCoord[0].st);
	vec4 specularLight;
	float shininess;
	
	float lamberFactor = max(dot(lightVec, normal), 0.0);
	vec4 diffuseMaterial = texture2D(diffuseTexture, gl_TexCoord[0].st)
			* (1 - pow(rand(gl_TexCoord[0].st), 2) * .25);
	vec4 diffuseLight = vec4(0.0);

	gl_FragColor = gl_LightModel.ambient * diffuseMaterial;

	if (lamberFactor > 0.0) {
		diffuseLight = gl_LightSource[0].diffuse;

		specularLight = gl_LightSource[0].specular;
		shininess = pow(max(dot(halfVec, normal), 0.0), 2.0);

		gl_FragColor += diffuseMaterial * diffuseLight * lamberFactor;
//		gl_FragColor += specularMaterial * specularLight * shininess;
	}
}
