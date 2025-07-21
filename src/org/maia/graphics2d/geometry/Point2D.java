package org.maia.graphics2d.geometry;

import java.text.NumberFormat;

public class Point2D {

	private static NumberFormat formatter;

	private double x;

	private double y;

	static {
		formatter = NumberFormat.getNumberInstance();
		formatter.setMinimumFractionDigits(4);
		formatter.setMaximumFractionDigits(4);
	}

	public Point2D() {
		this(0, 0);
	}

	public Point2D(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public static Point2D origin() {
		return new Point2D();
	}

	public static Point2D centerOfTwo(Point2D one, Point2D other) {
		return interpolateBetween(one, other, 0.5);
	}

	public static Point2D interpolateBetween(Point2D from, Point2D to, double r) {
		double s = 1.0 - r;
		double x = s * from.getX() + r * to.getX();
		double y = s * from.getY() + r * to.getY();
		return new Point2D(x, y);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(32);
		sb.append("[ ").append(formatter.format(getX())).append(" ; ").append(formatter.format(getY())).append(" ]");
		return sb.toString();
	}

	@Override
	public Point2D clone() {
		return new Point2D(getX(), getY());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Point2D other = (Point2D) obj;
		return getX() == other.getX() && getY() == other.getY();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(getX());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(getY());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	public Point2D plus(Vector2D vector) {
		double x = getX() + vector.getX();
		double y = getY() + vector.getY();
		return new Point2D(x, y);
	}

	public Point2D minus(Vector2D vector) {
		double x = getX() - vector.getX();
		double y = getY() - vector.getY();
		return new Point2D(x, y);
	}

	public Vector2D minus(Point2D other) {
		double x = getX() - other.getX();
		double y = getY() - other.getY();
		return new Vector2D(x, y);
	}

	public double distanceTo(Point2D other) {
		return Math.sqrt(squareDistanceTo(other));
	}

	public double squareDistanceTo(Point2D other) {
		double dx = getX() - other.getX();
		double dy = getY() - other.getY();
		return dx * dx + dy * dy;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

}