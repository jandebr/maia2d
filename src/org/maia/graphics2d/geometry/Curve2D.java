package org.maia.graphics2d.geometry;

import org.maia.graphics2d.transform.TransformMatrix2D;

public interface Curve2D {

	/**
	 * Samples this curve along its path
	 * 
	 * @param t
	 *            The relative distance along the path, with <code>t in [0,1]</code>
	 * @return A point along the curve's path
	 */
	Point2D sample(double t);

	/**
	 * Sequences this curve into a polyline
	 * 
	 * @param vertexCount
	 *            The number of vertices in the resulting polyline. Must be &gt;= 2
	 * @return The polyline. The first vertex of the polyline corresponds with <code>sample(0)</code> and the last
	 *         vertex of the polyline corresponds with <code>sample(1)</code>
	 * @throws IllegalArgumentException
	 *             When <code>vertexCount</code> &lt; 2
	 * @see #sample(double)
	 */
	PolyLine2D toPolyLine(int vertexCount);

	/**
	 * Transforms this curve into another curve
	 * 
	 * @param matrix
	 *            The transformation matrix
	 * @return The transformed curve
	 * @throws UnsupportedOperationException
	 *             If this curve does not support transformations
	 */
	Curve2D transform(TransformMatrix2D matrix);

}