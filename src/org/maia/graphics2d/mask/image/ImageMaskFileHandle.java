package org.maia.graphics2d.mask.image;

import java.awt.Color;

import org.maia.graphics2d.texture.image.ImageTextureMapFileHandle;

public class ImageMaskFileHandle extends ImageTextureMapFileHandle {

	private Color maskColor;

	public ImageMaskFileHandle(String filePath, Color maskColor) {
		super(filePath);
		this.maskColor = maskColor;
	}

	@Override
	protected ImageMask resolve() {
		System.out.println("Loading mask '" + getFilePath() + "'");
		return new ImageMask(readImageFromFile(), getMaskColor());
	}

	@Override
	protected void dispose() {
		System.out.println("Disposing mask '" + getFilePath() + "'");
	}

	public Color getMaskColor() {
		return maskColor;
	}

}
