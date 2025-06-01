package org.maia.graphics2d.geometry;

import java.util.Arrays;
import java.util.List;

/**
 * A convex polygon in XY coordinate space
 *
 * <p>
 * A polygon is made up of <em>n</em> vertices and <em>n</em> edges, connecting the vertices by line segments.
 * </p>
 * <p>
 * The polygon is <em>convex</em>, meaning the angle between adjacent edges must be &lt;= 180 degrees. This implies it
 * is a <em>simple</em> polygon, which does not intersect itself and has no holes.
 * </p>
 */
public class ConvexPolygon2D extends Polygon2D {

	private VerticesOrder order;

	public ConvexPolygon2D(Point2D... vertices) {
		this(Arrays.asList(vertices));
	}

	public ConvexPolygon2D(List<Point2D> vertices) {
		super(Polygon2D.deriveCentroid(vertices), vertices);
	}

	@Override
	public boolean contains(Point2D point) {
		VerticesOrder order = getVerticesOrder();
		if (VerticesOrder.COLLINEAR.equals(order)) {
			return getCollinearLineSegment().contains(point);
		} else {
			boolean cw = VerticesOrder.CLOCKWISE.equals(order);
			Point2D pi = getVertices().get(0);
			int n = getVertices().size();
			for (int i = 1; i <= n; i++) {
				Point2D pj = getVertices().get(i < n ? i : 0);
				double qx = point.getX() - pi.getX();
				double qy = point.getY() - pi.getY();
				double nx = cw ? pi.getY() - pj.getY() : pj.getY() - pi.getY();
				double ny = cw ? pj.getX() - pi.getX() : pi.getX() - pj.getX();
				if (qx * nx + qy * ny > 0)
					return false;
				pi = pj;
			}
			return true;
		}
	}

	private VerticesOrder getVerticesOrder() {
		if (order == null) {
			order = deriveVerticesOrder();
		}
		return order;
	}

	private VerticesOrder deriveVerticesOrder() {
		Point2D p0 = getVertices().get(0);
		Point2D p1 = getVertices().get(1);
		Point2D p2 = getVertices().get(2);
		double a = (p1.getX() - p0.getX()) * (p2.getY() - p0.getY());
		double b = (p1.getY() - p0.getY()) * (p2.getX() - p0.getX());
		if (a < b)
			return VerticesOrder.CLOCKWISE;
		else if (a > b)
			return VerticesOrder.COUNTER_CLOCKWISE;
		else
			return VerticesOrder.COLLINEAR;
	}

	private ClosedLineSegment2D getCollinearLineSegment() {
		Point2D p0 = getVertices().get(0);
		Point2D p1 = getVertices().get(1);
		Point2D p = null;
		boolean collinearOnX = p0.getX() == p1.getX();
		if ((collinearOnX && p0.getY() > p1.getY()) || (!collinearOnX && p0.getX() > p1.getX())) {
			p = p1;
			p1 = p0;
			p0 = p;
		}
		for (int i = 2; i < getVertices().size(); i++) {
			p = getVertices().get(i);
			if (collinearOnX) {
				if (p.getY() < p0.getY()) {
					p0 = p;
				} else if (p.getY() > p1.getY()) {
					p1 = p;
				}
			} else {
				if (p.getX() < p0.getX()) {
					p0 = p;
				} else if (p.getX() > p1.getX()) {
					p1 = p;
				}
			}
		}
		return new ClosedLineSegment2D(p0, p1);
	}

	private static enum VerticesOrder {

		CLOCKWISE,

		COUNTER_CLOCKWISE,

		COLLINEAR; // Edge case, not actually a convex polygon anymore

	}

}