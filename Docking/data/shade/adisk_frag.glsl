#version 120

varying vec4 varyingVertex;
varying vec4 varyingColor;

void main() {
	gl_FragColor = varyingColor;
}