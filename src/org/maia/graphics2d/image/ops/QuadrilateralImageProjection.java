package org.maia.graphics2d.image.ops;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Objects;

import org.maia.graphics2d.image.ImageUtils;

public class QuadrilateralImageProjection {

	private boolean smoothEdges = true;

	private boolean subSampling = true;

	private boolean flipHorizontally;

	private boolean flipVertically;

	private boolean rememberLastEdgeSmoothing;

	private boolean rememberLastProjectionData;

	private EdgeSmoothingMaskCacheKey lastEdgeSmoothingMaskCacheKey;

	private EdgeSmoothingMask lastEdgeSmoothingMask;

	private ProjectionStateCacheKey lastProjectionStateCacheKey;

	private ProjectionState lastProjectionState;

	public QuadrilateralImageProjection() {
	}

	public BufferedImage project(BufferedImage sourceImage, Quadrilateral targetArea) {
		return project(sourceImage, targetArea, null);
	}

	public BufferedImage project(BufferedImage sourceImage, Quadrilateral targetArea,
			PseudoPerspective pseudoPerspective) {
		return project(sourceImage, ImageUtils.getSize(sourceImage), targetArea, pseudoPerspective);
	}

	public BufferedImage project(BufferedImage sourceImage, Dimension targetImageSize, Quadrilateral targetArea) {
		return project(sourceImage, targetImageSize, targetArea, null);
	}

	public synchronized BufferedImage project(BufferedImage sourceImage, Dimension targetImageSize,
			Quadrilateral targetArea, PseudoPerspective pseudoPerspective) {
		BufferedImage targetImage = ImageUtils.createImage(targetImageSize);
		EdgeSmoothingMask edgeSmoothingMask = null;
		EdgeSmoothingMaskCacheKey edgeSmoothingMaskCacheKey = null;
		if (isSmoothEdges()) {
			edgeSmoothingMaskCacheKey = new EdgeSmoothingMaskCacheKey(targetImageSize, targetArea);
			if (edgeSmoothingMaskCacheKey.equals(getLastEdgeSmoothingMaskCacheKey())) {
				edgeSmoothingMask = getLastEdgeSmoothingMask();
			} else {
				edgeSmoothingMask = new EdgeSmoothingMask(targetImageSize, targetArea);
			}
		}
		SubSamplingState subSamplingState = isSubSampling() ? new SubSamplingState() : null;
		ComputeState computeState = null;
		boolean reuseProjectionState = false;
		ProjectionState projectionState = null;
		ProjectionStateCacheKey projectionStateCacheKey = new ProjectionStateCacheKey(targetArea, pseudoPerspective);
		if (projectionStateCacheKey.equals(getLastProjectionStateCacheKey())) {
			projectionState = getLastProjectionState();
			reuseProjectionState = true;
		} else {
			computeState = new ComputeState(targetArea, pseudoPerspective);
			if (isRememberLastProjectionData()) {
				projectionState = new ProjectionState(targetArea);
			}
		}
		float sw = ImageUtils.getWidth(sourceImage) - 1f;
		float sh = ImageUtils.getHeight(sourceImage) - 1f;
		Rectangle rect = targetArea.getBoundingBox();
		for (int yi = 0; yi < rect.height; yi++) {
			int ty = rect.y + yi;
			for (int xi = 0; xi < rect.width; xi++) {
				int tx = rect.x + xi;
				float srx = Float.NaN;
				float sry = Float.NaN;
				if (reuseProjectionState) {
					srx = projectionState.getRelativeSourceXCoordinate(xi, yi);
					sry = projectionState.getRelativeSourceYCoordinate(xi, yi);
				} else {
					Point2D.Float srloc = computeState.projectTo(tx, ty);
					if (srloc != null) {
						srx = srloc.x;
						sry = srloc.y;
					}
					if (projectionState != null) {
						projectionState.setRelativeSourceXCoordinate(xi, yi, srx);
						projectionState.setRelativeSourceYCoordinate(xi, yi, sry);
					}
				}
				if (!Float.isNaN(srx)) {
					if (isFlipHorizontally())
						srx = 1f - srx;
					if (isFlipVertically())
						sry = 1f - sry;
					float sx = 0.5f + srx * sw;
					float sy = 0.5f + sry * sh;
					int argb = 0;
					if (subSamplingState == null) {
						argb = sourceImage.getRGB((int) Math.floor(sx), (int) Math.floor(sy));
					} else {
						argb = subSamplingState.subSample(sourceImage, sx, sy);
					}
					if (edgeSmoothingMask != null) {
						int alpha = edgeSmoothingMask.getMaskValue(tx, ty);
						int sourceAlpha = (argb & 0xff000000) >>> 24;
						if (sourceAlpha < 255) {
							alpha = (int) Math.round((alpha / 255f) * sourceAlpha);
						}
						argb = (alpha << 24) | (argb & 0x00ffffff);
					}
					targetImage.setRGB(tx, ty, argb);
				}
			}
		}
		rememberEdgeSmoothing(edgeSmoothingMask, edgeSmoothingMaskCacheKey);
		rememberProjection(projectionState, projectionStateCacheKey);
		return targetImage;
	}

