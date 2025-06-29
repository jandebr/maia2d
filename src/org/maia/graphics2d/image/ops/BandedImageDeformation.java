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

		private int bandIndex = -1;

		private float bandOffset;

		private float bandWidth;

		private int sourceOffset;

		private float previousY;

		public VerticalBandedImageDeformation() {
			setHorizontalProjection(this);
		}

		@Override
		public float projectX(float x, float y, int width, int height) {
			if (y != previousY || bandIndex < 0) {
				bandIndex = 0;
				bandOffset = 0f;
				bandWidth = getBand(0).getTargetWidth(y, width, height);
				sourceOffset = 0;
				previousY = y;
			}
			while (x >= bandOffset + bandWidth && bandIndex < getBands().size() - 1) {
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

		private int bandIndex = -1;

		private float bandOffset;

		private float bandHeight;

		private int sourceOffset;

		private float previousX;

		public HorizontalBandedImageDeformation() {
			setVerticalProjection(this);
		}

		@Override
		public float projectY(float x, float y, int width, int height) {
			if (x != previousX || bandIndex < 0) {
				bandIndex = 0;
				bandOffset = 0f;
				bandHeight = getBand(0).getTargetHeight(x, width, height);
				sourceOffset = 0;
				previousX = x;
			}
			while (y >= bandOffset + bandHeight && bandIndex < getBands().size() - 1) {
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

		private int leftEdgeSeqIndex;

		private Curve2D rightEdge;

		private PolyLine2D rightEdgeSequenced;

		private int rightEdgeSeqIndex;

		private float previousY = Float.NaN;

		private float previousTargetWidth;

		public CurvedVerticalImageBand(int sourceWidth, Curve2D leftEdge, Curve2D rightEdge) {
			super(sourceWidth);
			this.leftEdge = leftEdge;
			this.rightEdge = rightEdge;
		}

		@Override
		public float getTargetWidth(float y, int width, int height) {
			if (y != previousY) {
				if (y < previousY) {
					leftEdgeSeqIndex = 0;
					rightEdgeSeqIndex = 0;
				}
				float xLeft = intersectLeftEdgeAtY(y, width, height);
				float xRight = intersectRightEdgeAtY(y, width, height);
				previousY = y;
				previousTargetWidth = Math.max(xRight - xLeft, 1f);
			}
			return previousTargetWidth;
		}

		private float intersectLeftEdgeAtY(float y, int width, int height) {
			PolyLine2D leftEdgeSeq = getLeftEdgeSequenced(height);
			if (leftEdgeSeq != null) {
				int n = leftEdgeSeq.getEdges().size();
				do {
					Point2D p = leftEdgeSeq.getEdges().get(leftEdgeSeqIndex).intersectAtY(y);
					if (p != null)
						return (float) p.getX();
				} while (++leftEdgeSeqIndex < n);
			}
			return 0f;
		}

		private float intersectRightEdgeAtY(float y, int width, int height) {
			PolyLine2D rightEdgeSeq = getRightEdgeSequenced(height);
			if (rightEdgeSeq != null) {
				int n = rightEdgeSeq.getEdges().size();
				do {
					Point2D p = rightEdgeSeq.getEdges().get(rightEdgeSeqIndex).intersectAtY(y);
					if (p != null)
						return (float) p.getX();
				} while (++rightEdgeSeqIndex < n);
			}
			return width;
		}

		private PolyLine2D getLeftEdgeSequenced(int height) {
			if (leftEdgeSequenced == null && getLeftEdge() != null) {
				leftEdgeSequenced = getLeftEdge().toPolyLine(getSequencedEdgeVertexCount(height));
			}
			return leftEdgeSequenced;
		}

		private PolyLine2D getRightEdgeSequenced(int height) {
			if (rightEdgeSequenced == null && getRightEdge() != null) {
				rightEdgeSequenced = getRightEdge().toPolyLine(getSequencedEdgeVertexCount(height));
			}
			return rightEdgeSequenced;
		}

		private int getSequencedEdgeVertexCount(int height) {
			return 2 + height / 2;
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

		private int topEdgeSeqIndex;

		private Curve2D bottomEdge;

		private PolyLine2D bottomEdgeSequenced;

		private int bottomEdgeSeqIndex;

		private float previousX = Float.NaN;

		private float previousTargetHeight;

		public CurvedHorizontalImageBand(int sourceHeight, Curve2D topEdge, Curve2D bottomEdge) {
			super(sourceHeight);
			this.topEdge = topEdge;
			this.bottomEdge = bottomEdge;
		}

		@Override
		public float getTargetHeight(float x, int width, int height) {
			if (x != previousX) {
				if (x < previousX) {
					topEdgeSeqIndex = 0;
					bottomEdgeSeqIndex = 0;
				}
				float yTop = intersectTopEdgeAtX(x, width, height);
				float yBottom = intersectBottomEdgeAtX(x, width, height);
				previousX = x;
				previousTargetHeight = Math.max(yBottom - yTop, 1f);
			}
			return previousTargetHeight;
		}

		private float intersectTopEdgeAtX(float x, int width, int height) {
			PolyLine2D topEdgeSeq = getTopEdgeSequenced(width);
			if (topEdgeSeq != null) {
				int n = topEdgeSeq.getEdges().size();
				do {
					Point2D p = topEdgeSeq.getEdges().get(topEdgeSeqIndex).intersectAtX(x);
					if (p != null)
						return (float) p.getY();
				} while (++topEdgeSeqIndex < n);
			}
			return 0f;
		}

		private float intersectBottomEdgeAtX(float x, int width, int height) {
			PolyLine2D bottomEdgeSeq = getBottomEdgeSequenced(width);
			if (bottomEdgeSeq != null) {
				int n = bottomEdgeSeq.getEdges().size();
				do {
					Point2D p = bottomEdgeSeq.getEdges().get(bottomEdgeSeqIndex).intersectAtX(x);
					if (p != null)
						return (float) p.getY();
				} while (++bottomEdgeSeqIndex < n);
			}
			return height;
		}

		private PolyLine2D getTopEdgeSequenced(int width) {
			if (topEdgeSequenced == null && getTopEdge() != null) {
				topEdgeSequenced = getTopEdge().toPolyLine(getSequencedEdgeVertexCount(width));
			}
			return topEdgeSequenced;
		}

		private PolyLine2D getBottomEdgeSequenced(int width) {
			if (bottomEdgeSequenced == null && getBottomEdge() != null) {
				bottomEdgeSequenced = getBottomEdge().toPolyLine(getSequencedEdgeVertexCount(width));
			}
			return bottomEdgeSequenced;
		}

		private int getSequencedEdgeVertexCount(int width) {
			return 2 + width / 2;
		}

		public Curve2D getTopEdge() {
			return topEdge;
		}

		public Curve2D getBottomEdge() {
			return bottomEdge;
		}

	}

}