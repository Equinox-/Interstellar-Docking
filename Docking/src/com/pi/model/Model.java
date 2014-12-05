package com.pi.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.pi.math.Matrix4;
import com.pi.math.Vector3;
import com.pi.util.LEInputStream;

public class Model {
	Texture[] textureTable;
	Material[] materialTable;
	Node[] nodeTable;
	Mesh[] meshTable;

	Node root;

	public Model(final String fname) throws IOException {
		final File file = new File(fname);
		final File parent = file.getParentFile();

		LEInputStream in = new LEInputStream(new FileInputStream(file));

		textureTable = new Texture[in.readIntLE()];
		for (int i = 0; i < textureTable.length; i++) {
			char[] raw = new char[in.readIntLE()];
			for (int j = 0; j < raw.length; j++)
				raw[j] = (char) in.read();

			textureTable[i] = new Texture(new File(parent, "tex/"
					+ new String(raw)));
		}

		materialTable = new Material[in.readIntLE()];
		for (int i = 0; i < materialTable.length; i++)
			materialTable[i] = new Material(in);

		meshTable = new Mesh[in.readIntLE()];
		for (int i = 0; i < meshTable.length; i++) {
			meshTable[i] = new Mesh(in);
			meshTable[i].updateMaterialRef(materialTable, textureTable);
		}

		nodeTable = new Node[in.readIntLE()];
		for (int i = 0; i < nodeTable.length; i++)
			nodeTable[i] = new Node(in);

		in.close();

		for (int i = 0; i < nodeTable.length; i++)
			nodeTable[i].updateRefs(nodeTable, meshTable);
		int rootCount = 0;
		for (int i = 0; i < nodeTable.length; i++)
			if (nodeTable[i].getParent() == null) {
				rootCount++;
				root = nodeTable[i];
			}
		if (rootCount > 1) {
			System.out.printf("Multiple root node\n");
			System.exit(1);
		}
		root.computePhysics();

		System.out.printf("Model Physics: (%s)\n", fname);
		System.out.printf("Mass: %f\n", root.mass);
		System.out
				.printf("COM: %f %f %f\n", root.com.x, root.com.y, root.com.z);
		System.out.printf("I:\t%.5f %.5f %.5f\n",
				root.inertiaTensor.data.get(0), root.inertiaTensor.data.get(4),
				root.inertiaTensor.data.get(8));
		System.out.printf("\t%.5f %.5f %.5f\n", root.inertiaTensor.data.get(1),
				root.inertiaTensor.data.get(5), root.inertiaTensor.data.get(9));
		System.out
				.printf("\t%.5f %.5f %.5f\n", root.inertiaTensor.data.get(2),
						root.inertiaTensor.data.get(6),
						root.inertiaTensor.data.get(10));
	}

	public void render() {
		root.render();
	}

	public final Vector3 getCOM() {
		return root.com;
	}

	public final Matrix4 getInertiaTensor() {
		return root.inertiaTensor;
	}

	public final Matrix4 getInertiaTensorInverse() {
		return root.inertiaInverse;
	}

	public final float getMass() {
		return root.mass;
	}
}
