package com.pi.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class LEInputStream extends InputStream {
	private final InputStream in;

	public LEInputStream(InputStream in) {
		this.in = in;
	}

	@Override
	public int read() throws IOException {
		return in.read();
	}

	private final byte[] intBuffer = new byte[4];

	public int readIntLE() throws IOException {
		in.read(intBuffer, 0, 4);

		return (intBuffer[0] & 0xFF) | ((intBuffer[1] & 0xFF) << 8)
				| ((intBuffer[2] & 0xFF) << 16) | ((intBuffer[3] & 0xFF) << 24);
	}

	public void readIntLE(int[] array, int offset, int count)
			throws IOException {
		final byte[] buffer = new byte[4 * count];
		in.read(buffer, 0, buffer.length);
		for (int i = 0, j = 0; i < count; i++, j += 4)
			array[offset + i] = (buffer[j] & 0xFF)
					| ((buffer[j + 1] & 0xFF) << 8)
					| ((buffer[j + 2] & 0xFF) << 16)
					| ((buffer[j + 3] & 0xFF) << 24);
	}

	public void readIntLE(IntBuffer array, int offset, int count)
			throws IOException {
		final byte[] buffer = new byte[4 * count];
		in.read(buffer, 0, buffer.length);
		for (int i = 0, j = 0; i < count; i++, j += 4)
			array.put(offset + i, (buffer[j] & 0xFF)
					| ((buffer[j + 1] & 0xFF) << 8)
					| ((buffer[j + 2] & 0xFF) << 16)
					| ((buffer[j + 3] & 0xFF) << 24));
	}

	public float readFloatLE() throws IOException {
		return Float.intBitsToFloat(readIntLE());
	}

	public void readFloatLE(float[] array, int offset, int count)
			throws IOException {
		final byte[] buffer = new byte[4 * count];
		in.read(buffer, 0, buffer.length);
		for (int i = 0, j = 0; i < count; i++, j += 4)
			array[offset + i] = Float.intBitsToFloat((buffer[j] & 0xFF)
					| ((buffer[j + 1] & 0xFF) << 8)
					| ((buffer[j + 2] & 0xFF) << 16)
					| ((buffer[j + 3] & 0xFF) << 24));
	}

	public void readFloatLE(FloatBuffer array, int offset, int count)
			throws IOException {
		final byte[] buffer = new byte[4 * count];
		in.read(buffer, 0, buffer.length);
		for (int i = 0, j = 0; i < count; i++, j += 4)
			array.put(
					i + offset,
					Float.intBitsToFloat((buffer[j] & 0xFF)
							| ((buffer[j + 1] & 0xFF) << 8)
							| ((buffer[j + 2] & 0xFF) << 16)
							| ((buffer[j + 3] & 0xFF) << 24)));
	}

	@Override
	public void close() throws IOException {
		in.close();
	}

}
