package org.maia.graphics2d;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.maia.graphics2d.function.Function2D;
import org.maia.graphics2d.function.PerpetualApproximatingFunction2D;
import org.maia.graphics2d.function.PerpetualApproximatingFunction2D.ControlValueGenerator;
import org.maia.graphics2d.geometry.ClosedLineSegment2D;
import org.maia.graphics2d.geometry.Point2D;
import org.maia.graphics2d.geometry.PolyLine2D;
import org.maia.graphics2d.geometry.Rectangle2D;
import org.maia.graphics2d.transform.TransformMatrix2D;
import org.maia.graphics2d.transform.Transformation2D;

public class PerpetualApproximatingFunctionDemo {

	public static void main(String[] args) {
		new PerpetualApproximatingFunctionDemo().startDemo();
	}

	private void startDemo() {
		ControlValueGeneratorImpl generator = new ControlValueGeneratorImpl();
		PerpetualApproximatingFunction2D function = PerpetualApproximatingFunction2D
				.createCubicPrimedApproximatingFunction(generator);
		GraphSeriesView view = createView(function);
		view.addSeries(new GraphSeries(generator.getControlPointsPolyLine(), Color.RED, false, true));
		showFrame(view);
	}

	private GraphSeriesView createView(PerpetualApproximatingFunction2D function) {
		GraphSeriesView view = new GraphSeriesView(new Dimension(800, 400), new Rectangle2D(-0.5, 25.5, -0.1, 1.1));
		view.addSeries(new GraphSeries(sampleFunction(function, 14, 10, 4.0), Color.YELLOW, true, false));
		return view;
	}

	private PolyLine2D sampleFunction(Function2D function, int units, int subSamplesPerUnit, double translationX) {
		System.out.println("Sampling function");
		int n = units * (1 + subSamplesPerUnit) + 1;
		List<Point2D> vertices = new Vector<Point2D>(n);
		for (int i = 0; i < n; i++) {
			double x = i / (1.0 + subSamplesPerUnit);
			double y = function.evaluate(x);
			vertices.add(new Point2D(x + translationX, y));
		}
		return new PolyLine2D(vertices);
	}

