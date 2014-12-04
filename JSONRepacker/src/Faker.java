import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Faker {
	static DataOutputStream dout;

	private static void writeIntLE(int v) throws IOException {
		dout.write(v & 0xFF);
		dout.write(v >> 8 & 0xFF);
		dout.write(v >> 16 & 0xFF);
		dout.write(v >> 24 & 0xFF);
	}

	private static void writeFloatLE(float f) throws IOException {
		writeIntLE(Float.floatToIntBits(f));
	}

	public static void main(String[] args) throws Exception {
		dout = new DataOutputStream(new FileOutputStream("endurance.pack"));
		writeIntLE(1);
		final int base = 4096;
		writeIntLE(base*2);
		for (int i = 0; i < base*2; i++) {
			float th = (i & (base-1)) * 3.14f * 2f / base;
			writeFloatLE(0);
			writeFloatLE(0);

			writeFloatLE(0);
			writeFloatLE(0);
			writeFloatLE(0);

			int rad = i>=base?100:80;
			writeFloatLE(rad * (float) Math.sin(th));
			writeFloatLE(rad * (float) Math.cos(th));
			writeFloatLE(0);
		}
		writeIntLE(base * 6);
		for (int i = 0; i < base; i++) {
			int next = i + 1;
			if (next >= base)
				next = 0;
			writeIntLE(i);
			writeIntLE(next);
			writeIntLE(next | base);

			writeIntLE(i);
			writeIntLE(i | base);
			writeIntLE(next | base);
		}

		writeIntLE(1);
		writeIntLE(0);
		writeIntLE(1);
		writeIntLE(0);
		dout.writeByte(0);
		dout.close();
	}

}
