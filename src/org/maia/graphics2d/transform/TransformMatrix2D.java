package org.maia.graphics2d.transform;

import java.util.List;
import java.util.Vector;

import org.maia.graphics2d.Metrics2D;
import org.maia.graphics2d.geometry.Point2D;

/**
 * A 2D transformation matrix.
 *
 * <p>
 * As it works with homogeneous coordinates, the matrix is 3x3.
 * </p>
 * 
 * <p>
 * Instances of this class are immutable. This allows composite transformation matrices to be computed and kept in
 * memory for efficient reuse. To create new or adjusted <code>TransformMatrix</code> instances, it is advised to use
 * <code>TransformMatrixBuilder2D</code>
 * </p>
 * 
 * @see TransformMatrixBuilder2D
 */
public class TransformMatrix2D {

	private double[] values;

	private TransformMatrix2D inverseMatrix;

	public TransformMatrix2D(double[] values) {
		if (values == null || values.length != 9)
			throw new IllegalArgumentException("The matrix should have 9 values as its dimensions are 3 by 3");
		this.values = values;
	}

	/**
	 * Multiplies this matrix with another matrix
	 * 
	 * @param matrix
	 *            The second operand
	 * @return The result of this matrix multiplied by the given <code>matrix</code>. In symbols, <code>result</code> =
	 *         <code>T</code> * <code>M</code> , where <code>T</code> denotes <code>this</code> and <code>M</code>
	 *         denotes <code>matrix</code>
	 */
	public TransformMatrix2D preMultiply(TransformMatrix2D matrix) {
		Metrics2D.getInstance().incrementMatrixMultiplications();
		double[] T = getValues();
		double[] M = matrix.getValues();
		// result R = T * M
		double[] R = new double[9];
		// First row
		R[0] = T[0] * M[0] + T[1] * M[3] + T[2] * M[6];
		R[1] = T[0] * M[1] + T[1] * M[4] + T[2] * M[7];
		R[2] = T[0] * M[2] + T[1] * M[5] + T[2] * M[8];
		// Second row
		R[3] = T[3] * M[0] + T[4] * M[3] + T[5] * M[6];
		R[4] = T[3] * M[1] + T[4] * M[4] + T[5] * M[7];
		R[5] = T[3] * M[2] + T[4] * M[5] + T[5] * M[8];
		// Third row
		R[6] = T[6] * M[0] + T[7] * M[3] + T[8] * M[6];
		R[7] = T[6] * M[1] + T[7] * M[4] + T[8] * M[7];
		R[8] = T[6] * M[2] + T[7] * M[5] + T[8] * M[8];
		return new TransformMatrix2D(R);
	}

	/**
	 * Multiplies another matrix with this matrix
	 * 
	 * @param matrix
	 *            The first operand
	 * @return The result of the given <code>matrix</code> multiplied by this matrix. In symbols, <code>result</code> =
	 *         <code>M</code> * <code>T</code> , where <code>T</code> denotes <code>this</code> and <code>M</code>
	 *         denotes <code>matrix</code>
	 */
	public TransformMatrix2D postMultiply(TransformMatrix2D matrix) {
		return matrix.preMultiply(this);
	}

	/**
	 * Transforms the given point
	 * 
	 * @param point
	 *            A point in 2D
	 * @return The transformation of the <code>point</code> under this matrix. In symbols, <code>result</code> =
	 *         <code>T</code> * <code>p</code> , where <code>T</code> denotes <code>this</code> and <code>p</code>
	 *         denotes <code>point</code>
	 */
	public Point2D transform(Point2D point) {
		Metrics2D.getInstance().incrementPointTransformations();
		double[] T = this.getValues();
		double px = point.getX();
		double py = point.getY();
		double pw = 1.0;
		double tx = T[0] * px + T[1] * py + T[2] * pw;
		double ty = T[3] * px + T[4] * py + T[5] * pw;
		double tw = T[6] * px + T[7] * py + T[8] * pw;
		if (tw != 1.0) {
			Metrics2D.getInstance().incrementPointNormalizations();
			tx /= tw;
			ty /= tw;
		}
		return new Point2D(tx, ty);
	}

	public List<Point2D> transform(List<Point2D> points) {
		List<Point2D> tPoints = new Vector<Point2D>(points.size());
		for (Point2D point : points) {
			tPoints.add(transform(point));
		}
		return tPoints;
	}

	public boolean isAffine() {
		double[] T = getValues();
		return T[6] == 0 && T[7] == 0 && T[8] == 1.0;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(256);
		for (int i = 0; i < 3; i++) {
			sb.append('[');
			sb.append(' ');
			for (int j = 0; j < 3; j++) {
				double v = getValue(i, j);
				sb.append(String.format("%10.3f", v));
			}
			sb.append(' ');
			sb.append(']');
			sb.append('\n');
		}
		return sb.toString();
	}

	protected double getValue(int row, int col) {
		return getValues()[row * 3 + col];
	}

	protected double[] getValues() {
		return values;
	}

	TransformMatrix2D getInverseMatrix() {
		return inverseMatrix;
	}

	void setInverseMatrix(TransformMatrix2D inverseMatrix) {
		this.inverseMatrix = inverseMatrix;
	}

}