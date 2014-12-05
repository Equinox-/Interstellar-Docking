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

public class Parser {
	static File content = new File("/home/localadmin/Downloads/endurance-rip/");
	static JSONObject root, buffers, materials, meshes, nodes, images;
	static DataOutputStream dout;

	static Map<String, byte[]> bufferData = new HashMap<String, byte[]>();
	static Map<String, Integer> materialMapping = new HashMap<String, Integer>();
	static Map<String, Integer> meshMapping = new HashMap<String, Integer>();
	static Map<String, Integer> imageMapping = new HashMap<String, Integer>();

	private static byte[] getBuffer(String s) throws Exception {
		byte[] data = bufferData.get(s);
		if (data == null) {
			JSONObject bspec = buffers.getJSONObject(s);
			data = new byte[bspec.getInt("byteLength")];
			FileInputStream fin = new FileInputStream(new File(content,
					bspec.getString("path")));
			fin.read(data, 0, data.length);
			fin.close();
			bufferData.put(s, data);
		}
		return data;
	}

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
		root = new JSONObject(new JSONTokener(new FileReader(new File(
				"../Program/data/endurance_png_medium_with_landers.json"))));
		dout = new DataOutputStream(new FileOutputStream(
				"../Program/data/endurance.pack"));
		buffers = root.getJSONObject("buffers");
		images = root.getJSONObject("images");
		materials = root.getJSONObject("materials");
		meshes = root.getJSONObject("meshes");
		nodes = root.getJSONObject("nodes");
		writeImages();
		writeMaterials();
		writeMeshes();
		writeNodes();
		dout.close();
	}

	private static void writeImages() throws IOException {
		writeIntLE(images.length());
		int i = 1;
		for (String key : images.keySet()) {
			imageMapping.put(key, i++);
			// write string
			String path = images.getJSONObject(key).getString("path");
			String name = path.contains("/") ? path.substring(
					path.lastIndexOf('/') + 1, path.length()) : path;
			writeIntLE(name.length());
			for (int j = 0; j < name.length(); j++)
				dout.writeByte(name.charAt(j) & 0xFF);
		}
	}

	private static void writeColor(JSONArray a, float opac) throws IOException {
		writeFloatLE((float) a.getDouble(0));
		writeFloatLE((float) a.getDouble(1));
		writeFloatLE((float) a.getDouble(2));
		if (a.length() >= 4)
			writeFloatLE((float) a.getDouble(3) * opac);
		else
			writeFloatLE(opac);
	}

	private static int filterInt(String s) {
		if (s.equals("LINEAR"))
			return 0;
		else
			throw new RuntimeException();
	}

	private static int wrapInt(String s) {
		if (s.equals("REPEAT"))
			return 0;
		else
			throw new RuntimeException();
	}

	private static void writeSourceData(JSONObject o, String field, float opac)
			throws IOException {
		if (o.has(field + "Texture")) {
			JSONObject t = o.getJSONObject(field + "Texture");
			writeIntLE(imageMapping.get(t.getString("image")));
			writeFloatLE(filterInt(t.getString("magFilter")));
			writeFloatLE(filterInt(t.getString("minFilter")));
			writeFloatLE(wrapInt(t.getString("wrapS")));
			writeFloatLE(wrapInt(t.getString("wrapT")));
		} else if (o.has(field + "Color")) {
			writeIntLE(0);
			writeColor(o.getJSONArray(field + "Color"), opac);
		}
	}

	private static void writeMaterials() throws Exception {
		writeIntLE(materials.length());
		int i = 1;
		for (String key : materials.keySet()) {
			materialMapping.put(key, i++);
			JSONObject matl = materials.getJSONObject(key);
			JSONObject lmb = matl.getJSONObject("techniques")
					.getJSONObject("lambert1").getJSONObject("parameters");

			float opacity = lmb.has("opacityColor") ? (float) lmb.getJSONArray(
					"opacityColor").getDouble(0) : 1;
			writeSourceData(lmb, "ambient", opacity);
			writeSourceData(lmb, "diffuse", opacity);
			writeSourceData(lmb, "emission", opacity);
			writeIntLE(lmb.getInt("indexOfRefraction"));
			writeSourceData(lmb, "reflective", opacity);
			writeFloatLE((float) lmb.getDouble("reflectivity"));
			writeFloatLE((float) lmb.getDouble("shininess"));
			writeSourceData(lmb, "specular", opacity);
		}
	}

	private static void writeMeshes() throws Exception {
		writeIntLE(meshes.length());
		int i = 0;
		for (String key : meshes.keySet()) {
			meshMapping.put(key, i++);
			writeMesh(meshes.getJSONObject(key));
		}
	}

	private static void writeMesh(JSONObject jsonObject) throws Exception {
		JSONObject primitives = jsonObject.getJSONArray("primitives")
				.getJSONObject(0);
		// Write Vertex Data
		{
			JSONObject accessors = jsonObject.getJSONObject("accessors");
			JSONObject[] accessParams = new JSONObject[3];
			// vertex, normal, texture

			JSONArray attrs = primitives.getJSONArray("vertexAttributes");
			Map<String, Integer> bufferStride = new HashMap<String, Integer>();
			for (int i = 0; i < attrs.length(); i++) {
				JSONObject obj = attrs.getJSONObject(i);
				String style = obj.getString("semantic");
				JSONObject access = accessors.getJSONObject(obj
						.getString("accessor"));
				Integer cv = bufferStride.get(access.getString("buffer"));
				if (cv == null)
					cv = 0;
				bufferStride.put(access.getString("buffer"),
						cv + access.getInt("byteStride"));
				if (style.equals("VERTEX"))
					accessParams[2] = access;
				else if (style.equals("NORMAL"))
					accessParams[1] = access;
				else if (style.equals("TEXCOORD"))
					accessParams[0] = access;
			}

			int vtxCount = accessParams[0].getInt("count");
			writeIntLE(vtxCount);

			byte[][] buffer = new byte[accessParams.length][];
			int[] stride = new int[accessParams.length];
			int[] offset = new int[accessParams.length];
			int[] size = new int[accessParams.length];
			for (int i = 0; i < accessParams.length; i++) {
				buffer[i] = getBuffer(accessParams[i].getString("buffer"));
				stride[i] = bufferStride.get(accessParams[i]
						.getString("buffer"));
				offset[i] = accessParams[i].getInt("byteOffset");
				size[i] = accessParams[i].getInt("elementsPerValue")
						* sizeof(accessParams[i].getString("elementType"));
			}

			for (int i = 0; i < vtxCount; i++)
				for (int j = 0; j < accessParams.length; j++)
					dout.write(buffer[j], offset[j] + i * stride[j], size[j]);
		}

		// Write index data
		{
			JSONObject indexSettings = primitives.getJSONObject("indices");
			byte[] indexBuffer = getBuffer(indexSettings.getString("buffer"));
			int offset = indexSettings.getInt("byteOffset");
			int count = indexSettings.getInt("length");
			writeIntLE(count);
			if (!indexSettings.getString("type").equals("Uint32Array"))
				throw new RuntimeException("Invalid type.");
			for (int i = 0; i < count; i++) {
				dout.write(indexBuffer, offset + i * 4, 4);
			}
		}

		// Material data
		{
			if (primitives.has("material"))
				writeIntLE(materialMapping
						.get(primitives.getString("material")));
			else
				writeIntLE(0);
		}
	}

	private static void writeNodes() throws Exception {
		Map<String, Integer> nodeMapping = new HashMap<String, Integer>();
		{
			int i = 0;
			for (String s : nodes.keySet())
				nodeMapping.put(s, i++);
		}
		writeIntLE(nodes.length());
		for (String s : nodes.keySet()) {
			final JSONObject node = nodes.getJSONObject(s);
			final JSONArray children = node.getJSONArray("children");
			writeIntLE(children.length());
			for (int i = 0; i < children.length(); i++)
				writeIntLE(nodeMapping.get(children.getString(i)));
			if (node.has("meshes")) {
				final JSONArray meshes = node.getJSONArray("meshes");
				writeIntLE(meshes.length());
				for (int i = 0; i < meshes.length(); i++)
					writeIntLE(meshMapping.get(meshes.getString(i)));
			} else
				writeIntLE(0);

			if (node.has("matrix")) {
				dout.writeByte(1);
				final JSONArray matrix = node.getJSONArray("matrix");
				if (matrix.length() != 16)
					throw new RuntimeException("Non 16 matrix");
				for (int i = 0; i < 16; i++)
					writeIntLE(Float
							.floatToIntBits((float) matrix.getDouble(i)));
			} else
				dout.writeByte(0);
		}
	}

	private static int sizeof(String t) {
		if (t.equals("Float32"))
			return 4;
		else
			throw new RuntimeException("Failed to get size: " + t);
	}
}
