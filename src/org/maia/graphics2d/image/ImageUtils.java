package org.maia.graphics2d.image;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.maia.util.ColorUtils;

public class ImageUtils {

	private ImageUtils() {
	}

	public static ImageIcon getIcon(String resourcePath) {
		return new ImageIcon(ClassLoader.getSystemResource(resourcePath));
	}

	public static BufferedImage createImage(Dimension size, Color color) {
		return createImage(size.width, size.height, color);
	}

	public static BufferedImage createImage(Dimension size) {
		return createImage(size.width, size.height);
	}

	public static BufferedImage createImage(int width, int height, Color color) {
		BufferedImage image = createImage(width, height);
		clearWithUniformColor(image, color);
		return image;
	}

	public static BufferedImage createImage(int width, int height) {
		return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	public static BufferedImage readFromFile(String filePath) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(new File(filePath));
		} catch (IOException e) {
			System.err.println("Failed to read image from file path '" + filePath + "'");
		}
		return image;
	}

	public static BufferedImage readFromResource(String resourcePath) {
		BufferedImage image = null;
		try {
			return readFromStream(ClassLoader.getSystemResource(resourcePath).openStream());
		} catch (IOException e) {
			System.err.println("Failed to read image from resource path '" + resourcePath + "'");
		}
		return image;
	}

	public static BufferedImage readFromStream(InputStream stream) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(stream);
			stream.close();
		} catch (IOException e) {
			System.err.println("Failed to read image from stream");
			e.printStackTrace();
		}
		return image;
	}

	public static void writeToFile(BufferedImage image, String filePath) {
		String format = "png";
		int i = filePath.lastIndexOf('.');
		if (i > 0) {
			format = filePath.substring(i + 1).toLowerCase();
		}
		try {
			ImageIO.write(image, format, new File(filePath));
		} catch (IOException e) {
			System.err.println("Failed to write image to file path '" + filePath + "'");
		}
	}

	public static BufferedImage scale(BufferedImage image, double scale) {
		return scale(image, scale, scale);
	}

	public static BufferedImage scale(BufferedImage image, double sx, double sy) {
		int sw = (int) Math.floor(image.getWidth() * sx);
		int sh = (int) Math.floor(image.getHeight() * sy);
		BufferedImage scaledImage = new BufferedImage(sw, sh, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics2D = scaledImage.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		AffineTransform at = AffineTransform.getScaleInstance(sx, sy);
		graphics2D.drawRenderedImage(image, at);
		graphics2D.dispose();
		return scaledImage;
	}

	public static BufferedImage convertToBufferedImage(Image image) {
		BufferedImage bufImage = createImage(getSize(image));
		Graphics2D graphics2D = bufImage.createGraphics();
		graphics2D.drawImage(image, 0, 0, null);
		graphics2D.dispose();
		return bufImage;
	}

	public static BufferedImage convertToGrayscale(BufferedImage image) {
		Dimension size = getSize(image);
		BufferedImage grayscale = createImage(size);
		for (int y = 0; y < size.height; y++) {
			for (int x = 0; x < size.width; x++) {
				Color color = new Color(image.getRGB(x, y), true);
				double brightness = ColorUtils.getBrightness(color);
				int gray = (int) Math.round(brightness * 255);
				int alpha = color.getAlpha();
				int rgba = (alpha << 24) | (gray << 16) | (gray << 8) | gray;
				grayscale.setRGB(x, y, rgba);
			}
		}
		return grayscale;
	}

	public static BufferedImage combineByTransparency(BufferedImage frontImage, BufferedImage backImage) {
		Dimension frontSize = getSize(frontImage);
		Dimension backSize = getSize(backImage);
		if (!frontSize.equals(backSize))
			throw new IllegalArgumentException("Images are not the same size");
		BufferedImage image = createImage(frontSize);
		for (int y = 0; y < frontSize.height; y++) {
			for (int x = 0; x < frontSize.width; x++) {
				int frontRgba = frontImage.getRGB(x, y);
				int frontAlpha = frontRgba >>> 24;
				if (frontAlpha == 0xff) {
					image.setRGB(x, y, frontRgba); // fully opaque
				} else if (frontAlpha == 0x00) {
					image.setRGB(x, y, backImage.getRGB(x, y)); // fully transparent
				} else {
					Color frontColor = new Color(frontRgba, true);
					Color backColor = new Color(backImage.getRGB(x, y), true);
					image.setRGB(x, y, ColorUtils.combineByTransparency(frontColor, backColor).getRGB());
				}
			}
		}
		return image;
	}

	public static BufferedImage addPadding(BufferedImage image, Insets padding, Color padColor) {
		int width = getWidth(image);
		int height = getHeight(image);
		BufferedImage padImage = createImage(width + padding.left + padding.right,
				height + padding.top + padding.bottom);
		clearWithUniformColor(padImage, padColor);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				padImage.setRGB(padding.left + x, padding.top + y, image.getRGB(x, y));
			}
		}
		return padImage;
	}

	public static BufferedImage cropSides(BufferedImage image, Insets cropping) {
		BufferedImage croppedImage = null;
		Dimension size = getSize(image);
		int croppedWidth = size.width - cropping.left - cropping.right;
		int croppedHeight = size.height - cropping.top - cropping.bottom;
		if (croppedWidth > 0 && croppedHeight > 0) {
			croppedImage = createImage(croppedWidth, croppedHeight);
			Graphics2D graphics2D = croppedImage.createGraphics();
			graphics2D.drawImage(image, -cropping.left, -cropping.top, null);
			graphics2D.dispose();
		}
		return croppedImage;
	}

	public static void clearWithUniformColor(BufferedImage image, Color color) {
		clearWithUniformColor(image, color, 0, 0, getWidth(image), getHeight(image));
	}

	public static void clearWithUniformColor(BufferedImage image, Color color, int x, int y, int width, int height) {
		if (ColorUtils.isFullyOpaque(color)) {
			Graphics2D graphics2D = image.createGraphics();
			graphics2D.setColor(color);
			graphics2D.fillRect(x, y, width, height);
			graphics2D.dispose();
		} else {
			int rgb = color.getRGB();
			for (int yi = y; yi < y + height; yi++) {
				for (int xi = x; xi < x + width; xi++) {
					image.setRGB(xi, yi, rgb);
				}
			}
		}
	}

	public static void makeFullyTransparent(BufferedImage image) {
		makeFullyTransparent(image, Color.BLACK);
	}

	public static void makeFullyTransparent(BufferedImage image, Color baseColor) {
		clearWithUniformColor(image, new Color(baseColor.getRGB() & 0x00ffffff, true)); // alpha = 0
	}

	public static boolean isFullyOpaque(BufferedImage image) {
		int width = getWidth(image);
		int height = getHeight(image);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (image.getRGB(x, y) >>> 24 < 0xff)
					return false;
			}
		}
		return true;
	}

	public static Dimension getSize(Image image) {
		return new Dimension(getWidth(image), getHeight(image));
	}

	public static int getWidth(Image image) {
		int width = image.getWidth(null);
		if (width < 0) {
			final Dimension dimension = new Dimension();
			synchronized (dimension) {
				width = image.getWidth(new ImageObserver() {

					@Override
					public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
						if ((infoflags & ImageObserver.WIDTH) > 0) {
							synchronized (dimension) {
								dimension.width = width;
								dimension.notify();
							}
							return false;
						} else {
							return true;
						}
					}
				});
				while (width < 0) {
					try {
						dimension.wait();
					} catch (InterruptedException e) {
					}
					width = dimension.width;
				}
			}
		}
		return width;
	}

	public static int getHeight(Image image) {
		int height = image.getHeight(null);
		if (height < 0) {
			final Dimension dimension = new Dimension();
			synchronized (dimension) {
				height = image.getHeight(new ImageObserver() {

					@Override
					public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
						if ((infoflags & ImageObserver.HEIGHT) > 0) {
							synchronized (dimension) {
								dimension.height = height;
								dimension.notify();
							}
							return false;
						} else {
							return true;
						}
					}
				});
				while (height < 0) {
					try {
						dimension.wait();
					} catch (InterruptedException e) {
					}
					height = dimension.height;
				}
			}
		}
		return height;
	}

}