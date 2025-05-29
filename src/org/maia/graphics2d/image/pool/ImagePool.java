package org.maia.graphics2d.image.pool;

import java.awt.Image;
import java.util.HashSet;
import java.util.Set;

import org.maia.util.KeyedCacheLRU;

public class ImagePool {

	private String name;

	private ImageCache imageCache;

	private Set<String> voidImageIdentifiers;

	public ImagePool(String name, int capacity) {
		this.name = name;
		this.imageCache = new ImageCache(capacity);
		this.voidImageIdentifiers = new HashSet<String>();
	}

	public synchronized void clear() {
		getImageCache().clear();
		getVoidImageIdentifiers().clear();
	}

	public synchronized void addVoidImage(String imageIdentifier) {
		getVoidImageIdentifiers().add(imageIdentifier);
		getImageCache().removeFromCache(imageIdentifier);
	}

	public synchronized void storeImage(String imageIdentifier, Image image) {
		if (image == null)
			throw new NullPointerException("image is null");
		getImageCache().storeInCache(imageIdentifier, image);
		getVoidImageIdentifiers().remove(imageIdentifier);
	}

	public synchronized void removeImage(String imageIdentifier) {
		getImageCache().removeFromCache(imageIdentifier);
	}

	public synchronized Image fetchImage(String imageIdentifier) {
		return getImageCache().fetchFromCache(imageIdentifier);
	}

	public synchronized boolean isVoidImage(String imageIdentifier) {
		return getVoidImageIdentifiers().contains(imageIdentifier);
	}

	public int getSize() {
		return getImageCache().size();
	}

	public int getCapacity() {
		return getImageCache().getCapacity();
	}

	public String getName() {
		return name;
	}

	private ImageCache getImageCache() {
		return imageCache;
	}

	private Set<String> getVoidImageIdentifiers() {
		return voidImageIdentifiers;
	}

	private class ImageCache extends KeyedCacheLRU<String, Image> {

		public ImageCache(int capacity) {
			super(capacity);
		}

		@Override
		protected void evicted(String imageIdentifier, Image image) {
			super.evicted(imageIdentifier, image);
			PooledImageLogger.log(imageIdentifier, "Evicted image from '" + getName() + "' pool");
		}

		@Override
		public void storeInCache(String imageIdentifier, Image image) {
			super.storeInCache(imageIdentifier, image);
			PooledImageLogger.log(imageIdentifier, "Added image to '" + getName() + "' pool");
		}

		@Override
		public void removeFromCache(String imageIdentifier) {
			super.removeFromCache(imageIdentifier);
			PooledImageLogger.log(imageIdentifier, "Removed image from '" + getName() + "' pool");
		}

	}

}