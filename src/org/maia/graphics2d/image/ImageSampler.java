package org.maia.graphics2d.image;

import java.awt.Color;
import java.awt.image.BufferedImage;

public abstract class ImageSampler {

	private BufferedImage image;

	private int imageWidth;

	private int imageHeight;

	protected ImageSampler(BufferedImage image) {
		this.image = image;
		this.imageWidth = ImageUtils.getWidth(image);
		this.imageHeight = ImageUtils.getHeight(image);
	}

	/**
	 * Samples the image at the specified coordinates. The coordinates need not to coincide with the center of a single
	 * pixel
	 * 
	 * @param sx
	 *            The x coordinate, in the range [0.5f, image width - 0.5f]
	 * @param sy
	 *            The y coordinate, in the range [0.5f, image height - 0.5f]
	 * @return The image sample, packed as an integer in the ARGB color model
	 */
	public abstract int sampleRGB(float sx, float sy);

	public Color sampleColor(float sx, float sy) {
		return new Color(sampleRGB(sx, sy), true);
	}

	public static ImageSampler createDefaultImageSampler(BufferedImage image) {
		return new InterpolatingImageSampler(image);
	}

	public BufferedImage getImage() {
		return image;
	}

	protected int getImageWidth() {
		return imageWidth;
	}

	protected int getImageHeight() {
		return imageHeight;
	}

	private static class InterpolatingImageSampler extends ImageSampler {

		private int cx, cy;

		private int minDx, maxDx;

		private int minDy, maxDy;

		private float cWeightX, cWeightY;

		public InterpolatingImageSampler(BufferedImage image) {
			super(image);
		}

		@Override
		public int sampleRGB(float sx, float sy) {
			update(sx, sy);
			int width = getImageWidth();
			int height = getImageHeight();
			float alpha = 0f;
			float red = 0f;
			float green = 0f;
			float blue = 0f;
			for (int dy = minDy; dy <= maxDy; dy++) {
				int y = Math.min(Math.max(cy + dy, 0), height - 1);
				float wy = dy == 0 ? cWeightY : 1f - cWeightY;
				for (int dx = minDx; dx <= maxDx; dx++) {
					int x = Math.min(Math.max(cx + dx, 0), width - 1);
					int argb = getImage().getRGB(x, y);
					float wx = dx == 0 ? cWeightX : 1f - cWeightX;
					float w = wy * wx;
					alpha += w * ((argb & 0xff000000) >>> 24);
					red += w * ((argb & 0x00ff0000) >>> 16);
					green += w * ((argb & 0x0000ff00) >>> 8);
					blue += w * (argb & 0x000000ff);
				}
			}
			int alphaInt = Math.min(Math.round(alpha), 255);
			int redInt = Math.min(Math.round(red), 255);
			int greenInt = Math.min(Math.round(green), 255);
			int blueInt = Math.min(Math.round(blue), 255);
			return (alphaInt << 24) | (redInt << 16) | (greenInt << 8) | blueInt;
		}

		private void update(float sx, float sy) {
			this.cx = (int) Math.floor(sx);
			this.cy = (int) Math.floor(sy);
			float dx = sx - cx - 0.5f;
			if (dx < 0f) {
				this.minDx = -1;
				this.maxDx = 0;
			} else if (dx > 0f) {
				this.minDx = 0;
				this.maxDx = 1;
			} else {
				this.minDx = 0;
				this.maxDx = 0;
			}
			float dy = sy - cy - 0.5f;
			if (dy < 0f) {
				this.minDy = -1;
				this.maxDy = 0;
			} else if (dy > 0f) {
				this.minDy = 0;
				this.maxDy = 1;
			} else {
				this.minDy = 0;
				this.maxDy = 0;
			}
			this.cWeightX = 1f - Math.abs(dx);
			this.cWeightY = 1f - Math.abs(dy);
		}

	}

}