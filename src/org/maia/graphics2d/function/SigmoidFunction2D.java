package org.maia.graphics2d.function;

public class SigmoidFunction2D implements Function2D {

	private double mean;

	private double horizontalScale;

	private double verticalScale;

	private double verticalOffset;

	public SigmoidFunction2D() {
		this(0, 1.0);
	}

	public SigmoidFunction2D(double mean, double horizontalScale) {
		this(mean, horizontalScale, 1.0, 0);
	}

	public SigmoidFunction2D(double mean, double horizontalScale, double verticalScale, double verticalOffset) {
		this.mean = mean;
		this.horizontalScale = horizontalScale;
		this.verticalScale = verticalScale;
		this.verticalOffset = verticalOffset;
	}

	@Override
	public double evaluate(double x) {
		return getVerticalOffset() + getVerticalScale() / (1.0 + Math.exp(-getHorizontalScale() * (x - getMean())));
	}

	public double getMean() {
		return mean;
	}

	public void setMean(double mean) {
		this.mean = mean;
	}

	public double getHorizontalScale() {
		return horizontalScale;
	}

	public void setHorizontalScale(double horizontalScale) {
		this.horizontalScale = horizontalScale;
	}

	public double getVerticalScale() {
		return verticalScale;
	}

	public void setVerticalScale(double verticalScale) {
		this.verticalScale = verticalScale;
	}

	public double getVerticalOffset() {
		return verticalOffset;
	}

	public void setVerticalOffset(double verticalOffset) {
		this.verticalOffset = verticalOffset;
	}

}