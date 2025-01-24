package org.osm2world.viewer.view.debug;

import static java.awt.Color.*;
import static org.osm2world.core.math.VectorXZ.Z_UNIT;

import java.awt.Color;

import org.osm2world.core.math.VectorXYZ;
import org.osm2world.core.target.common.material.ImmutableMaterial;
import org.osm2world.core.target.common.material.Material.Interpolation;
import org.osm2world.core.target.jogl.JOGLTarget;
import org.osm2world.core.world.attachment.AttachmentConnector;
import org.osm2world.core.world.data.WorldObject;

public class AttachmentConnectorDebugView extends DebugView {

	private final Color ATTACHED_COLOUR = GREEN;
	private final Color FAILED_COLOUR = RED;

	@Override
	public String getDescription() {
		return "shows connectors that attach objects to surfaces";
	}

	@Override
	public boolean canBeUsed() {
		return map != null;
	}

	@Override
	protected void fillTarget(JOGLTarget target) {

		for (WorldObject object : map.getWorldObjects()) {
			for (AttachmentConnector connector : object.getAttachmentConnectors()) {

				if (connector.isAttached()) {

					VectorXYZ pos = connector.getAttachedPos();
					target.drawBox(new ImmutableMaterial(Interpolation.FLAT, ATTACHED_COLOUR),
							pos.addY(-0.1), Z_UNIT, 0.2, 0.2, 0.2);
					drawArrow(target, ATTACHED_COLOUR, 0.2f, pos,
							pos.add(connector.getAttachedSurfaceNormal()));

				} else {

					target.drawBox(new ImmutableMaterial(Interpolation.FLAT, FAILED_COLOUR),
							connector.originalPos.addY(-0.1), Z_UNIT, 0.2, 0.2, 0.2);

				}

			}
		}

	}

}
