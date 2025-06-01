package org.maia.graphics2d.image.ops;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.maia.util.ColorUtils;

public class ImageTransparencyOperation {

	private BufferedImage sourceImage;

	private float transparencyMultiplier;

	public ImageTransparencyOperation(BufferedImage sourceImage, float transparencyMultiplier) {
		this.sourceImage = sourceImage;
		this.transparencyMultiplier = transparencyMultiplier;
	}

	public BufferedImage apply() throws IOException {
		BufferedImage source = getSourceImage();
		int width = source.getWidth();
		int height = source.getHeight();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				Color color = new Color(source.getRGB(x, y), true);
				float transparency = ColorUtils.getTransparency(color);
				transparency = 1f - (1f - transparency) * (1f - getTransparencyMultiplier());
				Color newColor = ColorUtils.setTransparency(color, transparency);
				image.setRGB(x, y, newColor.getRGB());
			}
		}
		return image;
	}

	private BufferedImage getSourceImage() {
		return sourceImage;
	}

	private float getTransparencyMultiplier() {
		return transparencyMultiplier;
	}

}