package org.maia.graphics2d.geometry;

public class Radians {

	public static double degreesToRadians(double degrees) {
		return degrees / 180.0 * Math.PI;
	}

	public static double radiansToDegrees(double radians) {
		return radians / Math.PI * 180.0;
	}

}