	private void rememberEdgeSmoothing(EdgeSmoothingMask edgeSmoothingMask,
			EdgeSmoothingMaskCacheKey edgeSmoothingMaskCacheKey) {
		if (edgeSmoothingMask != null && isRememberLastEdgeSmoothing()) {
			setLastEdgeSmoothingMask(edgeSmoothingMask);
			setLastEdgeSmoothingMaskCacheKey(edgeSmoothingMaskCacheKey);
		}
	}

	private void rememberProjection(ProjectionState projectionState, ProjectionStateCacheKey projectionStateCacheKey) {
		if (projectionState != null && isRememberLastProjectionData()) {
			setLastProjectionState(projectionState);
			setLastProjectionStateCacheKey(projectionStateCacheKey);
		}
	}

	public boolean isSmoothEdges() {
		return smoothEdges;
	}

	public void setSmoothEdges(boolean smoothEdges) {
		this.smoothEdges = smoothEdges;
	}

	public boolean isSubSampling() {
		return subSampling;
	}

	public void setSubSampling(boolean subSampling) {
		this.subSampling = subSampling;
	}

	public boolean isFlipHorizontally() {
		return flipHorizontally;
	}

	public void setFlipHorizontally(boolean flip) {
		this.flipHorizontally = flip;
	}

	public boolean isFlipVertically() {
		return flipVertically;
	}

	public void setFlipVertically(boolean flip) {
		this.flipVertically = flip;
	}

	public void setRememberLast(boolean remember) {
		setRememberLastEdgeSmoothing(remember);
		setRememberLastProjectionData(remember);
	}

	public boolean isRememberLastEdgeSmoothing() {
		return rememberLastEdgeSmoothing;
	}

	public void setRememberLastEdgeSmoothing(boolean remember) {
		this.rememberLastEdgeSmoothing = remember;
	}

	public boolean isRememberLastProjectionData() {
		return rememberLastProjectionData;
	}

	public void setRememberLastProjectionData(boolean remember) {
		this.rememberLastProjectionData = remember;
	}

	private EdgeSmoothingMaskCacheKey getLastEdgeSmoothingMaskCacheKey() {
		return lastEdgeSmoothingMaskCacheKey;
	}

	private void setLastEdgeSmoothingMaskCacheKey(EdgeSmoothingMaskCacheKey cacheKey) {
		this.lastEdgeSmoothingMaskCacheKey = cacheKey;
	}

	private EdgeSmoothingMask getLastEdgeSmoothingMask() {
		return lastEdgeSmoothingMask;
	}

	private void setLastEdgeSmoothingMask(EdgeSmoothingMask mask) {
		this.lastEdgeSmoothingMask = mask;
	}

	private ProjectionStateCacheKey getLastProjectionStateCacheKey() {
		return lastProjectionStateCacheKey;
	}

	private void setLastProjectionStateCacheKey(ProjectionStateCacheKey cacheKey) {
		this.lastProjectionStateCacheKey = cacheKey;
	}

