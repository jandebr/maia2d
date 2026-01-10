package org.maia.graphics2d.geometry;

import java.util.List;
import java.util.Vector;

public class BSplineUtils {

	private BSplineUtils() {
	}

	public static List<Point2D> deriveControlPointsToCloseCurve(List<Point2D> controlPoints, int m) {
		List<Point2D> derivedPoints = new Vector<Point2D>(controlPoints.size() + m - 1);
		derivedPoints.addAll(controlPoints);
		for (int i = 0; i < m - 1; i++) {
			derivedPoints.add(controlPoints.get(i));
		}
		return derivedPoints;
	}

	public static double[] buildEquispacedKnots(int m, int L) {
		double[] knots = new double[L + m + 1];
		for (int i = 0; i <= L + m; i++) {
			knots[i] = i;
		}
		return knots;
	}

	public static double[] buildStandardKnots(int m, int L) {
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

	public static double evaluateBlendingFunction(int k, int m, int L, double t, double[] knots) {
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

}