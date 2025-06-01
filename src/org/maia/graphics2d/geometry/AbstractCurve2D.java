package org.maia.graphics2d.geometry;

import java.util.List;
import java.util.Vector;

public abstract class AbstractCurve2D implements Curve2D {

	protected AbstractCurve2D() {
	}

	@Override
	public PolyLine2D toPolyLine(int vertexCount) {
		if (vertexCount < 2)
			throw new IllegalArgumentException("The vertex count must be at least 2 (" + vertexCount + ")");
		List<Point2D> vertices = new Vector<Point2D>(vertexCount);
		for (int i = 0; i < vertexCount; i++) {
			double t = i / (vertexCount - 1.0);
			vertices.add(sample(t));
		}
		return new PolyLine2D(vertices);
	}

}