	private ProjectionState getLastProjectionState() {
		return lastProjectionState;
	}

	private void setLastProjectionState(ProjectionState state) {
		this.lastProjectionState = state;
	}

	public static class PseudoPerspective {

		private float horizontalMagnitude;

		private float verticalMagnitude;

		public PseudoPerspective(float horizontalMagnitude, float verticalMagnitude) {
			if (horizontalMagnitude < 0f || horizontalMagnitude > 1f)
				throw new IllegalArgumentException(
						"The horizontal magnitude must be in unit interval (" + horizontalMagnitude + ")");
			if (verticalMagnitude < 0f || verticalMagnitude > 1f)
				throw new IllegalArgumentException(
						"The vertical magnitude must be in unit interval (" + verticalMagnitude + ")");
			this.horizontalMagnitude = horizontalMagnitude;
			this.verticalMagnitude = verticalMagnitude;
		}

		@Override
		public int hashCode() {
			return Objects.hash(getHorizontalMagnitude(), getVerticalMagnitude());
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PseudoPerspective other = (PseudoPerspective) obj;
			return getHorizontalMagnitude() == other.getHorizontalMagnitude()
					&& getVerticalMagnitude() == other.getVerticalMagnitude();
		}

		public float getHorizontalMagnitude() {
			return horizontalMagnitude;
		}

		public float getVerticalMagnitude() {
			return verticalMagnitude;
		}

	}

	public static class Quadrilateral {

		private Point upperLeftVertex;

		private Point upperRightVertex;

		private Point bottomRightVertex;

		private Point bottomLeftVertex;

		public Quadrilateral(Point upperLeftVertex, Point upperRightVertex, Point bottomRightVertex,
				Point bottomLeftVertex) {
			this.upperLeftVertex = upperLeftVertex;
			this.upperRightVertex = upperRightVertex;
			this.bottomRightVertex = bottomRightVertex;
			this.bottomLeftVertex = bottomLeftVertex;
		}

		@Override
		public int hashCode() {
			return Objects.hash(getBottomLeftVertex(), getBottomRightVertex(), getUpperLeftVertex(),
					getUpperRightVertex());
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Quadrilateral other = (Quadrilateral) obj;
			return Objects.equals(getBottomLeftVertex(), other.getBottomLeftVertex())
					&& Objects.equals(getBottomRightVertex(), other.getBottomRightVertex())
					&& Objects.equals(getUpperLeftVertex(), other.getUpperLeftVertex())
					&& Objects.equals(getUpperRightVertex(), other.getUpperRightVertex());
		}

		public Rectangle getBoundingBox() {
			int xmin = Math.min(getUpperLeftVertex().x, getBottomLeftVertex().x);
			int xmax = Math.max(getUpperRightVertex().x, getBottomRightVertex().x);
			int ymin = Math.min(getUpperLeftVertex().y, getUpperRightVertex().y);
			int ymax = Math.max(getBottomLeftVertex().y, getBottomRightVertex().y);
			return new Rectangle(xmin, ymin, xmax - xmin + 1, ymax - ymin + 1);
		}

		Shape getOutline() {
			Polygon polygon = new Polygon();
			polygon.addPoint(getUpperLeftVertex().x + 1, getUpperLeftVertex().y + 1);
			polygon.addPoint(getUpperRightVertex().x, getUpperRightVertex().y + 1);
			polygon.addPoint(getBottomRightVertex().x, getBottomRightVertex().y);
			polygon.addPoint(getBottomLeftVertex().x + 1, getBottomLeftVertex().y);
			return polygon;
		}

		int compareLeftToRightSideLength() {
			double left = getUpperLeftVertex().distanceSq(getBottomLeftVertex());
			double right = getUpperRightVertex().distanceSq(getBottomRightVertex());
			if (left == right) {
				return 0;
			} else if (left < right) {
				return -1;
			} else {
				return 1;
			}
		}

