package org.maia.graphics2d;

import java.awt.Point;
import java.awt.image.BufferedImage;

import org.maia.graphics2d.image.ImageUtils;
import org.maia.graphics2d.image.ops.QuadrilateralImageProjection;
import org.maia.graphics2d.image.ops.QuadrilateralImageProjection.PseudoPerspective;
import org.maia.graphics2d.image.ops.QuadrilateralImageProjection.Quadrilateral;

public class ImageProjectionDemo {

	public static void main(String[] args) {
		new ImageProjectionDemo().startDemo();
	}

	private void startDemo() {
		Point p1 = new Point(88, 61);
		Point p2 = new Point(247, 15);
		Point p3 = new Point(256, 345);
		Point p4 = new Point(75, 293);
		Quadrilateral targetArea = new Quadrilateral(p1, p2, p3, p4);
		QuadrilateralImageProjection projection = new QuadrilateralImageProjection();
		BufferedImage sourceImage = ImageUtils.readFromFile("demo-resources/zootropolis.png");
		BufferedImage targetImage = projection.project(sourceImage, targetArea, new PseudoPerspective(0.5f, 0f));
		ImageUtils.writeToFile(targetImage, "demo-resources/zootropolis-projected.png");
	}

}