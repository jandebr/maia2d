package org.maia.graphics2d.transform;

import java.util.Arrays;

public class TransformMatrixBuilder2D {

	private double[] values;

	public TransformMatrixBuilder2D() {
		this.values = new double[9];
	}

	public TransformMatrixBuilder2D(double[] values) {
		this.values = Arrays.copyOf(values, values.length);
	}

	public TransformMatrixBuilder2D(TransformMatrix2D matrix) {
		this(matrix.getValues());
	}

	public TransformMatrix2D build() {
		return new TransformMatrix2D(getValues());
	}

	public double getValue(int row, int col) {
		return getValues()[row * 3 + col];
	}

	public TransformMatrixBuilder2D setValue(int row, int col, double value) {
		getValues()[row * 3 + col] = value;
		return this;
	}

	private double[] getValues() {
		return values;
	}

}