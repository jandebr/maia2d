package org.maia.graphics2d.mask.image;

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.maia.graphics2d.mask.Mask;
import org.maia.graphics2d.texture.image.ImageTextureMap;

public class ImageMask extends ImageTextureMap implements Mask {

	private Color maskColor;

	public ImageMask(BufferedImage image, Color maskColor) {
		super(image);
		this.maskColor = maskColor;
	}

	@Override
	public boolean isMasked(double x, double y) {
		Color c = sampleColor(x, y);
		if (c == null) {
			return true;
		} else {
			return c.equals(getMaskColor());
		}
	}

	public Color getMaskColor() {
		return maskColor;
	}

}