		int compareUpperToBottomSideLength() {
			double upper = getUpperLeftVertex().distanceSq(getUpperRightVertex());
			double bottom = getBottomLeftVertex().distanceSq(getBottomRightVertex());
			if (upper == bottom) {
				return 0;
			} else if (upper < bottom) {
				return -1;
			} else {
				return 1;
			}
		}

		public Point getUpperLeftVertex() {
			return upperLeftVertex;
		}

		public Point getUpperRightVertex() {
			return upperRightVertex;
		}

		public Point getBottomLeftVertex() {
			return bottomLeftVertex;
		}

		public Point getBottomRightVertex() {
			return bottomRightVertex;
		}

	}

	private static class ComputeState {

		private float p1x, p1y;

		private float p2x, p2y;

		private float p3x, p3y;

		private float p4x, p4y;

		private float vx, vy;

		private float a0, b0, c, d0, e0, f;

		private float ex, ey;

		private Point2D.Float relativeSourceLocation = new Point2D.Float(); // for reuse

		public ComputeState(Quadrilateral targetArea, PseudoPerspective pseudoPerspective) {
			this.p1x = targetArea.getUpperLeftVertex().x + 0.5f;
			this.p1y = targetArea.getUpperLeftVertex().y + 0.5f;
			this.p2x = targetArea.getUpperRightVertex().x + 0.5f;
			this.p2y = targetArea.getUpperRightVertex().y + 0.5f;
			this.p3x = targetArea.getBottomLeftVertex().x + 0.5f;
			this.p3y = targetArea.getBottomLeftVertex().y + 0.5f;
			this.p4x = targetArea.getBottomRightVertex().x + 0.5f;
			this.p4y = targetArea.getBottomRightVertex().y + 0.5f;
			this.vx = p1x - p2x + p4x - p3x;
			this.vy = p1y - p2y + p4y - p3y;
			this.a0 = p2y - p1y;
			this.b0 = (p3x - p1x) * a0;
			this.c = (p1x - p3x) * vy;
			this.d0 = p2x - p1x;
			this.e0 = (p3y - p1y) * d0;
			this.f = (p1y - p3y) * vx;
			this.ex = computeHorizontalExponent(targetArea, pseudoPerspective);
			this.ey = computeVerticalExponent(targetArea, pseudoPerspective);
		}

		public Point2D.Float projectTo(int tx, int ty) {
			float qx = tx + 0.5f;
			float qy = ty + 0.5f;
			float a = (qx - p1x) * a0;
			float b = (qx - p1x) * vy - b0;
			float d = (qy - p1y) * d0;
			float e = (qy - p1y) * vx - e0;
			float eb = e - b;
			float cf = c - f;
			float ad = a - d;
			float beta = (eb + (float) Math.sqrt(eb * eb - 4f * cf * ad)) / (2f * cf);
			if (beta >= 0f && beta <= 1f) {
				float alpha = (qx - p1x - beta * (p3x - p1x)) / (d0 + beta * vx);
				if (alpha >= 0f && alpha <= 1f) {
					relativeSourceLocation.x = ex == 1f ? alpha : (float) Math.pow(alpha, ex);
					relativeSourceLocation.y = ey == 1f ? beta : (float) Math.pow(beta, ey);
					return relativeSourceLocation;
				}
			}
			return null;
		}

		private float computeHorizontalExponent(Quadrilateral targetArea, PseudoPerspective pseudoPerspective) {
			float exponent = 1f;
			if (pseudoPerspective != null) {
				exponent += pseudoPerspective.getHorizontalMagnitude();
				if (targetArea.compareLeftToRightSideLength() < 0)
					exponent = 1f / exponent;
			}
			return exponent;
		}

		private float computeVerticalExponent(Quadrilateral targetArea, PseudoPerspective pseudoPerspective) {
			float exponent = 1f;
			if (pseudoPerspective != null) {
				exponent += pseudoPerspective.getVerticalMagnitude();
				if (targetArea.compareUpperToBottomSideLength() < 0)
					exponent = 1f / exponent;
			}
			return exponent;
		}

	}

	private static class SubSamplingState {

		private int cx, cy;

		private int minDx, maxDx;

		private int minDy, maxDy;

