package org.maia.graphics2d.geometry;

import java.text.NumberFormat;

import org.maia.graphics2d.Metrics2D;

public class Vector2D {

	private static NumberFormat formatter;

	private double x;

	private double y;

	static {
		formatter = NumberFormat.getNumberInstance();
		formatter.setMinimumFractionDigits(4);
		formatter.setMaximumFractionDigits(4);
	}

	public Vector2D(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Vector2D(Point2D from, Point2D to) {
		this(to.getX() - from.getX(), to.getY() - from.getY());
	}

	public static Vector2D fromPolarCoordinates(double angleInRadians) {
		return fromPolarCoordinates(angleInRadians, 1.0);
	}

	public static Vector2D fromPolarCoordinates(double angleInRadians, double distance) {
		double x = distance * Math.cos(angleInRadians);
		double y = distance * Math.sin(angleInRadians);
		return new Vector2D(x, y);
	}

	@Override
	public Vector2D clone() {
		return new Vector2D(getX(), getY());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);
		sb.append("[ ").append(formatter.format(getX())).append(" ; ").append(formatter.format(getY())).append(" ]");
		return sb.toString();
	}

	public double getMagnitude() {
		return Math.sqrt(getX() * getX() + getY() * getY());
	}

	public Vector2D getUnitVector() {
		Metrics2D.getInstance().incrementVectorNormalizations();
		double m = getMagnitude();
		return new Vector2D(getX() / m, getY() / m);
	}

	public void makeUnitVector() {
		Metrics2D.getInstance().incrementVectorNormalizations();
		double m = getMagnitude();
		setX(getX() / m);
		setY(getY() / m);
	}

	public void scale(double scale) {
		setX(scale * getX());
		setY(scale * getY());
	}

	public double dotProduct(Vector2D other) {
		Metrics2D.getInstance().incrementVectorDotProducts();
		return getX() * other.getX() + getY() * other.getY();
	}

	/**
	 * Returns the angle between this and a given vector.
	 * 
	 * @param other
	 *            The other vector
	 * @return The angle between this vector and the <code>other</code> vector, in radians between 0.0 and <i>Pi</i>.
	 */
	public double getAngleBetween(Vector2D other) {
		Metrics2D.getInstance().incrementVectorAnglesInBetween();
		double cosine = dotProduct(other) / (getMagnitude() * other.getMagnitude());
		return Math.acos(cosine);
	}

	/**
	 * Returns the angle between this unit vector and another given unit vector.
	 * 
	 * @param otherUnitVector
	 *            The other vector, assumed to have unit length
	 * @return The angle between this (assumed unit length) vector and the <code>otherUnitVector</code>, in radians
	 *         between 0.0 and <i>Pi</i>.
	 */
	public double getAngleBetweenUnitVectors(Vector2D otherUnitVector) {
		Metrics2D.getInstance().incrementVectorAnglesInBetween();
		return Math.acos(dotProduct(otherUnitVector));
	}

	/**
	 * Returns the angle of this vector
	 * 
	 * @return The angle of this vector, in radians, as a number between 0 and 2*pi
	 */
	public double getAngleInRadians() {
		double x = getX();
		double y = getY();
		if (y == 0) {
			return x >= 0 ? 0 : Math.PI;
		} else {
			double alpha = Math.acos(x / Math.sqrt(x * x + y * y));
			if (y < 0) {
				alpha = 2.0 * Math.PI - alpha;
			}
			return alpha;
		}
	}

	/**
	 * Returns a vector that is orthogonal to this vector, in anti-clockwise direction
	 * <p>
	 * This method is a shorthand for {@link #getOrthogonalVectorAntiClockwise()}
	 * </p>
	 * 
	 * @return An orthogonal vector
	 */
	public Vector2D getOrthogonalVector() {
		return getOrthogonalVectorAntiClockwise();
	}

	/**
	 * Returns a vector that is orthogonal to this vector, in anti-clockwise direction
	 * 
	 * @return An orthogonal vector
	 */
	public Vector2D getOrthogonalVectorAntiClockwise() {
		return new Vector2D(-getY(), getX());
	}

	/**
	 * Returns a vector that is orthogonal to this vector, in clockwise direction
	 * 
	 * @return An orthogonal vector
	 */
	public Vector2D getOrthogonalVectorClockwise() {
		return new Vector2D(getY(), -getX());
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
