package org.osm2world.map_data.data;

/**
 * segment of a {@link MapArea}'s outline. This isn't a {@link MapElement}s
 * itself (for example, it has no individual representation).
 * Instead, it's simply a different way to access an area's data.
 *
 * @see MapData
 */
public class MapAreaSegment extends MapSegment {

	private final MapArea area;
	private final boolean areaRight;

	MapAreaSegment(MapArea area, boolean areaRight,
			MapNode startNode, MapNode endNode) {
		super(startNode, endNode);
		this.area = area;
		this.areaRight = areaRight;
	}

	public MapArea getArea() {
		return area;
	}

	/**
	 * returns true if the area is to the right of this segment
	 */
	public boolean isAreaRight() {
		return areaRight;
	}

	@Override
	public String toString() {
		return area + "[" + startNode + "->" + endNode + "]";
	}

}