	private void showFrame(GraphSeriesView view) {
		JFrame frame = new JFrame("Perpetual approximating function");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setBackground(Color.BLACK);
		frame.add(view, BorderLayout.CENTER);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private static class ControlValueGeneratorImpl implements ControlValueGenerator {

		private List<Point2D> controlPoints;

		public ControlValueGeneratorImpl() {
			this.controlPoints = new Vector<Point2D>();
		}

		@Override
		public double generateControlValue() {
			return sampleNewControlPoint().getY();
		}

		private Point2D sampleNewControlPoint() {
			double x = getControlPoints().size();
			double y = 0.5 + 0.3 * Math.sin(x * 0.5) + 0.4 * (Math.random() - 0.5);
			Point2D p = new Point2D(x, y);
			getControlPoints().add(p);
			System.out.println(p);
			return p;
		}

		public PolyLine2D getControlPointsPolyLine() {
			return new PolyLine2D(getControlPoints());
		}

		private List<Point2D> getControlPoints() {
			return controlPoints;
		}

	}

	private static class GraphSeries {

		private PolyLine2D polyLine;

		private Color color;

		private boolean drawEdges;

		private boolean drawVertices;

		public GraphSeries(PolyLine2D polyLine, Color color, boolean drawEdges, boolean drawVertices) {
			this.polyLine = polyLine;
			this.color = color;
			this.drawEdges = drawEdges;
			this.drawVertices = drawVertices;
		}

		public PolyLine2D getPolyLine() {
			return polyLine;
		}

		public Color getColor() {
			return color;
		}

		public boolean isDrawEdges() {
			return drawEdges;
		}

		public boolean isDrawVertices() {
			return drawVertices;
		}

	}

	@SuppressWarnings("serial")
	private static class GraphSeriesView extends JPanel {

		private Rectangle2D viewBox;

		private TransformMatrix2D coordinateToViewTransform;

		private List<GraphSeries> series;

		public GraphSeriesView(Dimension size, Rectangle2D viewBox) {
			this.viewBox = viewBox;
			this.coordinateToViewTransform = createCoordinateToViewTransform(size, viewBox);
			this.series = new Vector<GraphSeries>();
			setPreferredSize(size);
			setBackground(Color.BLACK);
			setForeground(Color.DARK_GRAY);
			setOpaque(true);
		}

		private TransformMatrix2D createCoordinateToViewTransform(Dimension size, Rectangle2D viewBox) {
			TransformMatrix2D T = Transformation2D.getTranslationMatrix(0, size.getHeight());
			T = T.preMultiply(Transformation2D.getScalingMatrix(1.0, -1.0));
			T = T.preMultiply(Transformation2D.getScalingMatrix(size.getWidth() / viewBox.getWidth(),
					size.getHeight() / viewBox.getHeight()));
			T = T.preMultiply(Transformation2D.getTranslationMatrix(-viewBox.getX1(), -viewBox.getY1()));
			return T;
		}

		public void addSeries(GraphSeries series) {
			getSeries().add(series);
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
			paintBackground(g);
			Graphics2D g2 = (Graphics2D) g.create();
			paintAxes(g2);
			paintSeries(g2);
			g2.dispose();
		}

		private void paintBackground(Graphics g) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
		}

		private void paintAxes(Graphics2D g2) {
			g2.setColor(getForeground());
			paintHorizontalAxis(g2);
			paintVerticalAxis(g2);
		}

		private void paintHorizontalAxis(Graphics2D g2) {
			Rectangle2D vb = getViewBox();
			paintClosedLineSegment(g2, new ClosedLineSegment2D(new Point2D(vb.getX1(), 0), new Point2D(vb.getX2(), 0)));
			int i0 = (int) Math.ceil(vb.getX1());
			int i1 = (int) Math.floor(vb.getX2());
			for (int i = i0; i <= i1; i++) {
				Point2D pi = toViewCoordinates(new Point2D(i, 0));
				int xi = (int) Math.round(pi.getX());
				int yi = (int) Math.round(pi.getY());
				g2.drawLine(xi, yi - 2, xi, yi + 2);
			}
		}

		private void paintVerticalAxis(Graphics2D g2) {
			Rectangle2D vb = getViewBox();
			paintClosedLineSegment(g2, new ClosedLineSegment2D(new Point2D(0, vb.getY1()), new Point2D(0, vb.getY2())));
			int i0 = (int) Math.ceil(vb.getY1());
			int i1 = (int) Math.floor(vb.getY2());
			for (int i = i0; i <= i1; i++) {
				Point2D pi = toViewCoordinates(new Point2D(0, i));
				int xi = (int) Math.round(pi.getX());
				int yi = (int) Math.round(pi.getY());
				g2.drawLine(xi - 2, yi, xi + 2, yi);
			}
		}

		private void paintSeries(Graphics2D g2) {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			for (GraphSeries series : getSeries()) {
				paintSeries(g2, series);
			}
		}

		private void paintSeries(Graphics2D g2, GraphSeries series) {
			g2.setColor(series.getColor());
			if (series.isDrawEdges()) {
				for (ClosedLineSegment2D segment : series.getPolyLine().getEdges()) {
					paintClosedLineSegment(g2, segment);
				}
			}
			if (series.isDrawVertices()) {
				for (Point2D vertex : series.getPolyLine().getVertices()) {
					Point2D p = toViewCoordinates(vertex);
					int x = (int) Math.round(p.getX());
					int y = (int) Math.round(p.getY());
					g2.fillOval(x - 3, y - 3, 6, 6);
				}
			}
		}

		private void paintClosedLineSegment(Graphics2D g2, ClosedLineSegment2D segment) {
			Point2D p1 = toViewCoordinates(segment.getP1());
			Point2D p2 = toViewCoordinates(segment.getP2());
			int x1 = (int) Math.round(p1.getX());
			int y1 = (int) Math.round(p1.getY());
			int x2 = (int) Math.round(p2.getX());
			int y2 = (int) Math.round(p2.getY());
			g2.drawLine(x1, y1, x2, y2);
		}

		private Point2D toViewCoordinates(Point2D point) {
			return getCoordinateToViewTransform().transform(point);
		}

		public Rectangle2D getViewBox() {
			return viewBox;
		}

		private TransformMatrix2D getCoordinateToViewTransform() {
			return coordinateToViewTransform;
		}

		private List<GraphSeries> getSeries() {
			return series;
		}

	}

}