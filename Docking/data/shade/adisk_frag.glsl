#version 120

varying vec4 varyingVertex;
varying vec4 varyingColor;

void main() {
//	float density = varyingColor.a;
//	gl_FragColor.rgb = mix(varyingColor.rgb, vec3(1, 1, 1), density);
//	gl_FragColor.a = density;
	gl_FragColor = varyingColor;
}
