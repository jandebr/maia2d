package org.maia.graphics2d;

import java.text.NumberFormat;

public class Metrics2D {

	private static Metrics2D instance;

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
		builder.append("\tLine with line intersections: ").append(format(lineWithLineIntersections)).append("\n");
		builder.append("}");
		return builder.toString();
	}

	public static String format(long value) {
		return numberFormat.format(value);
	}

	public void resetCounters() {
		lineWithLineIntersections = 0;
	}

	public void incrementLineWithLineIntersections() {
		lineWithLineIntersections++;
	}

	public long getLineWithLineIntersections() {
		return lineWithLineIntersections;
	}

}