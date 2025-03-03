package org.osm2world.viewer.control.navigation;

import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;

import javax.swing.*;
import javax.swing.event.MouseInputListener;

import org.osm2world.math.VectorXYZ;
import org.osm2world.output.common.rendering.MutableCamera;
import org.osm2world.viewer.model.RenderOptions;
import org.osm2world.viewer.view.ViewerFrame;


public class DefaultNavigation extends MouseAdapter implements KeyListener, MouseInputListener {

	private final static double ANGLE_INCREMENT = Math.PI/200;
	private final static double MOVEMENT_INCREMENT = 2.0;
	private final static double SLOW_MOVEMENT_INCREMENT = 0.1;

	private final RenderOptions renderOptions;
	private final ViewerFrame viewerFrame;

	private final Timer timer;

	public DefaultNavigation(ViewerFrame viewerFrame, RenderOptions renderOptions) {

		this.viewerFrame = viewerFrame;
		this.renderOptions = renderOptions;

		timer = new Timer(20, KEYBOARD_TASK);
	}

	private boolean translationDrag = false;
	private boolean rotationDrag = false;
	private boolean movementDrag = false;

	private Point previousMousePoint;

	private final Set<Integer> pressedKeys = new HashSet<Integer>();

	@Override
	public void keyPressed(KeyEvent e) {
		synchronized (pressedKeys) {
			pressedKeys.add(e.getKeyCode());
			timer.start();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		synchronized (pressedKeys) {
			pressedKeys.remove(e.getKeyCode());
			if (pressedKeys.isEmpty())
				timer.stop();
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void mouseDragged(MouseEvent e) {

		Point currentMousePoint = e.getPoint();

		float movementX = currentMousePoint.x - previousMousePoint.x;
		float movementY = currentMousePoint.y - previousMousePoint.y;

		MutableCamera camera = renderOptions.camera;

		if (camera != null) {

			if (translationDrag) {

				camera.moveMapForward(movementY);
				camera.moveMapRight(movementX);

			} else if (rotationDrag) {

				/* view left/right */
				camera.rotateY(movementX/100);

				/* view up/down */
				camera.mapPitch(movementY/-100);

			} else if (movementDrag) {

				/* roll left/right */
				camera.roll(movementX/100);

				/* move up/down */
				camera.moveMapUp(movementY);
			}
		}

		previousMousePoint = currentMousePoint;

	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			viewerFrame.setCursor(new Cursor(Cursor.MOVE_CURSOR));
			translationDrag = true;
		} else if (e.getButton() == MouseEvent.BUTTON2) {
			viewerFrame.setCursor(new Cursor(Cursor.MOVE_CURSOR));
			movementDrag = true;
		} else {
			viewerFrame.setCursor(new Cursor(Cursor.MOVE_CURSOR));
			rotationDrag = true;
		}
		previousMousePoint = e.getPoint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		viewerFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		translationDrag = false;
		rotationDrag = false;
		movementDrag = false;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {

		zoom(e.getWheelRotation() < 0, 1);
	}

	private void zoom(boolean zoomIn, double scale) {
		MutableCamera c = renderOptions.camera;

		if (c != null) {

			VectorXYZ toLookAt = c.lookAt().subtract(c.pos());
			VectorXYZ move = toLookAt.mult(scale * (zoomIn ? 0.2f : -0.25f));
			VectorXYZ newPos = c.pos().add(move);

			c.setPos(newPos);
		}
	}

	private final ActionListener KEYBOARD_TASK = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {

			MutableCamera c = renderOptions.camera;

			boolean shiftDown = pressedKeys.contains(KeyEvent.VK_SHIFT);
			double movementIncrement = shiftDown ? SLOW_MOVEMENT_INCREMENT :  MOVEMENT_INCREMENT;

			if (c != null) {

				synchronized (pressedKeys) {

					for (int key : pressedKeys) {

						switch (key) {
						case KeyEvent.VK_Q:
							c.roll(ANGLE_INCREMENT);
							break;
						case KeyEvent.VK_E:
							c.roll(-ANGLE_INCREMENT);
							break;
						case KeyEvent.VK_W:
							c.moveForward(movementIncrement);
							break;
						case KeyEvent.VK_S:
							c.moveForward(-movementIncrement);
							break;
						case KeyEvent.VK_A:
							c.moveRight(movementIncrement);
							break;
						case KeyEvent.VK_D:
							c.moveRight(-movementIncrement);
							break;
						case KeyEvent.VK_PAGE_UP:
							c.moveUp(movementIncrement);
							break;
						case KeyEvent.VK_PAGE_DOWN:
							c.moveUp(-movementIncrement);
							break;
						case KeyEvent.VK_UP:
							c.pitch(ANGLE_INCREMENT);
							break;
						case KeyEvent.VK_DOWN:
							c.pitch(-ANGLE_INCREMENT);
							break;
						case KeyEvent.VK_RIGHT:
							c.yaw(ANGLE_INCREMENT);
							break;
						case KeyEvent.VK_LEFT:
							c.yaw(-ANGLE_INCREMENT);
							break;
						case KeyEvent.VK_PLUS:
						case KeyEvent.VK_I:
							zoom(true, 0.5);
							break;
						case KeyEvent.VK_MINUS:
						case KeyEvent.VK_O:
							zoom(false, 0.5);
							break;
						}
					}
				}
			}
		}
	};
}
