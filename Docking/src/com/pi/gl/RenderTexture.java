package com.pi.gl;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

public class RenderTexture {
	public final int renderWidth, renderHeight;
	private final int colorLayout, colorFormat;
	private final boolean generateDepth;
	private int colorTexture, depthTexture;
	private int fbo;

	public RenderTexture(int renderWidth, int renderHeight) {
		this(renderWidth, renderHeight, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE,
				true);
	}

	public RenderTexture(int renderWidth, int renderHeight, int colorLayout,
			int colorFormat, final boolean generateDepth) {
		this.renderWidth = renderWidth;
		this.renderHeight = renderHeight;
		this.colorLayout = colorLayout;
		this.colorFormat = colorFormat;
		this.generateDepth = generateDepth;
	}

	public void generate() {
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		// Render Texture
		colorTexture = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTexture);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
				GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
				GL11.GL_LINEAR);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
				GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
				GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, colorLayout, renderWidth,
				renderHeight, 0, colorLayout, colorFormat, (ByteBuffer) null);

		// Depth Texture
		if (generateDepth) {
			depthTexture = GL11.glGenTextures();
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexture);

			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
					GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
					GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
					GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
					GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
					GL14.GL_DEPTH_TEXTURE_MODE, GL11.GL_INTENSITY);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
					GL14.GL_TEXTURE_COMPARE_MODE, GL14.GL_COMPARE_R_TO_TEXTURE);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
					GL14.GL_TEXTURE_COMPARE_FUNC, GL11.GL_LEQUAL);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_DEPTH_COMPONENT,
					renderWidth, renderHeight, 0, GL11.GL_DEPTH_COMPONENT,
					GL11.GL_FLOAT, (ByteBuffer) null);
		}

		fbo = GL30.glGenFramebuffers();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER,
				GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, colorTexture, 0);
		if (generateDepth)
			GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER,
					GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, depthTexture,
					0);
	}

	public void bindRender() {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
		GL11.glViewport(0, 0, renderWidth, renderHeight);
	}

	public int getColorTexture() {
		return colorTexture;
	}

	public int getDepthTexture() {
		return depthTexture;
	}
}
