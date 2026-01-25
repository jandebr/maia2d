package org.maia.graphics2d.function;

public class SigmoidFunction2D implements Function2D {

	private double mean;

	private double horizontalScale;

	private double verticalScale;

	private double verticalOffset;

	private double cappedMinimumValue = Double.MIN_VALUE;

	private double cappedMaximumValue = Double.MAX_VALUE;

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

	/**
	 * Creates a new capped Sigmoid-shaped function
	 * 
	 * @param yMin
	 *            The minimum function value
	 * @param yMax
	 *            The maximum function value
	 * @param xMin
	 *            The domain value for which the function value is minimal : <code>ft(xMin) = yMin</code>
	 * @param xMax
	 *            The domain value for which the function value is maximal : <code>ft(xMax) = yMax</code>
	 * @return A new instance of Sigmoid-shaped function whose range is capped between the specified values
	 */
	public static SigmoidFunction2D createCappedFunction(double yMin, double yMax, double xMin, double xMax) {
		return createCappedFunction(yMin, yMax, xMin, xMax, 0.5, 1.0);
	}

	/**
	 * Creates a new capped Sigmoid-shaped function
	 * 
	 * @param yMin
	 *            The minimum function value
	 * @param yMax
	 *            The maximum function value
	 * @param xMin
	 *            The domain value for which the function value is minimal : <code>ft(xMin) = yMin</code>
	 * @param xMax
	 *            The domain value for which the function value is maximal : <code>ft(xMax) = yMax</code>
	 * @param relativeInflectionDistance
	 *            A value between 0 and 1 representing the relative distance between <code>xMin</code> (as 0) and
	 *            <code>xMax</code> (as 1) where the function inflects. More accurately, where the second derivative of
	 *            the continuously increasing Sigmoid function is 0
	 * @param smoothness
	 *            A strictly positive number (&gt; 0) that controls the smoothness of the Sigmoid function. A larger
	 *            value gives a more smooth function. More accurately, the first derivative is reduced with a higher
	 *            value for smoothness
	 * @return A new instance of Sigmoid-shaped function whose range is capped between the specified values
	 */
	public static SigmoidFunction2D createCappedFunction(double yMin, double yMax, double xMin, double xMax,
			double relativeInflectionDistance, double smoothness) {
		if (yMin > yMax)
			throw new IllegalArgumentException("yMin (" + yMin + ") cannot be greater than yMax (" + yMax + ")");
		if (relativeInflectionDistance < 0 || relativeInflectionDistance > 1.0)
			throw new IllegalArgumentException(
					"relativeInflectionDistance (" + relativeInflectionDistance + ") must be in unit interval");
		if (smoothness <= 0)
			throw new IllegalArgumentException("smoothness (" + smoothness + ") must be strictly positive");
		double xDist = xMax - xMin;
		double ftMean = xMin + relativeInflectionDistance * xDist;
		double ftXscale = 8.0 / (xDist * smoothness);
		SigmoidFunction2D ft = new SigmoidFunction2D(ftMean, ftXscale);
		double y0 = ft.evaluate(xMin);
		double y1 = ft.evaluate(xMax);
		double ftYscale = (yMax - yMin) / (y1 - y0);
		ft.setVerticalScale(ftYscale);
		ft.setVerticalOffset(yMin - y0 * ftYscale);
		ft.setCappedMinimumValue(yMin);
		ft.setCappedMaximumValue(yMax);
		return ft;
	}

	@Override
	public double evaluate(double x) {
		double y = getVerticalOffset() + getVerticalScale() / (1.0 + Math.exp(-getHorizontalScale() * (x - getMean())));
		return Math.max(Math.min(y, getCappedMaximumValue()), getCappedMinimumValue());
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

	public double getCappedMinimumValue() {
		return cappedMinimumValue;
	}

	public void setCappedMinimumValue(double minimum) {
		this.cappedMinimumValue = minimum;
	}

	public double getCappedMaximumValue() {
		return cappedMaximumValue;
	}

	public void setCappedMaximumValue(double maximum) {
		this.cappedMaximumValue = maximum;
	}

}