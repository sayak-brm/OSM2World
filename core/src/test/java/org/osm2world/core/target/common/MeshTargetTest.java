package org.osm2world.core.target.common;

import static org.junit.Assert.assertEquals;
import static org.osm2world.core.target.common.MeshTarget.ClipToBounds.clipToBounds;
import static org.osm2world.core.target.common.MeshTarget.ClipToBounds.getSegmentsCCW;
import static org.osm2world.core.test.TestUtil.assertAlmostEquals;

import org.junit.Test;
import org.osm2world.core.math.*;

public class MeshTargetTest {

	@Test
	public void testClipToBounds() {

		var tOrig = new TriangleXYZ(new VectorXYZ(0, 0, 0), new VectorXYZ(10, 0, 0), new VectorXYZ(0, 0, 10));

		var bbox = new AxisAlignedRectangleXZ(-10, 5, +10, 15);
		var result1 = clipToBounds(tOrig, getSegmentsCCW(bbox));
		assertEquals(1, result1.size());
		var expectedResult1 = new TriangleXYZ(new VectorXYZ(0, 0, 5), new VectorXYZ(5, 0, 5), new VectorXYZ(0, 0, 10));
		assertAlmostEquals(expectedResult1.getCenter(), result1.iterator().next().getCenter());
		assertAlmostEquals(expectedResult1.getArea(), result1.iterator().next().getArea());

		var tSmall = new TriangleXZ(new VectorXZ(2, 2), new VectorXZ(8, 2), new VectorXZ(2, 8));
		var result2 = clipToBounds(tOrig, getSegmentsCCW(tSmall));
		assertEquals(1, result2.size());
		assertAlmostEquals(tSmall.getCenter().xyz(0), result2.iterator().next().getCenter());
		assertAlmostEquals(tSmall.getArea(), result2.iterator().next().getArea());

	}

}