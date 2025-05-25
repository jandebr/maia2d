package org.maia.graphics2d.image;

public interface ImageInfo {

	/**
	 * Returns a title of the image
	 * 
	 * @return An image title. Can be <code>null</code> as this is optional
	 */
	String getTitle();

	/**
	 * Returns a description of the image
	 * 
	 * @return An image description. Can be <code>null</code> as this is optional
	 */
	String getDescription();

}