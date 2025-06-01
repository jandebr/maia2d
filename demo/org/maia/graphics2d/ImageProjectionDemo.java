package org.maia.graphics2d;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Vector;

import org.maia.graphics2d.geometry.ApproximatingCurve2D;
import org.maia.graphics2d.geometry.Curve2D;
import org.maia.graphics2d.geometry.Point2D;
import org.maia.graphics2d.image.ImageUtils;
import org.maia.graphics2d.image.ops.BandedImageDeformation;
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
		String name = "zootropolis";
		BufferedImage sourceImage = ImageUtils.readFromFile("demo-resources/" + name + ".png");
		ImageUtils.writeToFile(projectQuadrilateral(sourceImage), "demo-resources/" + name + "-quadrilateral.png");
		ImageUtils.writeToFile(projectWithHorizontalDeformation(sourceImage),
				"demo-resources/" + name + "-deformation-hor.png");
		ImageUtils.writeToFile(projectWithVerticalDeformation(sourceImage),
				"demo-resources/" + name + "-deformation-ver.png");
		ImageUtils.writeToFile(projectWithVerticalDeformation(projectWithHorizontalDeformation(sourceImage)),
				"demo-resources/" + name + "-deformation.png");
	}

	private BufferedImage projectQuadrilateral(BufferedImage sourceImage) {
		Point p1 = new Point(88, 61);
		Point p2 = new Point(247, 15);
		Point p3 = new Point(256, 345);
		Point p4 = new Point(75, 293);
		Quadrilateral targetArea = new Quadrilateral(p1, p2, p3, p4);
		QuadrilateralImageProjection projection = new QuadrilateralImageProjection();
		return projection.project(sourceImage, targetArea, new PseudoPerspective(0.5f, 0f));
	}

	private BufferedImage projectWithHorizontalDeformation(BufferedImage sourceImage) {
		int n = 10;
		int width = ImageUtils.getWidth(sourceImage);
		int height = ImageUtils.getHeight(sourceImage);
		List<Point2D> controlPoints = new Vector<Point2D>(n);
		for (int i = 0; i < n; i++) {
			float x = (0.2f + 0.6f * (float) Math.random()) * width;
			float y = i / (n - 1f) * height;
			controlPoints.add(new Point2D(x, y));
		}
		Curve2D separator = ApproximatingCurve2D.createStandardCurve(controlPoints);
		BandedImageDeformation<VerticalImageBand> deformation = BandedImageDeformation
				.createCurvedVerticalBandedImageDeformation(width, 0.5f, separator);
		return deformation.deform(sourceImage);
	}

	private BufferedImage projectWithVerticalDeformation(BufferedImage sourceImage) {
		int n = 10;
		int width = ImageUtils.getWidth(sourceImage);
		int height = ImageUtils.getHeight(sourceImage);
		List<Point2D> controlPoints = new Vector<Point2D>(n);
		for (int i = 0; i < n; i++) {
			float x = i / (n - 1f) * width;
			float y = (0.2f + 0.6f * (float) Math.random()) * height;
			controlPoints.add(new Point2D(x, y));
		}
		Curve2D separator = ApproximatingCurve2D.createStandardCurve(controlPoints);
		BandedImageDeformation<HorizontalImageBand> deformation = BandedImageDeformation
				.createCurvedHorizontalBandedImageDeformation(height, 0.5f, separator);
		return deformation.deform(sourceImage);
	}

}