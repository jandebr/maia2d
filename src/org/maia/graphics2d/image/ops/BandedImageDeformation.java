package org.maia.graphics2d.image.ops;

import java.util.List;
import java.util.Vector;

import org.maia.graphics2d.image.ops.BandedImageDeformation.ImageBand;

public abstract class BandedImageDeformation<E extends ImageBand> extends NonLinearImageDeformation {

	private List<E> bands;

	protected BandedImageDeformation() {
		this.bands = new Vector<E>();
	}

	public static BandedImageDeformation<VerticalImageBand> createVerticalBandedImageDeformation() {
		return new VerticalBandedImageDeformation();
	}

	public static BandedImageDeformation<HorizontalImageBand> createHorizontalBandedImageDeformation() {
		return new HorizontalBandedImageDeformation();
	}

	public void addBand(E band) {
		getBands().add(band);
	}

	protected E getBand(int index) {
		return getBands().get(index);
	}

	protected List<E> getBands() {
		return bands;
	}

	private static class VerticalBandedImageDeformation extends BandedImageDeformation<VerticalImageBand>
			implements HorizontalCoordinateProjection {

		public VerticalBandedImageDeformation() {
			setHorizontalProjection(this);
		}

		@Override
		public float projectX(float x, float y, int width, int height) {
			int bandIndex = 0;
			int bandCount = getBands().size();
			int sourceOffset = 0;
			float bandOffset = 0;
			float bandWidth = getBand(bandIndex).getTargetWidth(y);
			while (x >= bandOffset + bandWidth && bandIndex < bandCount - 1) {
				sourceOffset += getBand(bandIndex).getSourceWidth();
				bandOffset += bandWidth;
				bandWidth = getBand(++bandIndex).getTargetWidth(y);
			}
			float r = (x - bandOffset) / bandWidth;
			float px = sourceOffset + r * getBand(bandIndex).getSourceWidth();
			return Math.min(Math.max(px, 0.5f), width - 0.5f);
		}

	}

	private static class HorizontalBandedImageDeformation extends BandedImageDeformation<HorizontalImageBand>
			implements VerticalCoordinateProjection {

		public HorizontalBandedImageDeformation() {
			setVerticalProjection(this);
		}

		@Override
		public float projectY(float x, float y, int width, int height) {
			int bandIndex = 0;
			int bandCount = getBands().size();
			int sourceOffset = 0;
			float bandOffset = 0;
			float bandHeight = getBand(bandIndex).getTargetHeight(x);
			while (y >= bandOffset + bandHeight && bandIndex < bandCount - 1) {
				sourceOffset += getBand(bandIndex).getSourceHeight();
				bandOffset += bandHeight;
				bandHeight = getBand(++bandIndex).getTargetHeight(x);
			}
			float r = (y - bandOffset) / bandHeight;
			float py = sourceOffset + r * getBand(bandIndex).getSourceHeight();
			return Math.min(Math.max(py, 0.5f), height - 0.5f);
		}

	}

	public static abstract class ImageBand {

		private int sourceSize;

		protected ImageBand(int sourceSize) {
			this.sourceSize = sourceSize;
		}

		public int getSourceSize() {
			return sourceSize;
		}

	}

	public static abstract class HorizontalImageBand extends ImageBand {

		protected HorizontalImageBand(int sourceSize) {
			super(sourceSize);
		}

		public int getSourceHeight() {
			return getSourceSize();
		}

		public abstract float getTargetHeight(float x);

	}

	public static class ConstantHorizontalImageBand extends HorizontalImageBand {

		private float targetHeight;

		public ConstantHorizontalImageBand(int sourceSize, float targetHeight) {
			super(sourceSize);
			this.targetHeight = targetHeight;
		}

		@Override
		public float getTargetHeight(float x) {
			return targetHeight;
		}

	}

	public static abstract class VerticalImageBand extends ImageBand {

		protected VerticalImageBand(int sourceSize) {
			super(sourceSize);
		}

		public int getSourceWidth() {
			return getSourceSize();
		}

		public abstract float getTargetWidth(float y);

	}

	public static class ConstantVerticalImageBand extends VerticalImageBand {

		private float targetWidth;

		public ConstantVerticalImageBand(int sourceSize, float targetWidth) {
			super(sourceSize);
			this.targetWidth = targetWidth;
		}

		@Override
		public float getTargetWidth(float y) {
			return targetWidth;
		}

	}

}