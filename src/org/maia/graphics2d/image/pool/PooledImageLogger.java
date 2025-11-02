package org.maia.graphics2d.image.pool;

public class PooledImageLogger {

	public static boolean logInfoEnabled = false;

	public static boolean logDebugEnabled = false;

	public static void logInfo(String imageIdentifier, String message) {
		if (logInfoEnabled) {
			log(message + " [" + imageIdentifier + "]");
		}
	}

	public static void logDebug(String imageIdentifier, String message) {
		if (logDebugEnabled) {
			log(message + " [" + imageIdentifier + "]");
		}
	}

	private static void log(String message) {
		System.out.println(message);
	}

}