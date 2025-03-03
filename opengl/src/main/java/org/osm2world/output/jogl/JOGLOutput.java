package org.osm2world.output.jogl;

import java.awt.*;
import java.io.File;
import java.util.List;

import org.osm2world.conversion.O2WConfig;
import org.osm2world.math.VectorXYZ;
import org.osm2world.math.shapes.AxisAlignedRectangleXZ;
import org.osm2world.output.common.DrawBasedOutput;
import org.osm2world.output.common.lighting.GlobalLightingParameters;
import org.osm2world.output.common.rendering.Camera;
import org.osm2world.output.common.rendering.Projection;

public interface JOGLOutput extends DrawBasedOutput {

	/**
	 * discards all accumulated draw calls
	 */
	public void reset();

	public void drawPoints(Color color, VectorXYZ... vs);

	public void drawLineStrip(Color color, int width, VectorXYZ... vs);

	public void drawLineStrip(Color color, int width, List<VectorXYZ> vs);

	public void drawLineLoop(Color color, int width, List<VectorXYZ> vs);

	/**
	 * set global lighting parameters. Using this method affects all primitives
	 * (even those from previous draw calls).
	 *
	 * @param parameters  parameter object; null disables lighting
	 */
	public void setGlobalLightingParameters(
			GlobalLightingParameters parameters);

	/**
	 * set global rendering parameters. Using this method affects all primitives
	 * (even those from previous draw calls).
	 */
	public void setRenderingParameters(
			JOGLRenderingParameters renderingParameters);

	public void setConfiguration(O2WConfig config);

	public boolean isFinished();

	public void render(Camera camera, Projection projection);

	/**
	 * similar to {@link #render(Camera, Projection)},
	 * but allows rendering only a part of the "normal" image.
	 * For example, with xStart=0, xEnd=0.5, yStart=0 and yEnd=1,
	 * only the left half of the full image will be rendered,
	 * but it will be stretched to cover the available space.
	 * Values other than start at 0 and end at 1 are only supported for orthographic projections!
	 */
	public void renderPart(Camera camera, Projection projection,
			double xStart, double xEnd, double yStart, double yEnd);

	public void freeResources();

	public void drawBackgoundImage(File backgroundImage,
			int startPixelX, int startPixelY,
			int pixelWidth, int pixelHeight,
			JOGLTextureManager textureManager);

	/**
	 * Set the boundary for the relevant data. All data outside of this boundary may be ignored by the target
	 * to improve quality and performance.
	 */
	public void setXZBoundary(AxisAlignedRectangleXZ boundary);

}
