package org.maia.graphics2d.image.pool;

public class PooledImageLogger {

	public static boolean logEnabled = false;

	public static void log(String imageIdentifier, String message) {
		log(message + " [" + imageIdentifier + "]");
	}

	public static void log(String message) {
		if (logEnabled) {
			System.out.println(message);
		}
	}

}