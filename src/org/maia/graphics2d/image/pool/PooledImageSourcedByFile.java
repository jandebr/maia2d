package org.maia.graphics2d.image.pool;

import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.maia.graphics2d.image.ImageInfo;
import org.maia.graphics2d.image.ImageInfoImpl;

public class PooledImageSourcedByFile extends AbstractPooledImage {

	private File imageSourceFile;

	private static PooledImageProducer imageLoader = new ImageFileLoader();

	public PooledImageSourcedByFile(File imageSourceFile, ImagePool imagePool) {
		this(imageSourceFile, imagePool, createImageIdentifierFor(imageSourceFile));
	}

	public PooledImageSourcedByFile(File imageSourceFile, ImagePool imagePool, ImageInfo imageInfo) {
		this(imageSourceFile, imagePool, createImageIdentifierFor(imageSourceFile), imageInfo);
	}

	public PooledImageSourcedByFile(File imageSourceFile, ImagePool imagePool, String imageIdentifier) {
		this(imageSourceFile, imagePool, imageIdentifier, new ImageInfoImpl());
	}

	public PooledImageSourcedByFile(File imageSourceFile, ImagePool imagePool, String imageIdentifier,
			ImageInfo imageInfo) {
		super(imageIdentifier, imagePool, imageInfo);
		this.imageSourceFile = imageSourceFile;
	}

	private static String createImageIdentifierFor(File imageSourceFile) {
		try {
			return imageSourceFile.getCanonicalPath();
		} catch (IOException e) {
			return imageSourceFile.getAbsolutePath();
		}
	}

	@Override
	public PooledImageProducer getImageProducer() {
		return imageLoader;
	}

	public File getImageSourceFile() {
		return imageSourceFile;
	}

	private static class ImageFileLoader implements PooledImageProducer {

		public ImageFileLoader() {
		}

		@Override
		public Image produceImage(PooledImage pooledImage) {
			Image image = null;
			if (pooledImage instanceof PooledImageSourcedByFile) {
				File file = ((PooledImageSourcedByFile) pooledImage).getImageSourceFile();
				try {
					image = ImageIO.read(file);
				} catch (Exception e) {
					System.err.println("Failed loading image from file '" + file.getPath() + "' " + e.getMessage());
				}
			}
			return image;
		}

	}

}