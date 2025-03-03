package org.osm2world.viewer.control.actions;

import java.awt.event.ActionEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.*;

import org.osm2world.math.VectorXYZ;
import org.osm2world.math.geo.MapProjection;
import org.osm2world.scene.Scene;
import org.osm2world.viewer.model.Data;
import org.osm2world.viewer.model.RenderOptions;

public class ShowCameraConfigurationAction
	extends AbstractAction implements Observer {

	private static final long serialVersionUID = -3461617949419339009L;
	private final Data data;
	private final RenderOptions renderOptions;

	public ShowCameraConfigurationAction(Data data, RenderOptions renderOptions) {

		super("Show current camera configuration");
		this.data = data;
		this.renderOptions = renderOptions;

		setEnabled(false);
		data.addObserver(this);

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

		Scene r = data.getConversionResults();

		if (r == null) {
			JOptionPane.showMessageDialog(null, "no Camera defined");
			return;
		}

		MapProjection mapProjection = data.getConversionResults().getMapProjection();
		assert mapProjection != null; // this action is disabled before data is loaded

		VectorXYZ pos = renderOptions.camera.pos();
		VectorXYZ lookAt = renderOptions.camera.lookAt();

		var text = new JTextArea(
				"posLat = " + mapProjection.toLat(pos.xz())
				+ "\nposLon = " + mapProjection.toLon(pos.xz())
				+ "\nposEle = " + pos.y
				+ "\nlookAtLat = " + mapProjection.toLat(lookAt.xz())
				+ "\nlookAtLon = " + mapProjection.toLon(lookAt.xz())
				+ "\nlookAtEle = " + lookAt.y);
		text.setEditable(false);
		text.setOpaque(false);
		JOptionPane.showMessageDialog(null, text,
				"Current camera configuration", JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public void update(Observable o, Object arg) {
		setEnabled(data.getConversionResults() != null);
	}

}
