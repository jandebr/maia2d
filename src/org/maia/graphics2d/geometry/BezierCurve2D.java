package org.maia.graphics2d.geometry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Standard Bézier curve defined by a sequence of two or more control points
 * 
 * <h4>Note on computational efficiency</h4>
 * <p>
 * The lower the number of control points, the more computationally efficient the sampling process is. This
 * implementation is however optimized as long as the number of control points does not exceed
 * {@link BezierCurve2D#MAXIMUM_EFFICIENT_CONTROL_POINTS}. A higher number of control points will exponentially increase
 * time complexity. For that reason, the number of control points cannot be higher than
 * {@link BezierCurve2D#MAXIMUM_CONTROL_POINTS}.
 * </p>
 */
public class BezierCurve2D extends AbstractCurve2D {

	private List<Point2D> controlPoints;

	public static final int MINIMUM_CONTROL_POINTS = 2;

	public static final int MAXIMUM_EFFICIENT_CONTROL_POINTS = 30;

	public static final int MAXIMUM_CONTROL_POINTS = 40;

	private static Map<Integer, Long> cachedBinomialCoefficients = new HashMap<Integer, Long>(100);

	public BezierCurve2D(List<Point2D> controlPoints) {
		checkParameters(controlPoints);
		this.controlPoints = controlPoints;
	}

	@Override
	public Point2D sample(double t) {
		t = Math.min(Math.max(t, 0.0), 1.0);
		double x = 0, y = 0;
		int L = getControlPoints().size() - 1;
		for (int k = 0; k <= L; k++) {
			Point2D cp = getControlPoints().get(k);
			double w = evaluateBernsteinPolynomial(k, L, t);
			x += w * cp.getX();
			y += w * cp.getY();
		}
		return new Point2D(x, y);
	}

	private static void checkParameters(List<Point2D> controlPoints) {
		int n = controlPoints.size();
		if (n < MINIMUM_CONTROL_POINTS)
			throw new IllegalArgumentException(
					"Too few control points (" + n + "), should be at least " + MINIMUM_CONTROL_POINTS);
		if (n > MAXIMUM_CONTROL_POINTS)
			throw new IllegalArgumentException(
					"Too many control points (" + n + "), should be at most " + MAXIMUM_CONTROL_POINTS);
	}

	private static double evaluateBernsteinPolynomial(int k, int L, double t) {
		if (k < 0 || k > L) {
			return 0.0;
		} else {
			Long cachedCoefficient = getCachedBinomialCoefficient(k, L);
			if (cachedCoefficient != null) {
				return cachedCoefficient.longValue() * Math.pow(1.0 - t, L - k) * Math.pow(t, k);
			} else if (L < MAXIMUM_EFFICIENT_CONTROL_POINTS) {
				long coefficient = computeBinomialCoefficient(k, L);
				cacheBinomialCoefficient(k, L, coefficient);
				return coefficient * Math.pow(1.0 - t, L - k) * Math.pow(t, k);
			} else {
				return (1.0 - t) * evaluateBernsteinPolynomial(k, L - 1, t)
						+ t * evaluateBernsteinPolynomial(k - 1, L - 1, t);
			}
		}
	}

	private static long computeBinomialCoefficient(int k, int L) {
		int max = Math.max(k, L - k);
		int min = Math.min(k, L - k);
		long n = 1L;
		for (int i = max + 1; i <= L; i++) {
			n *= i;
		}
		long d = 1L;
		for (int i = 2; i <= min; i++) {
			d *= i;
		}
		return n / d;
	}

	private static void cacheBinomialCoefficient(int k, int L, long coefficient) {
		getCachedBinomialCoefficients().put(getBinomialCoefficientCacheKey(k, L), coefficient);
	}

	private static Long getCachedBinomialCoefficient(int k, int L) {
		return getCachedBinomialCoefficients().get(getBinomialCoefficientCacheKey(k, L));
	}

	private static Integer getBinomialCoefficientCacheKey(int k, int L) {
		return (k << 16) | L;
	}

	private List<Point2D> getControlPoints() {
		return controlPoints;
	}

	private static Map<Integer, Long> getCachedBinomialCoefficients() {
		return cachedBinomialCoefficients;
	}

}