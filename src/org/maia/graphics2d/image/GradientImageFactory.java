package org.maia.graphics2d.image;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.image.BufferedImage;

import org.maia.util.ColorUtils;

public class GradientImageFactory {

	private GradientImageFactory() {
	}

	public static BufferedImage createLeftToRightGradientImage(Dimension size, Color startColor, Color endColor,
			GradientFunction function) {
		BufferedImage image = ImageUtils.createImage(size);
		double dx = 1.0 / (size.width - 1);
		for (int x = 0; x < size.width; x++) {
			double r = x * dx;
			Color c = ColorUtils.interpolate(startColor, endColor, function.eval(r));
			ImageUtils.clearWithUniformColor(image, c, x, 0, 1, size.height);
		}
		return image;
	}

	public static BufferedImage createRightToLeftGradientImage(Dimension size, Color startColor, Color endColor,
			GradientFunction function) {
		BufferedImage image = ImageUtils.createImage(size);
		double dx = 1.0 / (size.width - 1);
		for (int x = 0; x < size.width; x++) {
			double r = x * dx;
			Color c = ColorUtils.interpolate(startColor, endColor, function.eval(r));
			ImageUtils.clearWithUniformColor(image, c, size.width - 1 - x, 0, 1, size.height);
		}
		return image;
	}

	public static BufferedImage createTopToBottomGradientImage(Dimension size, Color startColor, Color endColor,
			GradientFunction function) {
		BufferedImage image = ImageUtils.createImage(size);
		double dy = 1.0 / (size.height - 1);
		for (int y = 0; y < size.height; y++) {
			double r = y * dy;
			Color c = ColorUtils.interpolate(startColor, endColor, function.eval(r));
			ImageUtils.clearWithUniformColor(image, c, 0, y, size.width, 1);
		}
		return image;
	}

	public static BufferedImage createBottomToTopGradientImage(Dimension size, Color startColor, Color endColor,
			GradientFunction function) {
		BufferedImage image = ImageUtils.createImage(size);
		double dy = 1.0 / (size.height - 1);
		for (int y = 0; y < size.height; y++) {
			double r = y * dy;
			Color c = ColorUtils.interpolate(startColor, endColor, function.eval(r));
			ImageUtils.clearWithUniformColor(image, c, 0, size.height - 1 - y, size.width, 1);
		}
		return image;
	}

	public static BufferedImage createGradientBorderImage(Dimension size, Color borderColor, int borderThickness) {
		return createGradientBorderImage(size, borderColor, borderThickness, createSigmoidGradientFunction());
	}

	public static BufferedImage createGradientBorderImage(Dimension size, Color borderColor, int borderThickness,
			GradientFunction function) {
		return createGradientBorderImage(size, borderColor, ColorUtils.setTransparency(borderColor, 1.0),
				borderThickness, function);
	}

	public static BufferedImage createGradientBorderImage(Dimension size, Color outsideColor, Color insideColor,
			int borderThickness, GradientFunction function) {
		int w = size.width, h = size.height, t = borderThickness;
		Color c1 = outsideColor, c2 = insideColor, c3 = ColorUtils.setTransparency(c2, 1.0);
		BufferedImage left = ImageUtils.addPadding(
				GradientImageFactory.createLeftToRightGradientImage(new Dimension(t, h), c1, c2, function),
				new Insets(0, 0, 0, w - t), c3);
		BufferedImage right = ImageUtils.addPadding(
				GradientImageFactory.createRightToLeftGradientImage(new Dimension(t, h), c1, c2, function),
				new Insets(0, w - t, 0, 0), c3);
		BufferedImage top = ImageUtils.addPadding(
				GradientImageFactory.createTopToBottomGradientImage(new Dimension(w, t), c1, c2, function),
				new Insets(0, 0, h - t, 0), c3);
		BufferedImage bottom = ImageUtils.addPadding(
				GradientImageFactory.createBottomToTopGradientImage(new Dimension(w, t), c1, c2, function),
				new Insets(h - t, 0, 0, 0), c3);
		return ImageUtils.combineByTransparency(ImageUtils.combineByTransparency(left, right),
				ImageUtils.combineByTransparency(top, bottom));
	}

	public static GradientFunction createLinearGradientFunction() {
		return new LinearGradientFunction();
	}

	public static GradientFunction createPolynomialGradientFunction(double exponent) {
		return new PolynomialGradientFunction(exponent);
	}

	public static GradientFunction createSigmoidGradientFunction() {
		return createSigmoidGradientFunction(0.5, 2.0);
	}

	/**
	 * Creates a new Sigmoid-shaped gradient function
	 * 
	 * @param inflectionPoint
	 *            A value between 0 and 1 where the function inflects. More accurately, where the second derivative of
	 *            the continuously increasing Sigmoid function is 0
	 * @param smoothness
	 *            A strictly positive number (&gt; 0) that controls the smoothness of the Sigmoid function. A larger
	 *            value gives a more smooth function. More accurately, the first derivative is reduced with a higher
	 *            value for smoothness
	 * @return A new instance of Sigmoid-shaped gradient function
	 */
	public static GradientFunction createSigmoidGradientFunction(double inflectionPoint, double smoothness) {
		SigmoidFunction function = new SigmoidFunction(12.0 / smoothness, inflectionPoint);
		double y0 = function.eval(0);
		double y1 = function.eval(1.0);
		double s = 1.0 / (y1 - y0);
		function.scale(s).translateY(-y0 * s); // such that: ft(0) = 0 and ft(1) = 1
		return new SigmoidGradientFunction(function);
	}

	public static interface GradientFunction {

		double eval(double r);

	}

	private static class LinearGradientFunction implements GradientFunction {

		public LinearGradientFunction() {
		}

		@Override
		public double eval(double r) {
			return r;
		}

	}

	private static class PolynomialGradientFunction implements GradientFunction {

		private double exponent;

		public PolynomialGradientFunction(double exponent) {
			this.exponent = exponent;
		}

		@Override
		public double eval(double r) {
			double e = getExponent();
			if (e >= 0) {
				return Math.pow(r, e);
			} else {
				return 1.0 - Math.pow(1.0 - r, -e);
			}
		}

		private double getExponent() {
			return exponent;
		}

	}

	private static class SigmoidGradientFunction implements GradientFunction {

		private SigmoidFunction function;

		public SigmoidGradientFunction(SigmoidFunction function) {
			this.function = function;
		}

		@Override
		public double eval(double r) {
			return Math.max(Math.min(getFunction().eval(r), 1.0), 0);
		}

		private SigmoidFunction getFunction() {
			return function;
		}

	}

	private static class SigmoidFunction {

		private double a, b, c, d;

		public SigmoidFunction() {
			this(1.0, 0);
		}

		public SigmoidFunction(double a, double b) {
			this(a, b, 0, 1.0);
		}

		public SigmoidFunction(double a, double b, double c, double d) {
			this.a = a;
			this.b = b;
			this.c = c;
			this.d = d;
		}

		public double eval(double x) {
			return c + d / (1.0 + Math.exp(-a * (x - b)));
		}

		public SigmoidFunction scale(double scale) {
			this.d = scale;
			return this;
		}

		public SigmoidFunction translateY(double dy) {
			this.c = dy;
			return this;
		}

	}

}