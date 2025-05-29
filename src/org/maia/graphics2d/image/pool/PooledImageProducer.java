package org.maia.graphics2d.image.pool;

import java.awt.Image;

public interface PooledImageProducer {

	/**
	 * Produces an image
	 * 
	 * @param pooledImage
	 *            A proxy for the image
	 * @return A materialized image, or <code>null</code> when the image cannot be produced
	 * @throws RetryablePooledImageProducerException
	 *             When the image failed to be produced, but could possibly succeed on a next attempt
	 */
	Image produceImage(PooledImage pooledImage) throws RetryablePooledImageProducerException;

}