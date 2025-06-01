package org.maia.graphics2d.image.ops;

import java.util.List;
import java.util.Vector;

import org.maia.graphics2d.geometry.Curve2D;
import org.maia.graphics2d.geometry.Point2D;
import org.maia.graphics2d.geometry.PolyLine2D;
import org.maia.graphics2d.image.ops.BandedImageDeformation.ImageBand;

public abstract class BandedImageDeformation<E extends ImageBand> extends NonLinearImageDeformation {

	private List<E> bands;

	protected BandedImageDeformation() {
		this.bands = new Vector<E>();
	}

	public static BandedImageDeformation<VerticalImageBand> createVerticalBandedImageDeformation() {
		return new VerticalBandedImageDeformation();
	}

	public static BandedImageDeformation<VerticalImageBand> createCurvedVerticalBandedImageDeformation(
			int totalSourceWidth, float relativeDistance, Curve2D separatorCurve) {
		int sourceWidthLeft = Math.round(relativeDistance * totalSourceWidth);
		int sourceWidthRight = totalSourceWidth - sourceWidthLeft;
		return createCurvedVerticalBandedImageDeformation(new int[] { sourceWidthLeft, sourceWidthRight },
				new Curve2D[] { separatorCurve });
	}

	public static BandedImageDeformation<VerticalImageBand> createCurvedVerticalBandedImageDeformation(
			int[] sourceWidths, Curve2D[] separatorCurves) {
		int s = separatorCurves.length;
		if (s == 0)
			throw new IllegalArgumentException("No separator curves provided");
		int n = sourceWidths.length;
		if (s != n - 1)
			throw new IllegalArgumentException(
					"Non matching number of separator curves provided (" + s + " instead of " + (n - 1) + ")");
		BandedImageDeformation<VerticalImageBand> deformation = createVerticalBandedImageDeformation();
		for (int i = 0; i < n; i++) {
			Curve2D leftEdge = i > 0 ? separatorCurves[i - 1] : null;
			Curve2D rightEdge = i < n - 1 ? separatorCurves[i] : null;
			deformation.addBand(new CurvedVerticalImageBand(sourceWidths[i], leftEdge, rightEdge));
		}
		return deformation;
	}

	public static BandedImageDeformation<HorizontalImageBand> createHorizontalBandedImageDeformation() {
		return new HorizontalBandedImageDeformation();
	}

	public static BandedImageDeformation<HorizontalImageBand> createCurvedHorizontalBandedImageDeformation(
			int totalSourceHeight, float relativeDistance, Curve2D separatorCurve) {
		int sourceHeightTop = Math.round(relativeDistance * totalSourceHeight);
		int sourceHeightBottom = totalSourceHeight - sourceHeightTop;
		return createCurvedHorizontalBandedImageDeformation(new int[] { sourceHeightTop, sourceHeightBottom },
				new Curve2D[] { separatorCurve });
	}

