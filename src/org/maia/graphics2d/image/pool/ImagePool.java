package org.maia.graphics2d.image.pool;

import java.awt.Image;

import org.maia.util.KeyedCacheLRU;

public class ImagePool extends KeyedCacheLRU<String, Image> {

	private String name;

	public ImagePool(String name, int capacity) {
		super(capacity);
		this.name = name;
	}

	@Override
	protected void evicted(String imageIdentifier, Image image) {
		super.evicted(imageIdentifier, image);
		PooledImageLogger.log(imageIdentifier, "Evicted image '" + imageIdentifier + "' from pool '" + getName() + "'");
	}

	@Override
	public void storeInCache(String imageIdentifier, Image image) {
		super.storeInCache(imageIdentifier, image);
		PooledImageLogger.log(imageIdentifier, "Added image '" + imageIdentifier + "' to pool '" + getName() + "'");
	}

	@Override
	public void removeFromCache(String imageIdentifier) {
		super.removeFromCache(imageIdentifier);
		PooledImageLogger.log(imageIdentifier, "Removed image '" + imageIdentifier + "' from pool '" + getName() + "'");
	}

	public String getName() {
		return name;
	}

}