package org.maia.graphics2d.image.ops;

import java.awt.image.BufferedImage;

import org.maia.graphics2d.image.ImageSampler;
import org.maia.graphics2d.image.ImageUtils;

public class NonLinearImageDeformation {

	private HorizontalCoordinateProjection horizontalProjection;

	private VerticalCoordinateProjection verticalProjection;

	public NonLinearImageDeformation() {
		this(null, null); // produces an identical image
	}

	public NonLinearImageDeformation(HorizontalCoordinateProjection horizontalProjection) {
		this(horizontalProjection, null);
	}

	public NonLinearImageDeformation(VerticalCoordinateProjection verticalProjection) {
		this(null, verticalProjection);
	}

	public NonLinearImageDeformation(HorizontalCoordinateProjection horizontalProjection,
			VerticalCoordinateProjection verticalProjection) {
		setHorizontalProjection(horizontalProjection);
		setVerticalProjection(verticalProjection);
	}

	public BufferedImage deform(BufferedImage sourceImage) {
		int width = ImageUtils.getWidth(sourceImage);
		int height = ImageUtils.getHeight(sourceImage);
		BufferedImage targetImage = ImageUtils.createImage(width, height);
		ImageSampler imageSampler = ImageSampler.createDefaultImageSampler(sourceImage);
		HorizontalCoordinateProjection projectionX = getHorizontalProjection();
		VerticalCoordinateProjection projectionY = getVerticalProjection();
		if (projectionX != null) {
			for (int yi = 0; yi < height; yi++) {
				float yc = 0.5f + yi;
				for (int xi = 0; xi < width; xi++) {
					float xc = 0.5f + xi;
					float pxc = projectionX.projectX(xc, yc, width, height);
					float pyc = projectionY == null ? yc : projectionY.projectY(xc, yc, width, height);
					int argb = imageSampler.sampleRGB(pxc, pyc);
					targetImage.setRGB(xi, yi, argb);
				}
			}
		} else {
			for (int xi = 0; xi < width; xi++) {
				float xc = 0.5f + xi;
				for (int yi = 0; yi < height; yi++) {
					float yc = 0.5f + yi;
					float pyc = projectionY == null ? yc : projectionY.projectY(xc, yc, width, height);
					int argb = imageSampler.sampleRGB(xc, pyc);
					targetImage.setRGB(xi, yi, argb);
				}
			}
		}
		return targetImage;
	}

	public HorizontalCoordinateProjection getHorizontalProjection() {
		return horizontalProjection;
	}

	public void setHorizontalProjection(HorizontalCoordinateProjection projection) {
		this.horizontalProjection = projection;
	}

	public VerticalCoordinateProjection getVerticalProjection() {
		return verticalProjection;
	}

	public void setVerticalProjection(VerticalCoordinateProjection projection) {
		this.verticalProjection = projection;
	}

	public static interface HorizontalCoordinateProjection {

		float projectX(float x, float y, int width, int height);

	}

	public static interface VerticalCoordinateProjection {

		float projectY(float x, float y, int width, int height);

	}

}