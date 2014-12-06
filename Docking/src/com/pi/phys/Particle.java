package com.pi.phys;

import org.lwjgl.opengl.GL11;

import com.pi.Main;
import com.pi.math.Vector3;

public class Particle {
	Vector3 color;
	Vector3 pos;
	Vector3 vel;
	double begin, end;

	public Particle() {
		color = new Vector3();
		pos = new Vector3();
		vel = new Vector3();
		begin = end = 0;
	}

	public void render() {
//		GL11.glColor3f(color.x, color.y, color.z,
//				1.0f - (float) ((Main.getTime() - begin) / (end - begin)));
		// It is always black >.>
		GL11.glVertex3f(pos.x, pos.y, pos.z);
		Vector3.addto(pos, vel, (float) Main.getDelta());
	}
}
