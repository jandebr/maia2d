package org.maia.graphics2d.geometry;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class PolyLine2D {

	private List<Point2D> vertices;

	private List<ClosedLineSegment2D> edges;

	public PolyLine2D(Point2D... vertices) {
		this(Arrays.asList(vertices));
	}

	public PolyLine2D(List<Point2D> vertices) {
		this.vertices = vertices;
	}

	public Point2D intersect(Line2D line) {
		for (ClosedLineSegment2D edge : getEdges()) {
			Point2D p = edge.intersect(line);
			if (p != null)
				return p;
		}
		return null;
	}

	public Point2D intersectAtX(double x) {
		for (ClosedLineSegment2D edge : getEdges()) {
			Point2D p = edge.intersectAtX(x);
			if (p != null)
				return p;
		}
		return null;
	}

	public Point2D intersectAtY(double y) {
		for (ClosedLineSegment2D edge : getEdges()) {
			Point2D p = edge.intersectAtY(y);
			if (p != null)
				return p;
		}
		return null;
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
		List<ClosedLineSegment2D> edges = new Vector<ClosedLineSegment2D>(n - 1);
		for (int i = 0; i < n - 1; i++) {
			edges.add(new ClosedLineSegment2D(vertices.get(i), vertices.get(i + 1)));
		}
		return edges;
	}

	public List<Point2D> getVertices() {
		return vertices;
	}

}