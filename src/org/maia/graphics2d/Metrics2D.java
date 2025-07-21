package org.maia.graphics2d;

import java.text.NumberFormat;

public class Metrics2D {

	private static Metrics2D instance;

	private long pointTransformations;

	private long pointNormalizations;

	private long matrixMultiplications;

	private long matrixInversions;

	private long vectorDotProducts;

	private long vectorNormalizations;

	private long vectorAnglesInBetween;

	private long lineWithLineIntersections;

	private static NumberFormat numberFormat;

	static {
		numberFormat = NumberFormat.getNumberInstance();
		numberFormat.setGroupingUsed(true);
	}

	private Metrics2D() {
		resetCounters();
	}

	public static Metrics2D getInstance() {
		if (instance == null) {
			setInstance(new Metrics2D());
		}
		return instance;
	}

	private static synchronized void setInstance(Metrics2D metrics) {
		if (instance == null) {
			instance = metrics;
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Metrics 2D {\n");
		builder.append("\tMatrix multiplications: ").append(format(matrixMultiplications)).append("\n");
		builder.append("\tMatrix inversions: ").append(format(matrixInversions)).append("\n");
		builder.append("\tPoint transformations: ").append(format(pointTransformations)).append("\n");
		builder.append("\tPoint normalizations: ").append(format(pointNormalizations)).append("\n");
		builder.append("\tVector dot products: ").append(format(vectorDotProducts)).append("\n");
		builder.append("\tVector normalizations: ").append(format(vectorNormalizations)).append("\n");
		builder.append("\tVector angles: ").append(format(vectorAnglesInBetween)).append("\n");
		builder.append("\t---\n");
		builder.append("\tLine with line intersections: ").append(format(lineWithLineIntersections)).append("\n");
		builder.append("}");
		return builder.toString();
	}

	public static String format(long value) {
		return numberFormat.format(value);
	}

	public void resetCounters() {
		pointTransformations = 0;
		pointNormalizations = 0;
		matrixMultiplications = 0;
		matrixInversions = 0;
		vectorDotProducts = 0;
		vectorNormalizations = 0;
		vectorAnglesInBetween = 0;
		lineWithLineIntersections = 0;
	}

	public void incrementPointTransformations() {
		pointTransformations++;
	}

	public void incrementPointNormalizations() {
		pointNormalizations++;
	}

	public void incrementMatrixMultiplications() {
		matrixMultiplications++;
	}

	public void incrementMatrixInversions() {
		matrixInversions++;
	}

	public void incrementVectorDotProducts() {
		vectorDotProducts++;
	}

	public void incrementVectorNormalizations() {
		vectorNormalizations++;
	}

	public void incrementVectorAnglesInBetween() {
		vectorAnglesInBetween++;
	}

	public void incrementLineWithLineIntersections() {
		lineWithLineIntersections++;
	}

	public long getPointTransformations() {
		return pointTransformations;
	}

	public long getPointNormalizations() {
		return pointNormalizations;
	}

	public long getMatrixMultiplications() {
		return matrixMultiplications;
	}

	public long getMatrixInversions() {
		return matrixInversions;
	}

	public long getVectorDotProducts() {
		return vectorDotProducts;
	}

	public long getVectorNormalizations() {
		return vectorNormalizations;
	}

	public long getVectorAnglesInBetween() {
		return vectorAnglesInBetween;
	}

	public long getLineWithLineIntersections() {
		return lineWithLineIntersections;
	}

}