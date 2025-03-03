package org.osm2world.scene.model;

import static org.osm2world.math.VectorXYZ.NULL_VECTOR;

import java.util.List;

import org.osm2world.scene.mesh.Mesh;

/**
 * a single 3D model, defined in code or loaded from a file or other resource
 */
public interface Model {

	/**
	 * returns the meshes making up this {@link Model}
	 */
	default List<Mesh> getMeshes() {
		return buildMeshes(new InstanceParameters(NULL_VECTOR, 0));
	}

	/**
	 * returns the meshes making up an instance of this {@link Model}.
	 */
	List<Mesh> buildMeshes(InstanceParameters params);

}