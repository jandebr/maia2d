package org.maia.graphics2d.function;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.maia.graphics2d.geometry.LineSegment2D;
import org.maia.graphics2d.geometry.Point2D;
import org.maia.graphics2d.geometry.Rectangle2D;
import org.maia.util.Randomizer;

public class PiecewiseLinearFunction2D implements ProbabilityDensityFunction2D {

	private List<Point2D> points;

	private List<LineSegment2D> orderedLineSegments;

	private Rectangle2D bounds;

	private PiecewiseLinearFunction2D cumulativeDistributionFunction; // range [0,1]

	private PiecewiseLinearFunction2D transposedCumulativeDistributionFunction; // domain [0,1]

	private Randomizer randomizer;

	private int minimumCumulativeDistributionFunctionPoints = 1000;

	public PiecewiseLinearFunction2D() {
		this.points = new Vector<Point2D>();
		this.randomizer = new Randomizer();
	}

	public PiecewiseLinearFunction2D(List<Point2D> points) {
		this();
		for (Point2D point : points) {
			addPoint(point);
		}
	}

	public void addPoint(double x, double y) {
		addPoint(new Point2D(x, y));
	}

	public void addPoint(Point2D point) {
		Point2D previous = getPointAtX(point.getX());
		if (previous != null) {
			removePoint(previous);
		}
		getPoints().add(point);
		invalidate();
	}

	public void removePoint(Point2D point) {
		getPoints().remove(point);
		invalidate();
	}

	private Point2D getPointAtX(double x) {
		for (Point2D point : getPoints()) {
			if (point.getX() == x)
				return point;
		}
		return null;
	}

	private void invalidate() {
		setOrderedLineSegments(null);
		setBounds(null);
		invalidateCumulativeDistributionFunction();
	}

	private void invalidateCumulativeDistributionFunction() {
		setCumulativeDistributionFunction(null);
		setTransposedCumulativeDistributionFunction(null);
	}

	@Override
	public double evaluate(double x) {
		LineSegment2D segment = getLineSegmentAtX(x);
		if (segment != null) {
			return segment.intersectAtX(x).getY();
		} else {
			return Double.NaN;
		}
	}

	private LineSegment2D getLineSegmentAtX(double x) {
		LineSegment2D result = null;
		List<LineSegment2D> segments = getOrderedLineSegments();
		int n = segments.size();
		if (n > 0) {
			if (x >= segments.get(0).getP1().getX() && x <= segments.get(n - 1).getP2().getX()) {
				int i = 0;
				int j = n - 1;
				do {
					int k = (i + j) / 2;
					LineSegment2D segment = segments.get(k);
					double x1 = segment.getP1().getX();
					double x2 = segment.getP2().getX();
					if (x >= x1 && x <= x2) {
						result = segment;
					} else if (x < x1) {
						j = k - 1;
					} else if (x > x2) {
						i = k + 1;
					}
				} while (result == null && i <= j);
			}
		}
		return result;
	}

	private List<LineSegment2D> createOrderedLineSegments() {
		int n = getPoints().size() - 1;
		if (n > 0) {
			List<Point2D> orderedPoints = new Vector<Point2D>(getPoints());
			Collections.sort(orderedPoints, new Comparator<Point2D>() {

				@Override
				public int compare(Point2D p1, Point2D p2) {
					double x1 = p1.getX();
					double x2 = p2.getX();
					if (x1 < x2)
						return -1;
					else if (x1 > x2)
						return 1;
					else
						return 0;
				}

			});
			List<LineSegment2D> segments = new Vector<LineSegment2D>(n);
			for (int i = 0; i < n; i++) {
				segments.add(new LineSegment2D(orderedPoints.get(i), orderedPoints.get(i + 1)));
			}
			return segments;
		} else {
			return Collections.emptyList();
		}
	}

	private Rectangle2D createBounds() {
		Rectangle2D bounds = null;
		List<Point2D> points = getPoints();
		int n = points.size();
		if (n > 0) {
			double x = points.get(0).getX();
			double y = points.get(0).getY();
			bounds = new Rectangle2D(x, x, y, y);
			for (int i = 1; i < n; i++) {
				bounds.expandToContain(points.get(i));
			}
		}
		return bounds;
	}

