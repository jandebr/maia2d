package org.maia.graphics2d.function;

import java.util.List;
import java.util.Vector;

import org.maia.graphics2d.geometry.ApproximatingCurve2D;
import org.maia.graphics2d.geometry.BSplineUtils;
import org.maia.graphics2d.geometry.Point2D;

/**
 * A function in 2D that approximates (approaches, not necessarily interpolates) an open-ended sequence of control
 * values.
 * 
 * <p>
 * The paradigm is to evaluate the function for increasing floating point values of <em>x</em>, starting at 0. Whenever
 * <em>x</em> passes an integer number (<code>i</code> = 0, 1, 2,...) the function automatically appends a new control
 * value <code>V(i)</code>, allowing the smooth approximation to continue. Control values are sampled via an instance of
 * {@link ControlValueGenerator} which is provided at construction time.
 * </p>
 * <p>
 * This class provides convenient constructor methods for common function types:
 * <ul>
 * <li>A <b>linear function</b> generates a polyline that interpolates the control values, more specifically
 * <code>y=V(0)</code> at <code>x=0</code>, <code>y=V(1)</code> at <code>x=1.0</code>, <code>y=V(2)</code> at
 * <code>x=2.0</code> etc.</li>
 * <li>A <b>quadratic function</b> generates a function that is 1-smooth (first derivative is contiguous), interpolates
 * the first control value <code>y=V(0)</code> at <code>x=0</code> and approximates any subsequent control values</li>
 * <li>A <b>quadratic primed function</b> generates a function that is 1-smooth, uses the first control points
 * <code>V(0)</code> and <code>V(1)</code> for priming and approximates <code>V(2)</code> and subsequent control values
 * starting at <code>x=0</code></li>
 * <li>A <b>cubic function</b> generates a function that is 2-smooth (first and second derivatives are contiguous),
 * interpolates the first control value <code>y=V(0)</code> at <code>x=0</code> and approximates any subsequent control
 * values</li>
 * <li>A <b>cubic primed function</b> generates a function that is 2-smooth, uses the first control points
 * <code>V(0)</code>, <code>V(1)</code> and <code>V(2)</code> for priming and approximates <code>V(3)</code> and
 * subsequent control values starting at <code>x=0</code></li>
 * </ul>
 * </p>
 * <p>
 * The implementation uses <em>B-Spline</em> functions of a specific order as blending functions
 * </p>
 * 
 * @see ApproximatingCurve2D
 */
public class PerpetualApproximatingFunction2D implements Function2D {

	private ControlValueGenerator controlValueGenerator;

	private PerpetualStandardCurve2D approximatingCurve;

	private double startX;

	private int shifts;

	private PerpetualApproximatingFunction2D(ControlValueGenerator controlValueGenerator, int blendingFunctionOrder) {
		this(controlValueGenerator, blendingFunctionOrder, 0);
	}

	private PerpetualApproximatingFunction2D(ControlValueGenerator controlValueGenerator, int blendingFunctionOrder,
			double startX) {
		this.controlValueGenerator = controlValueGenerator;
		this.approximatingCurve = createApproximatingCurve(blendingFunctionOrder);
		this.startX = startX;
	}

	public static PerpetualApproximatingFunction2D createLinearInterpolatingFunction(
			ControlValueGenerator controlValueGenerator) {
		return new PerpetualApproximatingFunction2D(controlValueGenerator, 2);
	}

	public static PerpetualApproximatingFunction2D createQuadraticApproximatingFunction(
			ControlValueGenerator controlValueGenerator) {
		return new PerpetualApproximatingFunction2D(controlValueGenerator, 3);
	}

	public static PerpetualApproximatingFunction2D createQuadraticPrimedApproximatingFunction(
			ControlValueGenerator controlValueGenerator) {
		return new PerpetualApproximatingFunction2D(controlValueGenerator, 3, 2.0);
	}

	public static PerpetualApproximatingFunction2D createCubicApproximatingFunction(
			ControlValueGenerator controlValueGenerator) {
		return new PerpetualApproximatingFunction2D(controlValueGenerator, 4);
	}

	public static PerpetualApproximatingFunction2D createCubicPrimedApproximatingFunction(
			ControlValueGenerator controlValueGenerator) {
		return new PerpetualApproximatingFunction2D(controlValueGenerator, 4, 3.0);
	}

	private PerpetualStandardCurve2D createApproximatingCurve(int blendingFunctionOrder) {
		int L = 1 + 2 * blendingFunctionOrder;
		List<Point2D> controlPoints = new Vector<Point2D>(L + 1);
		for (int i = 0; i <= L; i++) {
			controlPoints.add(sampleNewControlPoint());
		}
		return new PerpetualStandardCurve2D(controlPoints, blendingFunctionOrder);
	}

	private Point2D sampleNewControlPoint() {
		return new Point2D(0, getControlValueGenerator().generateControlValue());
	}

	@Override
	public double evaluate(double x) {
		PerpetualStandardCurve2D curve = getApproximatingCurve();
		double t = getStartX() + x - getShifts();
		while (t >= getBlendingFunctionOrder()) {
			setShifts(getShifts() + 1);
			curve.shift(sampleNewControlPoint());
			t = getStartX() + x - getShifts();
		}
		return curve.sample(t).getY();
	}

	public int getBlendingFunctionOrder() {
		return getApproximatingCurve().getBlendingFunctionOrder();
	}

	public ControlValueGenerator getControlValueGenerator() {
		return controlValueGenerator;
	}

	private PerpetualStandardCurve2D getApproximatingCurve() {
		return approximatingCurve;
	}

	private double getStartX() {
		return startX;
	}

	private int getShifts() {
		return shifts;
	}

	private void setShifts(int shifts) {
		this.shifts = shifts;
	}

	public static interface ControlValueGenerator {

		double generateControlValue();

	}

	private static class PerpetualStandardCurve2D extends ApproximatingCurve2D {

		public PerpetualStandardCurve2D(List<Point2D> controlPoints, int blendingFunctionOrder) {
			super(controlPoints, blendingFunctionOrder,
					BSplineUtils.buildStandardKnots(blendingFunctionOrder, controlPoints.size() - 1), 0,
					controlPoints.size() - blendingFunctionOrder + 1);
		}

		public void shift(Point2D newControlPoint) {
			getControlPoints().remove(0);
			getControlPoints().add(newControlPoint);
		}

		@Override
		protected double projectT(double t) {
			return t;
		}

	}

}