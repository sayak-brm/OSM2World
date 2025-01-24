package org.osm2world.core.world.modules.building.roof;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.osm2world.core.math.GeometryUtil.closeLoop;
import static org.osm2world.core.target.common.material.Materials.ROOF_DEFAULT;

import org.junit.Test;
import org.osm2world.core.map_data.data.TagSet;
import org.osm2world.core.math.PolygonWithHolesXZ;
import org.osm2world.core.math.SimplePolygonXZ;
import org.osm2world.core.math.VectorXZ;

public class SkillionRoofTest {

	@Test
	public void testTrapezoidRoof() {

		var polygon = new SimplePolygonXZ(closeLoop(
				new VectorXZ(+5, -5),
				new VectorXZ(+10, +5),
				new VectorXZ(-10, +5),
				new VectorXZ(-5, -5)
				));

		var roof = new SkillionRoof(new PolygonWithHolesXZ(polygon, emptyList()),
				TagSet.of("roof:direction", "S", "roof:height", "1"), ROOF_DEFAULT);

		assertEquals(1.0, roof.getRoofHeightAt(new VectorXZ(+10, +5)), 1e-5);
		assertEquals(1.0, roof.getRoofHeightAt(new VectorXZ(  0, +5)), 1e-5);
		assertEquals(1.0, roof.getRoofHeightAt(new VectorXZ(-10, +5)), 1e-5);

		assertEquals(0.5, roof.getRoofHeightAt(new VectorXZ(+7.5, 0)), 1e-5);
		assertEquals(0.5, roof.getRoofHeightAt(new VectorXZ(0, 0)), 1e-5);
		assertEquals(0.5, roof.getRoofHeightAt(new VectorXZ(-7.5, 0)), 1e-5);

		assertEquals(0.0, roof.getRoofHeightAt(new VectorXZ(+5, -5)), 1e-5);
		assertEquals(0.0, roof.getRoofHeightAt(new VectorXZ( 0, -5)), 1e-5);
		assertEquals(0.0, roof.getRoofHeightAt(new VectorXZ(-5, -5)), 1e-5);

	}

	@Test
	public void testRoofAngle() {

		var polygon = new SimplePolygonXZ(closeLoop(
				new VectorXZ(0, 0),
				new VectorXZ(10, 0),
				new VectorXZ(10, 10),
				new VectorXZ(0, 10)
		));

		var roof = new SkillionRoof(new PolygonWithHolesXZ(polygon, emptyList()),
				TagSet.of("roof:direction", "W", "roof:angle", "45"), ROOF_DEFAULT);

		assertEquals(10, roof.calculatePreliminaryHeight(), 1e-2);

	}

}
