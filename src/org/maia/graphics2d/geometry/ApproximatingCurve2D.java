package org.maia.graphics2d.geometry;

import java.util.List;
import java.util.Vector;

import org.maia.graphics2d.transform.TransformMatrix2D;

/**
 * A curve in 2D that approximates (approaches, not necessarily interpolates) a sequence of control points
 *
 * <p>
 * This implementation uses <em>B-Spline</em> functions of a specific order as blending functions. One special case is
 * when the order equals the number of control points, which generates a <em>Bézier</em> curve. One other special case
 * is when the order equals 2, which generates a polyline through the control points. The smaller the order, the more
 * local control is enforced on the resulting curve. In particular, a curve with order <em>m</em> is defined at each
 * sampling point by at most <em>m</em> control points. An additional property is that the curve is confined to lie
 * within the consecutive convex hulls of <em>m</em> control points.
 * </p>
 * <p>
 * To create a new curve, use one of the class factory methods. You can choose between these predefined curve variants:
 * <ul>
 * <li>A <b>standard curve</b> starts at the first control point and ends in the last control point</li>
 * <li>A <b>uniform open curve</b> starts inside the convex hull of the first <em>m</em> control points and ends inside
 * the convex hull of the last <em>m</em> control points</li>
 * <li>A <b>uniform closed curve</b> is a closed curve; it starts and ends at the exact same point (not necessarily a
 * control point)</li>
 * </ul>
 * </p>
 * <p>
 * There is some techniques to have the curve interpolate a certain control point <code>P(i)</code>, however this
 * typically reduces the curve's smoothness around that point:
 * <ul>
 * <li>For the first point <code>P(0)</code> and/or the last point <code>P(L)</code>, use a <em>standard curve</em>
 * <li>Have <code>P(i-m+2), P(i-m+3), ... , P(i), P(i+1), ... , P(i+m-2)</code> lie on a straight line</li>
 * <li>Repeat the point <code>P(i)</code> <em>m</em> times (which is called the <em>multiplicity</em> of the control
 * point), for instance <code>P(i+m-1) = P(i+m-2) = ... = P(i)</code></li>
 * </ul>
 * </p>
 * <h4>Note on computational efficiency</h4>
 * <p>
 * The lower the order, the more computationally efficient the sampling process is. In this implementation, the time
 * complexity grows exponentially with the order. As this is particularly a problem for <em>Bézier</em> curves, the
 * factory method for a {@linkplain ApproximatingCurve2D#createStandardBezierCurve(List) standard Bézier curve} returns
 * an instance of the class {@link BezierCurve2D}. That class is highly optimized as long as the number of control
 * points does not exceed {@link BezierCurve2D#MAXIMUM_EFFICIENT_CONTROL_POINTS}.
 * </p>
 */
public class ApproximatingCurve2D extends AbstractCurve2D implements Cloneable {

	private List<Point2D> controlPoints;

	private int blendingFunctionOrder;

	private double[] knots;

	private double startT;

	private double endT;

	private ApproximatingCurve2D(List<Point2D> controlPoints, int blendingFunctionOrder, double[] knots, double startT,
			double endT) {
		this.controlPoints = controlPoints;
		this.blendingFunctionOrder = blendingFunctionOrder;
		this.knots = knots;
		this.startT = startT;
		this.endT = endT;
	}

	@Override
	protected ApproximatingCurve2D clone() {
		return new ApproximatingCurve2D(getControlPoints(), getBlendingFunctionOrder(), getKnots(), getStartT(),
				getEndT());
	}

	@Override
	public Point2D sample(double t) {
		double tp = projectT(t);
		double x = 0, y = 0;
		int L = getControlPoints().size() - 1;
		for (int k = 0; k <= L; k++) {
			Point2D cp = getControlPoints().get(k);
			double w = evaluateBlendingFunction(k, getBlendingFunctionOrder(), L, tp, getKnots());
			x += w * cp.getX();
			y += w * cp.getY();
		}
		return new Point2D(x, y);
	}

	@Override
	public Curve2D transform(TransformMatrix2D matrix) {
		if (!matrix.isAffine())
			throw new UnsupportedOperationException("This curve only supports affine transformations");
		ApproximatingCurve2D tCurve = clone();
		tCurve.setControlPoints(matrix.transform(getControlPoints()));
		return tCurve;
	}

	private double projectT(double t) {
		t = Math.min(Math.max(t, 0.0), 1.0);
		return getStartT() + t * (getEndT() - getStartT());
	}

	public static Curve2D createStandardBezierCurve(List<Point2D> controlPoints) {
		return new BezierCurve2D(controlPoints);
	}

	public static Curve2D createStandardCurve(List<Point2D> controlPoints) {
		return createStandardCurve(controlPoints, chooseBlendingFunctionOrder(controlPoints));
	}

