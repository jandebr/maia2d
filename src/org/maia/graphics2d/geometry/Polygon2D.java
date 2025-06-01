package org.maia.graphics2d.geometry;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
 * A simple polygon in XY coordinate space
 *
 * <p>
 * A polygon is made up of <em>n</em> vertices and <em>n</em> edges, connecting the vertices by line segments.
 * </p>
 * <p>
 * The polygon is <em>simple</em>, meaning it does not intersect itself and has no holes. A special case is a
 * <em>convex</em> polygon, as represented by the subclass {@link ConvexPolygon2D}
 * </p>
 */
public class Polygon2D {

	private List<Point2D> vertices;

	private List<ClosedLineSegment2D> edges;

	private Point2D insidePoint;

	private Point2D centroid;

	public Polygon2D(Point2D insidePoint, Point2D... vertices) {
		this(insidePoint, Arrays.asList(vertices));
	}

	public Polygon2D(Point2D insidePoint, List<Point2D> vertices) {
		this.insidePoint = insidePoint;
		this.vertices = vertices;
	}

	public boolean contains(Point2D point) {
		ClosedLineSegment2D line = new ClosedLineSegment2D(point, getInsidePoint());
		for (ClosedLineSegment2D edge : getEdges()) {
			Point2D p = line.intersect(edge);
			if (p != null && !p.equals(point))
				return false;
		}
		return true;
	}

	public List<ClosedLineSegment2D> getEdges() {
		if (edges == null) {
			edges = deriveEdges();
		}
		return edges;
	}

	private List<ClosedLineSegment2D> deriveEdges() {
		List<Point2D> vertices = getVertices();
		int n = vertices.size();
		List<ClosedLineSegment2D> edges = new Vector<ClosedLineSegment2D>(n);
		for (int i = 0; i < n; i++) {
			int j = (i + 1) % n;
			edges.add(new ClosedLineSegment2D(vertices.get(i), vertices.get(j)));
		}
		return edges;
	}

	public Point2D getCentroid() {
		if (centroid == null) {
			centroid = deriveCentroid(getVertices());
		}
		return centroid;
	}

	public static Point2D deriveCentroid(List<Point2D> vertices) {
		int n = vertices.size();
		double x = 0;
		double y = 0;
		for (int i = 0; i < n; i++) {
			x += vertices.get(i).getX();
			y += vertices.get(i).getY();
		}
		return new Point2D(x / n, y / n);
	}

	public List<Point2D> getVertices() {
		return vertices;
	}

	private Point2D getInsidePoint() {
		return insidePoint;
	}

}