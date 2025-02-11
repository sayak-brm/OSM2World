package org.osm2world.target;

import javax.annotation.Nullable;

import org.osm2world.conversion.O2WConfig;
import org.osm2world.target.common.MeshStore;
import org.osm2world.target.common.mesh.LevelOfDetail;
import org.osm2world.target.common.mesh.Mesh;
import org.osm2world.target.common.mesh.TriangleGeometry;
import org.osm2world.world.data.WorldObject;

/**
 * A sink for rendering/writing {@link WorldObject}s to.
 */
public interface Target extends CommonTarget {

	void setConfiguration(O2WConfig config);
	O2WConfig getConfiguration();

	default LevelOfDetail getLod() {
		return getConfiguration().getLod();
	}

	/**
	 * announces the begin of the draw* calls for a {@link WorldObject}.
	 * This allows targets to group them, if desired.
	 * Otherwise, this can be ignored.
	 *
	 * @param object  the object that all draw method calls until the next beginObject belong to; can be null
	 */
	default void beginObject(@Nullable WorldObject object) {}

	public default void drawMesh(Mesh mesh) {
		if (mesh.lodRange.contains(getLod())) {
			var converter = new MeshStore.ConvertToTriangles(getLod());
			TriangleGeometry tg = converter.applyToGeometry(mesh.geometry);
			drawTriangles(mesh.material, tg.triangles, tg.normalData.normals(), tg.texCoords);
		}
	}

	/**
	 * gives the target the chance to perform finish/cleanup operations
	 * after all objects have been drawn.
	 */
	void finish();

}