	private PiecewiseLinearFunction2D createCumulativeDistributionFunction() {
		List<LineSegment2D> segments = getOrderedLineSegments();
		int n = segments.size();
		if (n > 0) {
			int minPts = getMinimumCumulativeDistributionFunctionPoints();
			List<Point2D> points = new Vector<Point2D>(minPts + n + 1);
			double x1 = segments.get(0).getP1().getX();
			double x2 = segments.get(n - 1).getP2().getX();
			double width = x2 - x1;
			double w = 0;
			points.add(new Point2D(x1, 0));
			for (int i = 0; i < n; i++) {
				LineSegment2D segment = segments.get(i);
				x1 = segment.getP1().getX();
				x2 = segment.getP2().getX();
				int m = Math.max((int) Math.round((x2 - x1) / width * minPts), 1);
				double dx = (x2 - x1) / m;
				for (int j = 0; j < m; j++) {
					double x = x1 + dx * (j + 0.5);
					double y = Math.max(segment.intersectAtX(x).getY(), 0);
					w += dx * y;
					points.add(new Point2D(x + dx / 2.0, w));
				}
			}
			// Normalize
			if (w > 0) {
				for (Point2D point : points) {
					point.setY(point.getY() / w);
				}
			}
			return new PiecewiseLinearFunction2D(points);
		} else {
			return new PiecewiseLinearFunction2D();
		}
	}

	private PiecewiseLinearFunction2D transposeFunction(PiecewiseLinearFunction2D ft) {
		PiecewiseLinearFunction2D transposedFt = new PiecewiseLinearFunction2D();
		for (Point2D point : ft.getPoints()) {
			transposedFt.addPoint(new Point2D(point.getY(), point.getX()));
		}
		return transposedFt;
	}

	@Override
	public double sample() {
		double r = getRandomizer().drawDoubleUnitNumber();
		return getTransposedCumulativeDistributionFunction().evaluate(r);
	}

	@Override
	public PiecewiseLinearFunction2D getCumulativeDistributionFunction() {
		if (cumulativeDistributionFunction == null) {
			cumulativeDistributionFunction = createCumulativeDistributionFunction();
		}
		return cumulativeDistributionFunction;
	}

	private void setCumulativeDistributionFunction(PiecewiseLinearFunction2D ft) {
		this.cumulativeDistributionFunction = ft;
	}

	public List<Point2D> getPoints() {
		return points;
	}

	public List<LineSegment2D> getOrderedLineSegments() {
		if (orderedLineSegments == null) {
			orderedLineSegments = createOrderedLineSegments();
		}
		return orderedLineSegments;
	}

	private void setOrderedLineSegments(List<LineSegment2D> segments) {
		this.orderedLineSegments = segments;
	}

	public Rectangle2D getBounds() {
		if (bounds == null) {
			bounds = createBounds();
		}
		return bounds;
	}

	private void setBounds(Rectangle2D bounds) {
		this.bounds = bounds;
	}

	private PiecewiseLinearFunction2D getTransposedCumulativeDistributionFunction() {
		if (transposedCumulativeDistributionFunction == null) {
			transposedCumulativeDistributionFunction = transposeFunction(getCumulativeDistributionFunction());
		}
		return transposedCumulativeDistributionFunction;
	}

	private void setTransposedCumulativeDistributionFunction(PiecewiseLinearFunction2D ft) {
		this.transposedCumulativeDistributionFunction = ft;
	}

	public Randomizer getRandomizer() {
		return randomizer;
	}

	public void setRandomizer(Randomizer randomizer) {
		this.randomizer = randomizer;
	}

	public int getMinimumCumulativeDistributionFunctionPoints() {
		return minimumCumulativeDistributionFunctionPoints;
	}

	public void setMinimumCumulativeDistributionFunctionPoints(int n) {
		if (n <= 0) {
			throw new IllegalArgumentException("Must be strictly positive (" + n + ")");
		} else if (n != getMinimumCumulativeDistributionFunctionPoints()) {
			this.minimumCumulativeDistributionFunctionPoints = n;
			invalidateCumulativeDistributionFunction();
		}
	}

}