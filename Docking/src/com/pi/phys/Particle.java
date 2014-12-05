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
		end = 0;
	}

	public void render() {
		// glColor4f(p.color.x, p.color.y, p.color.z,
		// 1.0f - (glfwGetTime() - p.begin) / (p.end - p.begin));
		GL11.glColor3f(1, 1, 1);
		GL11.glVertex3f(pos.x, pos.y, pos.z);
		Vector3.addto(pos, vel, (float) Main.getDelta());
	}
}
