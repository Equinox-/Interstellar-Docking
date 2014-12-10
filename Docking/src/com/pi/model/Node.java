package com.pi.model;

import java.io.IOException;

import com.pi.gl.MatrixStack;
import com.pi.math.Matrix4;
import com.pi.math.Vector3;
import com.pi.util.LEInputStream;

public class Node {
	Matrix4 trans;
	Matrix4 inverse;

	int meshes[];
	Mesh meshRef[];

	int children[];
	Node childRef[];

	Node parentRef;

	Vector3 com;
	Matrix4 inertiaTensor, inertiaInverse;
	float mass;

	public Node(LEInputStream in) throws IOException {
		children = new int[in.readIntLE()];
		childRef = new Node[children.length];
		in.readIntLE(children, 0, children.length);

		meshes = new int[in.readIntLE()];
		meshRef = new Mesh[meshes.length];
		in.readIntLE(meshes, 0, meshes.length);

		if (in.read() != 0) {
			trans = new Matrix4();
			in.readFloatLE(trans.data, 0, 16);
		} else {
			trans = Matrix4.identity();
		}
		inverse = Matrix4.invert(trans);

		parentRef = null;

		mass = 0;
		com = new Vector3();
	}

	public final Node getParent() {
		return parentRef;
	}

	public final float getMass() {
		return mass;
	}

	public final Vector3 getCOM() {
		return com;
	}

	public void render() {
		MatrixStack.glPushMatrix();
		MatrixStack.glMultMatrix(trans);
		MatrixStack.commitModelview();
		for (Mesh m : meshRef)
			m.render();
		for (Node n : childRef)
			n.render();
		MatrixStack.glPopMatrix();
	}

	void updateRefs(Node[] nodeTable, Mesh[] meshTable) {
		for (int i = 0; i < children.length; i++) {
			childRef[i] = nodeTable[children[i]];
			nodeTable[children[i]].parentRef = this;
		}

		for (int i = 0; i < meshes.length; i++)
			meshRef[i] = meshTable[meshes[i]];
	}

	void computePhysics() {
		mass = 0;
		com.x = com.y = com.z;
		for (int i = 0; i < meshes.length; i++) {
			mass += meshRef[i].mass;
			Vector3.addto(com, meshRef[i].com, meshRef[i].mass);
		}

		for (int i = 0; i < children.length; i++) {
			childRef[i].computePhysics();
			mass += childRef[i].mass;
			Vector3.addto(com, childRef[i].com, childRef[i].mass);
		}
		com = Vector3.multiply(com, 1.0f / mass);

		// Now the inertia tensor
		inertiaTensor = Matrix4.identity();
		inertiaTensor.data.put(0, 0);
		inertiaTensor.data.put(5, 0);
		inertiaTensor.data.put(10, 0);

		// Add in other values
		for (int i = 0; i < meshes.length; i++) {
			Matrix4.addto(inertiaTensor, meshRef[i].inertiaTensor);
			Matrix4.add_inertia_tensor(inertiaTensor, meshRef[i].mass,
					Vector3.lincom(meshRef[i].com, 1, com, -1));
		}
		for (int i = 0; i < children.length; i++) {
			Matrix4.addto(inertiaTensor, childRef[i].inertiaTensor);
			Matrix4.add_inertia_tensor(inertiaTensor, childRef[i].mass,
					Vector3.lincom(childRef[i].com, 1, com, -1));
		}

		// Now convert out of body space into parent space
		com = Matrix4.multiply(trans, com);

		final Matrix4 rotMat = Matrix4.mat3(trans);
		inertiaTensor = Matrix4.inertia_tensor_multiply(inertiaTensor, rotMat);
		inertiaInverse = Matrix4.invert(inertiaTensor);
	}
}
