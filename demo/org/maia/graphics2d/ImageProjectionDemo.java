package org.maia.graphics2d;

import java.awt.Point;
import java.awt.image.BufferedImage;

import org.maia.graphics2d.image.ImageUtils;
import org.maia.graphics2d.image.ops.BandedImageDeformation;
import org.maia.graphics2d.image.ops.BandedImageDeformation.ConstantHorizontalImageBand;
import org.maia.graphics2d.image.ops.BandedImageDeformation.ConstantVerticalImageBand;
import org.maia.graphics2d.image.ops.BandedImageDeformation.HorizontalImageBand;
import org.maia.graphics2d.image.ops.BandedImageDeformation.VerticalImageBand;
import org.maia.graphics2d.image.ops.QuadrilateralImageProjection;
import org.maia.graphics2d.image.ops.QuadrilateralImageProjection.PseudoPerspective;
import org.maia.graphics2d.image.ops.QuadrilateralImageProjection.Quadrilateral;

public class ImageProjectionDemo {

	public static void main(String[] args) {
		new ImageProjectionDemo().startDemo();
	}

	private void startDemo() {
		BufferedImage sourceImage = ImageUtils.readFromFile("demo-resources/zootropolis.png");
		// projectQuadrilateral(sourceImage);
		projectWithHorizontalDeformation(sourceImage);
		projectWithVerticalDeformation(sourceImage);
	}

	private void projectQuadrilateral(BufferedImage sourceImage) {
		Point p1 = new Point(88, 61);
		Point p2 = new Point(247, 15);
		Point p3 = new Point(256, 345);
		Point p4 = new Point(75, 293);
		Quadrilateral targetArea = new Quadrilateral(p1, p2, p3, p4);
		QuadrilateralImageProjection projection = new QuadrilateralImageProjection();
		BufferedImage targetImage = projection.project(sourceImage, targetArea, new PseudoPerspective(0.5f, 0f));
		ImageUtils.writeToFile(targetImage, "demo-resources/zootropolis-quadrilateral.png");
	}

	private void projectWithHorizontalDeformation(BufferedImage sourceImage) {
		BandedImageDeformation<VerticalImageBand> deformation = BandedImageDeformation
				.createVerticalBandedImageDeformation();
		deformation.addBand(new ConstantVerticalImageBand(100, 150f));
		deformation.addBand(new ConstantVerticalImageBand(200, 100f));
		deformation.addBand(new ConstantVerticalImageBand(100, 150f));
		BufferedImage targetImage = deformation.deform(sourceImage);
		ImageUtils.writeToFile(targetImage, "demo-resources/zootropolis-deformation-hor.png");
	}

	private void projectWithVerticalDeformation(BufferedImage sourceImage) {
		BandedImageDeformation<HorizontalImageBand> deformation = BandedImageDeformation
				.createHorizontalBandedImageDeformation();
		deformation.addBand(new ConstantHorizontalImageBand(100, 150f));
		deformation.addBand(new ConstantHorizontalImageBand(200, 100f));
		deformation.addBand(new ConstantHorizontalImageBand(100, 150f));
		BufferedImage targetImage = deformation.deform(sourceImage);
		ImageUtils.writeToFile(targetImage, "demo-resources/zootropolis-deformation-ver.png");
	}

}