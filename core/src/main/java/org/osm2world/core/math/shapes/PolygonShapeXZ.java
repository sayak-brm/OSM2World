package org.osm2world.core.math.shapes;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.osm2world.core.math.GeometryUtil;
import org.osm2world.core.math.LineSegmentXZ;
import org.osm2world.core.math.PolygonWithHolesXZ;
import org.osm2world.core.math.SimplePolygonXZ;
import org.osm2world.core.math.TriangleXZ;
import org.osm2world.core.math.VectorXZ;

/**
 * supertype for polygons, defined as closed 2d shapes with 3 or more vertices.
 *
 * <p>{@link SimplePolygonXZ} is the subclass that can represent any polygon without holes,
 * and {@link PolygonWithHolesXZ} allows for polygons with holes.
 * Other subclasses are specialized to a subset, e.g. triangles in the case of {@link TriangleXZ}.
 */
public interface PolygonShapeXZ extends ClosedShapeXZ {

	@Override
	public SimplePolygonShapeXZ getOuter();

	@Override
	public Collection<? extends SimplePolygonShapeXZ> getHoles();

	/** returns a collection that contains the outer polygon and all holes */
	@Override
	public default List<? extends SimplePolygonShapeXZ> getRings() {
		if (getHoles().isEmpty()) {
			return singletonList(getOuter());
		} else {
			List<SimplePolygonShapeXZ> result = new ArrayList<>(getHoles().size() + 1);
			result.add(getOuter());
			result.addAll(getHoles());
			return result;
		}
	}

	/** returns this polygon's area */
	@Override
	public default double getArea() {
		//FIXME incorrect for overlapping holes (those should probably be made explicitly illegal)
		double area = getOuter().getArea();
		for (SimplePolygonShapeXZ hole : getHoles()) {
			area -= hole.getArea();
		}
		return area;
	}

	/** returns the largest distance between any pair of vertices of this polygon */
	public default double getDiameter() {
		return getOuter().getDiameter();
	}

	public default boolean contains(VectorXZ v) {
		if (!getOuter().contains(v)) {
			return false;
		} else {
			for (SimplePolygonShapeXZ hole : getHoles()) {
				if (hole.contains(v)) {
					return false;
				}
			}
			return true;
		}
	}

	public default boolean contains(LineSegmentXZ lineSegment) {
		if (!this.contains(lineSegment.p1) || !this.contains(lineSegment.p2)) {
			return false;
		} else {
			for (SimplePolygonShapeXZ ring : getRings()) {
				if (ring.intersects(lineSegment.p1, lineSegment.p2)) {
					return false;
				}
			}
		}
		return true;
	}

	/** returns true if this polygon contains the parameter polygon */
	public default boolean contains(PolygonShapeXZ p) {
		//FIXME: it is possible that a polygon contains all vertices of another polygon, but still not the entire polygon
		List<VectorXZ> vertexLoop = vertices();
		for (VectorXZ v : p.vertices()) {
			if (!vertexLoop.contains(v) && !this.contains(v)) {
				return false;
			}
		}
		return true;
	}

	/** checks if this polygon's outline intersects the line segment defined by the two parameter points */
	public default boolean intersects(VectorXZ segmentP1, VectorXZ segmentP2) {

		List<VectorXZ> vertexList = vertices();

		for (int i = 0; i + 1 < vertexList.size(); i++) {

			VectorXZ intersection = GeometryUtil.getTrueLineSegmentIntersection(
					segmentP1, segmentP2,
					vertexList.get(i), vertexList.get(i+1));

			if (intersection != null) {
				return true;
			}

		}

		return false;

	}

	/** @see #intersects(VectorXZ, VectorXZ) */
	public default boolean intersects(LineSegmentXZ lineSegment) {
		return intersects(lineSegment.p1, lineSegment.p2);
	}

	/** returns true if there is an intersection between this polygon's and the parameter polygon's outlines */
	public default boolean intersects(PolygonShapeXZ outlinePolygonXZ) {

		//TODO (performance): pairwise intersection checks for each line segment of this shape and the other other will often not be the fastest method

		List<VectorXZ> vertexList = vertices();
		for (int i = 0; i + 1 < vertexList.size(); i++) {
			if (outlinePolygonXZ.intersects(vertexList.get(i), vertexList.get(i+1))) {
				return true;
			}
		}

		return false;
	}

	@Override
	public default List<VectorXZ> intersectionPositions(LineSegmentXZ lineSegment) {
		return getRings().stream().flatMap(p -> p.intersectionPositions(lineSegment).stream()).collect(toList());
	}

	public default Collection<VectorXZ> intersectionPositions(PolygonShapeXZ p2) {
		return getRings().stream().flatMap(p -> p.intersectionPositions(p2).stream()).collect(toList());
	}

	/** returns a point contained inside the shape, i.e. a point for which {@link #contains(VectorXZ)} is true */
	public default VectorXZ getPointInside() {
		VectorXZ center = getOuter().getCentroid();
		if (this.contains(center)) {
			return center;
		} else {
			return getTriangulation().iterator().next().getCenter();
		}
	}

	@Override
	public PolygonShapeXZ transform(Function<VectorXZ, VectorXZ> operation);

	@Override
	public default PolygonShapeXZ mirrorX(double axisX) {
		return transform(v -> v.mirrorX(axisX));
	}

	@Override
	default PolygonShapeXZ rotatedCW(double angleRad) {
		return transform(v -> v.rotate(angleRad));
	}

}
