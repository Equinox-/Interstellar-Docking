package com.pi.phys;

import java.io.File;
import java.io.FileInputStream;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import com.pi.Main;
import com.pi.gl.MatrixStack;
import com.pi.gl.Shaders;
import com.pi.math.Vector3;
import com.pi.model.Mesh;
import com.pi.model.Texture;
import com.pi.util.LEInputStream;

public class CelestialBody {
	private float radius;
	private float atmosphereScale;
	private float period;
	private Vector3 place;

	private Texture diffuse, specular;

	private static Mesh sphere;

	public CelestialBody(float radius, float atmosphere, float period,
			Vector3 place, Texture diffuse, Texture specular) {
		if (sphere == null) {
			try {
				LEInputStream in = new LEInputStream(new FileInputStream(
						new File(Main.dataDir, "sphere.lpack")));
				sphere = new Mesh(in);
				in.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		this.radius = radius;
		this.atmosphereScale = atmosphere;
		this.period = period;
		this.place = place;
		this.diffuse = diffuse;
		this.specular = specular;
	}

	public void render() {
		MatrixStack.glPushMatrix();
		MatrixStack.glTranslatef(place.x, place.y, place.z);
		MatrixStack.glRotatef((float) Main.getTime() * 360.0f / period, 0, 1, 0);
		{
			Shaders.PLANET.use();
			
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			diffuse.bind();
			GL13.glActiveTexture(GL13.GL_TEXTURE1);
			specular.bind();
			MatrixStack.glScalef(radius, radius, radius);
			MatrixStack.commit();
			sphere.render();
			if (atmosphereScale > 1) {
				Shaders.noProgram();
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glDisable(GL11.GL_LIGHTING);
				Shaders.ATMOSPHERE.use();
				MatrixStack.glScalef(atmosphereScale, atmosphereScale, atmosphereScale);
				MatrixStack.commit();
				sphere.render();
				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glDisable(GL11.GL_BLEND);
			}
		}
		MatrixStack.glPopMatrix();
	}
}