	public static BandedImageDeformation<HorizontalImageBand> createCurvedHorizontalBandedImageDeformation(
			int[] sourceHeights, Curve2D[] separatorCurves) {
		int s = separatorCurves.length;
		if (s == 0)
			throw new IllegalArgumentException("No separator curves provided");
		int n = sourceHeights.length;
		if (s != n - 1)
			throw new IllegalArgumentException(
					"Non matching number of separator curves provided (" + s + " instead of " + (n - 1) + ")");
		BandedImageDeformation<HorizontalImageBand> deformation = createHorizontalBandedImageDeformation();
		for (int i = 0; i < n; i++) {
			Curve2D topEdge = i > 0 ? separatorCurves[i - 1] : null;
			Curve2D bottomEdge = i < n - 1 ? separatorCurves[i] : null;
			deformation.addBand(new CurvedHorizontalImageBand(sourceHeights[i], topEdge, bottomEdge));
		}
		return deformation;
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
			float bandWidth = getBand(bandIndex).getTargetWidth(y, width, height);
			while (x >= bandOffset + bandWidth && bandIndex < bandCount - 1) {
				sourceOffset += getBand(bandIndex).getSourceWidth();
				bandOffset += bandWidth;
				bandWidth = getBand(++bandIndex).getTargetWidth(y, width, height);
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
			float bandHeight = getBand(bandIndex).getTargetHeight(x, width, height);
			while (y >= bandOffset + bandHeight && bandIndex < bandCount - 1) {
				sourceOffset += getBand(bandIndex).getSourceHeight();
				bandOffset += bandHeight;
				bandHeight = getBand(++bandIndex).getTargetHeight(x, width, height);
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

	public static abstract class VerticalImageBand extends ImageBand {

		protected VerticalImageBand(int sourceWidth) {
			super(sourceWidth);
		}

		public int getSourceWidth() {
			return getSourceSize();
		}

		public abstract float getTargetWidth(float y, int width, int height);

	}

	public static class ConstantVerticalImageBand extends VerticalImageBand {

		private float targetWidth;

		public ConstantVerticalImageBand(int sourceWidth, float targetWidth) {
			super(sourceWidth);
			this.targetWidth = targetWidth;
		}

		@Override
		public float getTargetWidth(float y, int width, int height) {
			return targetWidth;
		}

	}

	public static class CurvedVerticalImageBand extends VerticalImageBand {

		private Curve2D leftEdge;

		private PolyLine2D leftEdgeSequenced;

		private Curve2D rightEdge;

		private PolyLine2D rightEdgeSequenced;

		private float previousY = Float.NaN;

		private float previousXleft;

		private float previousXright;

		public CurvedVerticalImageBand(int sourceWidth, Curve2D leftEdge, Curve2D rightEdge) {
			super(sourceWidth);
			this.leftEdge = leftEdge;
			this.rightEdge = rightEdge;
		}

		@Override
		public float getTargetWidth(float y, int width, int height) {
			float xLeft = 0, xRight = width;
			if (y == previousY) {
				xLeft = previousXleft;
				xRight = previousXright;
			} else {
				PolyLine2D leftEdgeSeq = getLeftEdgeSequenced(height + 1);
				if (leftEdgeSeq != null) {
					Point2D p = leftEdgeSeq.intersectAtY(y);
					if (p != null) {
						xLeft = (float) p.getX();
					}
				}
				PolyLine2D rightEdgeSeq = getRightEdgeSequenced(height + 1);
				if (rightEdgeSeq != null) {
					Point2D p = rightEdgeSeq.intersectAtY(y);
					if (p != null) {
						xRight = (float) p.getX();
					}
				}
				previousY = y;
				previousXleft = xLeft;
				previousXright = xRight;
			}
			return Math.max(xRight - xLeft, 1f);
		}

		private PolyLine2D getLeftEdgeSequenced(int vertexCount) {
			if (leftEdgeSequenced == null && getLeftEdge() != null) {
				leftEdgeSequenced = getLeftEdge().toPolyLine(vertexCount);
			}
			return leftEdgeSequenced;
		}

		private PolyLine2D getRightEdgeSequenced(int vertexCount) {
			if (rightEdgeSequenced == null && getRightEdge() != null) {
				rightEdgeSequenced = getRightEdge().toPolyLine(vertexCount);
			}
			return rightEdgeSequenced;
		}

		public Curve2D getLeftEdge() {
			return leftEdge;
		}

		public Curve2D getRightEdge() {
			return rightEdge;
		}

	}

	public static abstract class HorizontalImageBand extends ImageBand {

		protected HorizontalImageBand(int sourceHeight) {
			super(sourceHeight);
		}

		public int getSourceHeight() {
			return getSourceSize();
		}

		public abstract float getTargetHeight(float x, int width, int height);

	}

	public static class ConstantHorizontalImageBand extends HorizontalImageBand {

		private float targetHeight;

		public ConstantHorizontalImageBand(int sourceHeight, float targetHeight) {
			super(sourceHeight);
			this.targetHeight = targetHeight;
		}

		@Override
		public float getTargetHeight(float x, int width, int height) {
			return targetHeight;
		}

	}

	public static class CurvedHorizontalImageBand extends HorizontalImageBand {

		private Curve2D topEdge;

		private PolyLine2D topEdgeSequenced;

		private Curve2D bottomEdge;

		private PolyLine2D bottomEdgeSequenced;

		private float previousX = Float.NaN;

		private float previousYtop;

		private float previousYbottom;

		public CurvedHorizontalImageBand(int sourceHeight, Curve2D topEdge, Curve2D bottomEdge) {
			super(sourceHeight);
			this.topEdge = topEdge;
			this.bottomEdge = bottomEdge;
		}

		@Override
		public float getTargetHeight(float x, int width, int height) {
			float yTop = 0, yBottom = height;
			if (x == previousX) {
				yTop = previousYtop;
				yBottom = previousYbottom;
			} else {
				PolyLine2D topEdgeSeq = getTopEdgeSequenced(width + 1);
				if (topEdgeSeq != null) {
					Point2D p = topEdgeSeq.intersectAtX(x);
					if (p != null) {
						yTop = (float) p.getY();
					}
				}
				PolyLine2D bottomEdgeSeq = getBottomEdgeSequenced(width + 1);
				if (bottomEdgeSeq != null) {
					Point2D p = bottomEdgeSeq.intersectAtX(x);
					if (p != null) {
						yBottom = (float) p.getY();
					}
				}
				previousX = x;
				previousYtop = yTop;
				previousYbottom = yBottom;
			}
			return Math.max(yBottom - yTop, 1f);
		}

		private PolyLine2D getTopEdgeSequenced(int vertexCount) {
			if (topEdgeSequenced == null && getTopEdge() != null) {
				topEdgeSequenced = getTopEdge().toPolyLine(vertexCount);
			}
			return topEdgeSequenced;
		}

		private PolyLine2D getBottomEdgeSequenced(int vertexCount) {
			if (bottomEdgeSequenced == null && getBottomEdge() != null) {
				bottomEdgeSequenced = getBottomEdge().toPolyLine(vertexCount);
			}
			return bottomEdgeSequenced;
		}

		public Curve2D getTopEdge() {
			return topEdge;
		}

		public Curve2D getBottomEdge() {
			return bottomEdge;
		}

	}

}