		private float cWeightX, cWeightY;

		public SubSamplingState() {
		}

		public int subSample(BufferedImage sourceImage, float sx, float sy) {
			update(sx, sy);
			float alpha = 0f;
			float red = 0f;
			float green = 0f;
			float blue = 0f;
			for (int dy = minDy; dy <= maxDy; dy++) {
				int y = cy + dy;
				float wy = dy == 0 ? cWeightY : 1f - cWeightY;
				for (int dx = minDx; dx <= maxDx; dx++) {
					int x = cx + dx;
					int argb = sourceImage.getRGB(x, y);
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

	private static class EdgeSmoothingMask {

		private BufferedImage maskImage;

		public EdgeSmoothingMask(Dimension size, Quadrilateral area) {
			this.maskImage = createMaskImage(size, area);
		}

		private static BufferedImage createMaskImage(Dimension size, Quadrilateral area) {
			BufferedImage mask = new BufferedImage(size.width, size.height, BufferedImage.TYPE_BYTE_GRAY);
			ImageUtils.clearWithUniformColor(mask, Color.BLACK);
			Graphics2D g2 = mask.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(Color.WHITE);
			g2.fill(area.getOutline());
			g2.dispose();
			return mask;
		}

		public int getMaskValue(int x, int y) {
			return maskImage.getRGB(x, y) & 0xff;
		}

	}

	private static class EdgeSmoothingMaskCacheKey {

		private Dimension targetImageSize;

		private Quadrilateral targetArea;

		public EdgeSmoothingMaskCacheKey(Dimension targetImageSize, Quadrilateral targetArea) {
			this.targetImageSize = targetImageSize;
			this.targetArea = targetArea;
		}

		@Override
		public int hashCode() {
			return Objects.hash(getTargetImageSize(), getTargetArea());
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			EdgeSmoothingMaskCacheKey other = (EdgeSmoothingMaskCacheKey) obj;
			return Objects.equals(getTargetImageSize(), other.getTargetImageSize())
					&& Objects.equals(getTargetArea(), other.getTargetArea());
		}

		private Dimension getTargetImageSize() {
			return targetImageSize;
		}

		private Quadrilateral getTargetArea() {
			return targetArea;
		}

	}

	private static class ProjectionState {

		private float[][] relativeSourceXCoordinates;

		private float[][] relativeSourceYCoordinates;

		public ProjectionState(Quadrilateral targetArea) {
			Rectangle rect = targetArea.getBoundingBox();
			this.relativeSourceXCoordinates = new float[rect.height][rect.width];
			this.relativeSourceYCoordinates = new float[rect.height][rect.width];
		}

		public float getRelativeSourceXCoordinate(int xi, int yi) {
			return relativeSourceXCoordinates[yi][xi];
		}

		public void setRelativeSourceXCoordinate(int xi, int yi, float coord) {
			relativeSourceXCoordinates[yi][xi] = coord;
		}

		public float getRelativeSourceYCoordinate(int xi, int yi) {
			return relativeSourceYCoordinates[yi][xi];
		}

		public void setRelativeSourceYCoordinate(int xi, int yi, float coord) {
			relativeSourceYCoordinates[yi][xi] = coord;
		}

	}

	private static class ProjectionStateCacheKey {

		private Quadrilateral targetArea;

		private PseudoPerspective pseudoPerspective;

		public ProjectionStateCacheKey(Quadrilateral targetArea, PseudoPerspective pseudoPerspective) {
			this.targetArea = targetArea;
			this.pseudoPerspective = pseudoPerspective;
		}

		@Override
		public int hashCode() {
			return Objects.hash(getTargetArea(), getPseudoPerspective());
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ProjectionStateCacheKey other = (ProjectionStateCacheKey) obj;
			return Objects.equals(getPseudoPerspective(), other.getPseudoPerspective())
					&& Objects.equals(getTargetArea(), other.getTargetArea());
		}

		private Quadrilateral getTargetArea() {
			return targetArea;
		}

		private PseudoPerspective getPseudoPerspective() {
			return pseudoPerspective;
		}

	}

}