package org.osm2world.math.datastructures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.osm2world.map_data.data.MapArea;
import org.osm2world.map_data.data.MapElement;
import org.osm2world.map_data.data.MapNode;
import org.osm2world.map_data.data.MapWaySegment;
import org.osm2world.math.BoundedObject;
import org.osm2world.math.VectorXZ;
import org.osm2world.math.shapes.AxisAlignedRectangleXZ;

import com.google.common.collect.Collections2;

/**
 * a quadtree managing {@link MapElement}s of a data set
 * according on their coordinates in the XZ plane.
 */
public class MapQuadtree implements SpatialIndex<MapElement> {

	static final int LEAF_SPLIT_SIZE = 11;

	final QuadInnerNode root;

	static abstract class QuadNode {

		public final AxisAlignedRectangleXZ bounds;

		QuadNode(double minX, double maxX, double minZ, double maxZ) {
			bounds = new AxisAlignedRectangleXZ(minX, minZ, maxX, maxZ);
		}

		/** returns true if this node's bounds contain at least a part of the element */
		boolean contains(BoundedObject element) {

			if (element instanceof MapNode) {

				return contains(((MapNode)element).getPos());

			} else if (element instanceof MapWaySegment) {
				MapWaySegment line = (MapWaySegment)element;

				VectorXZ lineStart = line.getStartNode().getPos();
				VectorXZ lineEnd = line.getEndNode().getPos();

				if (contains(lineStart) || contains(lineEnd)) {
					return true;
				} else if (bounds.intersects(lineStart, lineEnd)) {
					return true;
				}
				return false;

			} else if (element instanceof MapArea) {
				MapArea area = ((MapArea)element);

				for (MapNode node : area.getBoundaryNodes()) {
					if (contains(node.getPos())) {
						return true;
					}
				}

				if (bounds.intersects(area.getPolygon().getOuter())
						|| area.getPolygon().contains(bounds)) {
					return true;
				}

				return false;
			} else {

				return bounds.overlaps(element.boundingBox());

			}
		}

		boolean contains(VectorXZ pos) {
			return bounds.contains(pos);
		}

		abstract void add(MapElement element);

		abstract void addAll(Collection<MapElement> elements);

		/** adds all leaves in the subtree starting at this node to a list */
		abstract void collectLeaves(List<QuadLeaf> leaves);

	}

	static class QuadInnerNode extends QuadNode {

		/** array with four elements */
		final QuadNode childNodes[];

		QuadInnerNode(double minX, double maxX, double minZ, double maxZ) {
			super(minX, maxX, minZ, maxZ);

			childNodes = new QuadNode[4];

			double halfX = (minX+maxX)/2;
			double halfZ = (minZ+maxZ)/2;

			childNodes[0] = new QuadLeaf(this, minX, halfX, minZ, halfZ);
			childNodes[1] = new QuadLeaf(this, halfX, maxX, minZ, halfZ);
			childNodes[2] = new QuadLeaf(this, minX, halfX, halfZ, maxZ);
			childNodes[3] = new QuadLeaf(this, halfX, maxX, halfZ, maxZ);

		}

		@Override
		void add(MapElement element) {
			for (int i=0; i<4; i++) {
				if (childNodes[i].contains(element)) {
					childNodes[i].add(element);
					//continue loop, the element can cross leaf borders
				}
			}
		}

		@Override
		void addAll(Collection<MapElement> elements) {
			for (MapElement element : elements) {
				add(element);
			}
		}

		void trySplitLeaf(QuadLeaf leaf) {

			QuadInnerNode newChild =
					new QuadInnerNode(leaf.bounds.minX, leaf.bounds.maxX, leaf.bounds.minZ, leaf.bounds.maxZ);

			/* check whether splitting will reduce the maximum node size */

			boolean nodeSizeReduced = true;

			for (int i=0; i<4; i++) {
				boolean newLeafContainsAllElements = true;
				for (MapElement element : leaf) {
					if (!newChild.childNodes[i].contains(element)) {
						newLeafContainsAllElements = false;
						break;
					}
				}
				if (newLeafContainsAllElements) {
					nodeSizeReduced = false;
					break;
				}
			}

			if (nodeSizeReduced) {

				/* replace the leaf with the new child node */

				for (int i=0; i<4; i++) {
					if (childNodes[i] == leaf) {
						childNodes[i] = newChild;
						childNodes[i].addAll(leaf.elements);
						return;
					}
				}

				throw new AssertionError("leaf is not a child of this node");

			}

		}

		@Override
		void collectLeaves(List<QuadLeaf> leaves) {
			for (int i=0; i<4; i++) {
				childNodes[i].collectLeaves(leaves);
			}
		}

	}

	static public class QuadLeaf extends QuadNode implements Iterable<MapElement> {

		final QuadInnerNode parent;
		final ArrayList<MapElement> elements;

		QuadLeaf(QuadInnerNode parent, double minX, double maxX, double minZ, double maxZ) {
			super(minX, maxX, minZ, maxZ);

			this.parent = parent;

			elements = new ArrayList<MapElement>(LEAF_SPLIT_SIZE);

		}

		@Override
		void add(MapElement element) {

			elements.add(element);

			if (elements.size() >= LEAF_SPLIT_SIZE) {
				parent.trySplitLeaf(this);
			}

		}

		@Override
		void addAll(Collection<MapElement> element) {
			/* addAll cannot be implemented by iterating over add:
			 * if the leaf would be "split"(replaced with an inner node)
			 * during the iteration, the remaining elements would still
			 * be added to the now-useless leaf object */

			elements.addAll(element);

			if (elements.size() >= LEAF_SPLIT_SIZE) {
				parent.trySplitLeaf(this);
			}

		}

		@Override
		public Iterator<MapElement> iterator() {
			return elements.iterator();
		}

		@Override
		void collectLeaves(List<QuadLeaf> leaves) {
			leaves.add(this);
		}

	}

	public MapQuadtree(AxisAlignedRectangleXZ dataBoundary) {

		root = new QuadInnerNode(
				dataBoundary.minX, dataBoundary.maxX,
				dataBoundary.minZ, dataBoundary.maxZ);

	}

	@Override
	public void insert(MapElement e) {
		root.add(e);
	}

	@Override
	public Collection<QuadLeaf> probeLeaves(BoundedObject e) {
		// TODO this seems like a very inefficient implementation that does not utilize the quadtree structure
		return Collections2.filter(getLeaves(), it -> it.contains(e));
	}

	@Override
	public Collection<QuadLeaf> getLeaves() {
		List<QuadLeaf> leaves = new ArrayList<>();
		root.collectLeaves(leaves);
		return leaves;
	}

}
