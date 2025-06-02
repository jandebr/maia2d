package org.maia.graphics2d.transform;

import org.maia.graphics2d.Metrics2D;

public class Transformation2D {

	private static TransformMatrix2D IDENTITY_MATRIX;

	private Transformation2D() {
	}

	public static TransformMatrix2D getIdentityMatrix() {
		if (IDENTITY_MATRIX == null) {
			TransformMatrix2D I = createIdentityMatrix();
			I.setInverseMatrix(I);
			IDENTITY_MATRIX = I;
		}
		return IDENTITY_MATRIX;
	}

	private static TransformMatrix2D createIdentityMatrix() {
		TransformMatrixBuilder2D builder = new TransformMatrixBuilder2D();
		builder.setValue(0, 0, 1.0);
		builder.setValue(1, 1, 1.0);
		builder.setValue(2, 2, 1.0);
		return builder.build();
	}

	public static TransformMatrix2D getTranslationMatrix(double dx, double dy) {
		TransformMatrix2D M = createTranslationMatrix(dx, dy);
		TransformMatrix2D I = createTranslationMatrix(-dx, -dy);
		M.setInverseMatrix(I);
		I.setInverseMatrix(M);
		return M;
	}

	private static TransformMatrix2D createTranslationMatrix(double dx, double dy) {
		TransformMatrixBuilder2D builder = new TransformMatrixBuilder2D();
		builder.setValue(0, 0, 1.0);
		builder.setValue(0, 2, dx);
		builder.setValue(1, 1, 1.0);
		builder.setValue(1, 2, dy);
		builder.setValue(2, 2, 1.0);
		return builder.build();
	}

	public static TransformMatrix2D getScalingMatrix(double sx, double sy) {
		TransformMatrix2D M = createScalingMatrix(sx, sy);
		TransformMatrix2D I = createScalingMatrix(1.0 / sx, 1.0 / sy);
		M.setInverseMatrix(I);
		I.setInverseMatrix(M);
		return M;
	}

	private static TransformMatrix2D createScalingMatrix(double sx, double sy) {
		TransformMatrixBuilder2D builder = new TransformMatrixBuilder2D();
		builder.setValue(0, 0, sx);
		builder.setValue(1, 1, sy);
		builder.setValue(2, 2, 1.0);
		return builder.build();
	}

	public static TransformMatrix2D getRotationMatrix(double angleInRadians) {
		TransformMatrix2D M = createRotationMatrix(angleInRadians);
		TransformMatrix2D I = createRotationMatrix(-angleInRadians);
		M.setInverseMatrix(I);
		I.setInverseMatrix(M);
		return M;
	}

	private static TransformMatrix2D createRotationMatrix(double angleInRadians) {
		double c = Math.cos(angleInRadians);
		double s = Math.sin(angleInRadians);
		TransformMatrixBuilder2D builder = new TransformMatrixBuilder2D();
		builder.setValue(0, 0, c);
		builder.setValue(0, 1, -s);
		builder.setValue(1, 0, s);
		builder.setValue(1, 1, c);
		builder.setValue(2, 2, 1.0);
		return builder.build();
	}

	public static TransformMatrix2D getInverseMatrix(TransformMatrix2D matrix) throws MatrixInversionException {
		TransformMatrix2D inverse = matrix.getInverseMatrix();
		if (inverse == null) {
			inverse = createInverseMatrix(matrix);
			inverse.setInverseMatrix(matrix); // wiring for future reuse
			matrix.setInverseMatrix(inverse); // wiring for future reuse
		}
		return inverse;
	}

	private static TransformMatrix2D createInverseMatrix(TransformMatrix2D matrix) throws MatrixInversionException {
		Metrics2D.getInstance().incrementMatrixInversions();
		double det = computeDeterminant(matrix);
		if (det == 0)
			throw new MatrixInversionException();
		double[] T = matrix.getValues();
		TransformMatrixBuilder2D builder = new TransformMatrixBuilder2D();
		if (matrix.isAffine()) {
			builder.setValue(0, 0, T[4] * T[8] / det);
			builder.setValue(0, 1, -T[1] * T[8] / det);
			builder.setValue(0, 2, (T[1] * T[5] - T[2] * T[4]) / det);
			builder.setValue(1, 0, -T[3] * T[8] / det);
			builder.setValue(1, 1, T[0] * T[8] / det);
			builder.setValue(1, 2, -(T[0] * T[5] - T[2] * T[3]) / det);
			builder.setValue(2, 2, (T[0] * T[4] - T[1] * T[3]) / det);
		} else {
			builder.setValue(0, 0, (T[4] * T[8] - T[5] * T[7]) / det);
			builder.setValue(0, 1, -(T[1] * T[8] - T[2] * T[7]) / det);
			builder.setValue(0, 2, (T[1] * T[5] - T[2] * T[4]) / det);
			builder.setValue(1, 0, -(T[3] * T[8] - T[5] * T[6]) / det);
			builder.setValue(1, 1, (T[0] * T[8] - T[2] * T[6]) / det);
			builder.setValue(1, 2, -(T[0] * T[5] - T[2] * T[3]) / det);
			builder.setValue(2, 0, (T[3] * T[7] - T[4] * T[6]) / det);
			builder.setValue(2, 1, -(T[0] * T[7] - T[1] * T[6]) / det);
			builder.setValue(2, 2, (T[0] * T[4] - T[1] * T[3]) / det);
		}
		return builder.build();
	}

	private static double computeDeterminant(TransformMatrix2D matrix) {
		double[] T = matrix.getValues();
		if (matrix.isAffine()) {
			return T[0] * T[4] * T[8] - T[3] * T[1] * T[8];
		} else {
			return T[0] * (T[4] * T[8] - T[7] * T[5]) - T[3] * (T[1] * T[8] - T[7] * T[2])
					+ T[6] * (T[1] * T[5] - T[4] * T[2]);
		}
	}

	@SuppressWarnings("serial")
	public static class MatrixInversionException extends RuntimeException {

		public MatrixInversionException() {
			this("Cannot invert a singular matrix");
		}

		public MatrixInversionException(String message) {
			super(message);
		}

	}

}