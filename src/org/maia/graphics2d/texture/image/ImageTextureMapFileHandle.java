package org.maia.graphics2d.texture.image;

import java.awt.image.BufferedImage;

import org.maia.graphics2d.image.ImageUtils;
import org.maia.graphics2d.texture.TextureMap;
import org.maia.graphics2d.texture.TextureMapHandle;

public class ImageTextureMapFileHandle extends TextureMapHandle {

	private String filePath;

	private double scaleX;

	private double scaleY;

	public ImageTextureMapFileHandle(String filePath) {
		this(filePath, 1.0);
	}

	public ImageTextureMapFileHandle(String filePath, double scale) {
		this(filePath, scale, scale);
	}

	public ImageTextureMapFileHandle(String filePath, double scaleX, double scaleY) {
		this.filePath = filePath;
		this.scaleX = scaleX;
		this.scaleY = scaleY;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + filePath.hashCode();
		long temp;
		temp = Double.doubleToLongBits(scaleX);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(scaleY);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImageTextureMapFileHandle other = (ImageTextureMapFileHandle) obj;
		return getFilePath().equals(other.getFilePath()) && getScaleX() == other.getScaleX()
				&& getScaleY() == other.getScaleY();
	}

	@Override
	protected TextureMap resolve() {
		System.out.println("Loading texture map '" + getFilePath() + "'");
		return new ImageTextureMap(readImageFromFile());
	}

	@Override
	protected void dispose() {
		System.out.println("Disposing texture map '" + getFilePath() + "'");
	}

	protected BufferedImage readImageFromFile() {
		BufferedImage image = ImageUtils.readFromFile(getFilePath());
		if (getScaleX() != 1.0 || getScaleY() != 1.0) {
			image = ImageUtils.scale(image, getScaleX(), getScaleY());
		}
		return image;
	}

	public String getFilePath() {
		return filePath;
	}

	public double getScaleX() {
		return scaleX;
	}

	public double getScaleY() {
		return scaleY;
	}

}
