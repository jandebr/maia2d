package org.maia.graphics2d.image.pool;

import java.awt.Image;

import org.maia.graphics2d.image.ImageInfo;

public interface PooledImage {

	/**
	 * Returns an application-wide unique identifier for the image
	 * 
	 * @return The image identifier. Is guaranteed not <code>null</code>
	 */
	String getImageIdentifier();

	/**
	 * Return information about the image
	 * 
	 * @return An object containing image information
	 */
	ImageInfo getImageInfo();

	/**
	 * Returns an instance that can produce (load, retrieve, construct) the image
	 * 
	 * @return An image producer
	 */
	PooledImageProducer getImageProducer();

	/**
	 * Returns the image pool that this image belongs to
	 * 
	 * <p>
	 * An image pool has a maximum capacity for images that are kept in memory
	 * </p>
	 * 
	 * @return The image pool
	 */
	ImagePool getImagePool();

	/**
	 * Returns the image. Produces the image when not available (blocking call)
	 * 
	 * <p>
	 * This call is equivalent to <code>getImageProducer().produceImage(this)</code>
	 * </p>
	 * 
	 * @return The image
	 * @see #getImageProducer()
	 */
	Image getImage();

	/**
	 * Returns the image when directly available
	 * 
	 * @return The image, when directly available, otherwise returns <code>null</code>
	 */
	Image probeImage();

	/**
	 * Returns the image when directly available, otherwise requests the image to become available
	 * 
	 * @return The image, when directly available, otherwise returns <code>null</code> and produces the image
	 *         asynchronously in the background
	 */
	Image requestImage();

	/**
	 * Disposes this image and any resources used by it
	 */
	void disposeImage();

}