	public static Curve2D createStandardCurve(List<Point2D> controlPoints, int blendingFunctionOrder) {
		int m = blendingFunctionOrder;
		int L = controlPoints.size() - 1;
		checkParameters(m, L);
		return new ApproximatingCurve2D(controlPoints, m, buildStandardKnots(m, L), 0, L - m + 2);
	}

	public static Curve2D createUniformOpenBezierCurve(List<Point2D> controlPoints) {
		return createUniformOpenCurve(controlPoints, controlPoints.size());
	}

	public static Curve2D createUniformOpenCurve(List<Point2D> controlPoints) {
		return createUniformOpenCurve(controlPoints, chooseBlendingFunctionOrder(controlPoints));
	}

	public static Curve2D createUniformOpenCurve(List<Point2D> controlPoints, int blendingFunctionOrder) {
		int m = blendingFunctionOrder;
		int L = controlPoints.size() - 1;
		checkParameters(m, L);
		return new ApproximatingCurve2D(controlPoints, m, buildEquispacedKnots(m, L), m - 1, L + 1);
	}

	public static Curve2D createUniformClosedBezierCurve(List<Point2D> controlPoints) {
		return createUniformClosedCurve(controlPoints, controlPoints.size());
	}

	public static Curve2D createUniformClosedCurve(List<Point2D> controlPoints) {
		return createUniformClosedCurve(controlPoints, chooseBlendingFunctionOrder(controlPoints));
	}

	public static Curve2D createUniformClosedCurve(List<Point2D> controlPoints, int blendingFunctionOrder) {
		int m = blendingFunctionOrder;
		int L = controlPoints.size() - 1;
		checkParameters(m, L);
		List<Point2D> derivedPoints = deriveControlPointsToCloseCurve(controlPoints, m);
		L = derivedPoints.size() - 1;
		return new ApproximatingCurve2D(derivedPoints, m, buildEquispacedKnots(m, L), m - 1, L + 1);
	}

	private static void checkParameters(int m, int L) {
		if (m < 2)
			throw new IllegalArgumentException("The order (" + m + ") should be at least 2");
		if (L + 1 < m)
			throw new IllegalArgumentException("Too few control points (" + (L + 1) + "), should be at least " + m);
	}

	private static int chooseBlendingFunctionOrder(List<Point2D> controlPoints) {
		// Order = 4 produces cubic blending functions, which are 2-smooth (1st and 2nd derivative is continuous)
		// Order = 3 produces quadratic blending functions, which are 1-smooth (1st derivative is continuous)
		// Order = 2 produces linear blending functions (a polyline defined by the control points)
		return Math.max(Math.min(controlPoints.size(), 4), 2);
	}

	private static List<Point2D> deriveControlPointsToCloseCurve(List<Point2D> controlPoints, int m) {
		List<Point2D> derivedPoints = new Vector<Point2D>(controlPoints.size() + m - 1);
		derivedPoints.addAll(controlPoints);
		for (int i = 0; i < m - 1; i++) {
			derivedPoints.add(controlPoints.get(i));
		}
		return derivedPoints;
	}

	private static double[] buildEquispacedKnots(int m, int L) {
		double[] knots = new double[L + m + 1];
		for (int i = 0; i <= L + m; i++) {
			knots[i] = i;
		}
		return knots;
	}

	private static double[] buildStandardKnots(int m, int L) {
		double[] knots = new double[L + m + 1];
		for (int i = 0; i <= L + m; i++) {
			if (i < m) {
				knots[i] = 0;
			} else if (i <= L) {
				knots[i] = i - m + 1;
			} else {
				knots[i] = L - m + 2;
			}
		}
		return knots;
	}

	private static double evaluateBlendingFunction(int k, int m, int L, double t, double[] knots) {
		if (m == 1) {
			if (t == knots[knots.length - 1] && k == L) {
				return 1.0;
			} else if (t >= knots[k] && t < knots[k + 1]) {
				return 1.0;
			} else {
				return 0.0;
			}
		}
		double sum = 0;
		double denom1 = knots[k + m - 1] - knots[k];
		if (denom1 != 0) {
			sum = (t - knots[k]) / denom1 * evaluateBlendingFunction(k, m - 1, L, t, knots);
		}
		double denom2 = knots[k + m] - knots[k + 1];
		if (denom2 != 0) {
			sum += (knots[k + m] - t) / denom2 * evaluateBlendingFunction(k + 1, m - 1, L, t, knots);
		}
		return sum;
	}

	private List<Point2D> getControlPoints() {
		return controlPoints;
	}

	private void setControlPoints(List<Point2D> controlPoints) {
		this.controlPoints = controlPoints;
	}

	private int getBlendingFunctionOrder() {
		return blendingFunctionOrder;
	}

	private double[] getKnots() {
		return knots;
	}

	private double getStartT() {
		return startT;
	}

	private double getEndT() {
		return endT;
	}

}