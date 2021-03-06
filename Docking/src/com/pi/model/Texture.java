package com.pi.model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import sun.awt.image.ByteInterleavedRaster;

public class Texture {
	private static final ColorModel glAlphaColorModel;
	private static final ColorModel glColorModel;
	static {
		glAlphaColorModel = new ComponentColorModel(
				ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] { 8, 8,
						8, 8 }, true, false, ComponentColorModel.TRANSLUCENT,
				DataBuffer.TYPE_BYTE);

		glColorModel = new ComponentColorModel(
				ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] { 8, 8,
						8, 0 }, false, false, ComponentColorModel.OPAQUE,
				DataBuffer.TYPE_BYTE);
	}

	private BufferedImage loaded;
	public final int width, height;

	private int textureID;

	public Texture(File file) throws IOException {
		loaded = ImageIO.read(file);
		width = loaded.getWidth();
		height = loaded.getHeight();
		textureID = 0;
	}

	public void freeTexture() {
		if (textureID > 0) {
			GL11.glDeleteTextures(textureID);
			textureID = 0;
		}
	}

	private ByteBuffer convertImageData(BufferedImage bufferedImage) {
		ByteBuffer imageBuffer = null;
		WritableRaster raster;
		BufferedImage texImage;
		// create a raster that can be used by OpenGL as a source
		// for a texture

		if (bufferedImage.getColorModel().hasAlpha()) {
			raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
					width, height, 4, null);
			texImage = new BufferedImage(glAlphaColorModel, raster, false, null);
		} else {
			raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
					width, height, 3, null);
			texImage = new BufferedImage(glColorModel, raster, false, null);
		}

		// copy the source image into the produced image
		Graphics g = texImage.getGraphics();
		g.setColor(new Color(0f, 0f, 0f, 0f));
		g.fillRect(0, 0, width, height);
		g.drawImage(bufferedImage, 0, 0, null);

		// build a byte buffer from the temporary image

		// that be used by OpenGL to produce a texture.

		byte[] data = ((DataBufferByte) texImage.getRaster().getDataBuffer())
				.getData();

		imageBuffer = ByteBuffer.allocateDirect(data.length);
		imageBuffer.order(ByteOrder.nativeOrder());
		imageBuffer.put(data, 0, data.length);
		imageBuffer.flip();

		return imageBuffer;
	}

	public void loadToVRAM() {
		freeTexture();

		textureID = GL11.glGenTextures();

		bind();

		// Hard coded loading optimizations
		if (loaded.getColorModel().getColorSpace().isCS_sRGB()
				&& loaded.getColorModel().getClass()
						.equals(ComponentColorModel.class)
				&& loaded.getRaster().getClass()
						.equals(ByteInterleavedRaster.class)
				&& (loaded.getColorModel().getPixelSize() == 24 || loaded
						.getColorModel().getPixelSize() == 32)) {
			int srcType = loaded.getColorModel().getPixelSize() == 24 ? GL11.GL_RGB
					: GL11.GL_RGBA;
			ByteInterleavedRaster b = (ByteInterleavedRaster) loaded
					.getRaster();

			int[] offsets = b.getDataOffsets();
			GL11.glMatrixMode(GL11.GL_COLOR);
			FloatBuffer clM = BufferUtils.createFloatBuffer(16);
			clM.limit(16);
			clM.put(0 + (4 * offsets[0]), 1);
			clM.put(1 + (4 * offsets[1]), 1);
			clM.put(2 + (4 * offsets[2]), 1);
			if (offsets.length == 4)
				clM.put(3 + (4 * offsets[3]), 1);
			GL11.glLoadMatrix(clM);
			byte[] data = ((DataBufferByte) loaded.getRaster().getDataBuffer())
					.getData();
			ByteBuffer imageBuffer = ByteBuffer.allocateDirect(data.length);
			imageBuffer.order(ByteOrder.nativeOrder());
			imageBuffer.put(data, 0, data.length);
			imageBuffer.flip();

			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, srcType, width, height, 0,
					srcType, GL11.GL_UNSIGNED_BYTE, imageBuffer);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
		} else {
			int srcPixelFormat = loaded.getColorModel().hasAlpha() ? GL11.GL_RGBA
					: GL11.GL_RGB;

			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, srcPixelFormat, width,
					height, 0, srcPixelFormat, GL11.GL_UNSIGNED_BYTE,
					convertImageData(loaded));
		}

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
				GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
				GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
				GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
				GL11.GL_REPEAT);
	}

	public void bind() {
		if (textureID <= 0) {
			loadToVRAM();
		}
		if (textureID > 0) {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
		}
	}
}
