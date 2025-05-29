package org.maia.graphics2d.image.pool;

public class RetryablePooledImageProducerException extends Exception {

	public RetryablePooledImageProducerException() {
	}

	public RetryablePooledImageProducerException(String message) {
		super(message);
	}

	public RetryablePooledImageProducerException(Throwable cause) {
		super(cause);
	}

	public RetryablePooledImageProducerException(String message, Throwable cause) {
		super(message, cause);
	}

}