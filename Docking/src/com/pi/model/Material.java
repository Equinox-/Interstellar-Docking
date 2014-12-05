package com.pi.model;

import java.io.IOException;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.pi.util.LEInputStream;

public class Material {
	public SourceData ambient;
	public SourceData diffuse;
	public SourceData emission;
	public int indexOfRefraction;
	public SourceData reflective;
	public float reflectivity;
	public float shininess;
	public SourceData specular;

	public Material(LEInputStream in) throws IOException {
		ambient = new SourceData(in);
		diffuse = new SourceData(in);
		emission = new SourceData(in);
		indexOfRefraction = in.readIntLE();
		reflective = new SourceData(in);
		reflectivity = in.readFloatLE();
		shininess = in.readFloatLE();
		specular = new SourceData(in);
	}

	public static class SourceData {
		int imageID;
		final FloatBuffer args = BufferUtils.createFloatBuffer(4);

		public SourceData(LEInputStream in) throws IOException {
			imageID = in.readIntLE();
			args.put(0, in.readFloatLE());
			args.put(1, in.readFloatLE());
			args.put(2, in.readFloatLE());
			args.put(3, in.readFloatLE());
		}
	}

	public void glBindMaterial() {
		if (ambient.imageID == 0) {
			GL11.glMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT,
					ambient.args);
		}
		if (diffuse.imageID == 0) {
			GL11.glMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_DIFFUSE,
					diffuse.args);
		}
		if (emission.imageID == 0) {
			GL11.glMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_EMISSION,
					diffuse.args);
		}
		GL11.glMaterialf(GL11.GL_FRONT_AND_BACK, GL11.GL_SHININESS, shininess);
	}
}
