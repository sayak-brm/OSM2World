package org.osm2world.core.target.common.material;

import static java.util.Collections.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.PNGTranscoder;

public class ImageFileTexture extends TextureData {

	/**
	 * Path to the texture file.
	 * Represents a permanent, already saved image file in contrast to {@link RuntimeTexture}'s temmporary image file.
	 */
	private final File file;

	public ImageFileTexture(File file, double width, double height, @Nullable Double widthPerEntity, @Nullable Double heightPerEntity,
			Wrap wrap, TexCoordFunction texCoordFunction) {
		super(width, height, widthPerEntity, heightPerEntity, wrap, texCoordFunction);
		this.file = file;
	}

	public File getFile() {
		return file;
	}

	public boolean isSvgTexture() {
		return file.getName().endsWith(".svg");
	}

	@Override
	public BufferedImage getBufferedImage() {
		try {
			if (isSvgTexture()) {
				return svgToBufferedImage(this.file);
			} else {
				return ImageIO.read(this.file);
			}
		} catch (IOException e) {
			throw new Error("Could not read texture file " + file, e);
		}
	}

	/** variant of {@link #svgToBufferedImage(File, int, int)} that uses a default, power-of-two image size */
	private static final BufferedImage svgToBufferedImage(File svg) throws IOException {
		return svgToBufferedImage(svg, 512, 512);
	}

	/**
	 * Converts an .svg image file to a raster image and returns it
	 *
	 * @param svgFile  the svg file to be converted
	 * @param width  horizontal resolution (in pixels) of the output image
	 * @param height  vertical resolution (in pixels) of the output image
	 */
	private static final BufferedImage svgToBufferedImage(File svgFile, int width, int height) throws IOException {

		if (width <= 0 || height <= 0) throw new IllegalArgumentException("Invalid resolution: " + width + "x" + height);

		double outputAspectRatio = width / height;

		try {

			/* first conversion (temporary result to determine the SVG's aspect ratio) */

			BufferedImage tmpImage = svgToBufferedImageImpl(svgFile, emptyMap());
			double inputAspectRatio = tmpImage.getWidth() / (float)tmpImage.getHeight();

			/* second conversion (to produce the actual output image) */

			Map<TranscodingHints.Key, Object> transcodingHints;

			if (outputAspectRatio > inputAspectRatio) {
				transcodingHints = singletonMap(PNGTranscoder.KEY_WIDTH, (float) width);
			} else {
				transcodingHints = singletonMap(PNGTranscoder.KEY_HEIGHT, (float) height);
			}

			BufferedImage outputImage = svgToBufferedImageImpl(svgFile, transcodingHints);

			/* scale the output image to the desired resolution */

			outputImage = getScaledImage(outputImage, width, height);

			return outputImage;

		} catch (TranscoderException e) {
			throw new IOException(e);
		}

	}

	private static final BufferedImage svgToBufferedImageImpl(File svgFile,
			Map<TranscodingHints.Key, Object> transcodingHints) throws IOException, TranscoderException {

		PNGTranscoder t = new PNGTranscoder();
		t.setTranscodingHints(transcodingHints);

		TranscoderInput input = new TranscoderInput(svgFile.toURI().toString());

		try (ByteArrayOutputStream ostream = new ByteArrayOutputStream()) {

			TranscoderOutput output = new TranscoderOutput(ostream);
			t.transcode(input, output);
			ostream.flush();
			byte[] tempImageBytes = ostream.toByteArray();

			try (ByteArrayInputStream istream = new ByteArrayInputStream(tempImageBytes)) {
				return ImageIO.read(istream);
			}

		}

	}

	@Override
	public String getDataUri() {
		return imageToDataUri(getBufferedImage(), getRasterImageFileFormat());
	}

	private String getRasterImageFileFormat() {
		return (isSvgTexture() || getFile().getName().endsWith(".png")) ? "png" : "jpeg";
	}

	@Override
	public String toString() {
		return file.getName();
	}

	//auto-generated
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		return result;
	}

	//auto-generated
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImageFileTexture other = (ImageFileTexture) obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		return true;
	}
}