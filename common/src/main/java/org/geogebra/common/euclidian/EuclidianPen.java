package org.geogebra.common.euclidian;

import java.util.ArrayList;

import org.geogebra.common.awt.GBasicStroke;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.awt.GGeneralPath;
import org.geogebra.common.awt.GGraphics2D;
import org.geogebra.common.awt.GPoint;
import org.geogebra.common.euclidian.event.AbstractEvent;
import org.geogebra.common.factories.AwtFactory;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.MyPoint;
import org.geogebra.common.kernel.algos.AlgoLocusStroke;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoPolyLine;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.kernel.matrix.Coords;
import org.geogebra.common.main.App;
import org.geogebra.common.plugin.EuclidianStyleConstants;
import org.geogebra.common.util.DoubleUtil;
import org.geogebra.common.util.GTimer;
import org.geogebra.common.util.GTimerListener;

/**
 * Handles pen and freehand tool
 *
 */
public class EuclidianPen implements GTimerListener {

	/**
	 * app
	 */
	protected App app;
	/**
	 * view
	 */
	protected EuclidianView view;

	/** Polyline that conects stylebar to pen settings */
	public final GeoPolyLine defaultPenLine;

	private AlgoLocusStroke lastAlgo = null;
	/** points created by pen */
	protected ArrayList<GPoint> penPoints = new ArrayList<>();

	// segment
	private final static int PEN_SIZE_FACTOR = 2;
	/** skip intermediate points on segments longer than this */
	private static final double MAX_POINT_DIST = 30;
	/** ignore consecutive pen points closer than this */
	private static final double MIN_POINT_DIST = 3;
	private static final double MAX_POINT_COS = Math.cos(Math.PI / 36);

	private boolean startNewStroke = false;

	private int penSize;
	private int lineOpacity;
	private int lineThickness;
	private GColor lineDrawingColor;
	private int lineDrawingStyle;
	// true if we need repaint
	private boolean needsRepaint;

	/**
	 * start point of the gesture
	 */
	protected GeoPoint initialPoint = null;

	/**
	 * delete initialPoint if no shape is found
	 */
	protected boolean deleteInitialPoint = false;

	private GTimer timer = null;

	private int penLineStyle;
	private GColor penColor = GColor.BLACK;
	private PenPreviewLine penPreviewLine;

	/************************************************
	 * Construct EuclidianPen
	 *
	 * @param app
	 *            application
	 * @param view
	 *            view
	 */
	public EuclidianPen(App app, EuclidianView view) {
		this.view = view;
		this.app = app;
		this.penPreviewLine = view.newPenPreview();
		timer = app.newTimer(this, 1500);

		defaultPenLine = new GeoPolyLine(app.getKernel().getConstruction()) {
			@Override
			public void setObjColor(GColor color) {
				super.setObjColor(color);
				setPenColor(color);
			}

			@Override
			public void setLineThickness(int th) {
				super.setLineThickness(th);
				setPenSize(th);
			}

			@Override
			public void setLineType(int i) {
				super.setLineType(i);
				setPenLineStyle(i);
			}

			@Override
			public void setLineOpacity(int lineOpacity) {
				super.setLineOpacity(lineOpacity);
				setPenOpacity(lineOpacity);
			}
		};
		setDefaults();
		defaultPenLine.setLineThickness(penSize);
		defaultPenLine.setLineOpacity(lineOpacity);
		defaultPenLine.setObjColor(penColor);
	}

	// ===========================================
	// Getters/Setters
	// ===========================================

	/**
	 * Set default pen color, line style, thickness, eraser size
	 */
	public void setDefaults() {
		penSize = EuclidianConstants.DEFAULT_PEN_SIZE;
		penLineStyle = EuclidianStyleConstants.LINE_TYPE_FULL;
		penColor = GColor.BLACK;
		lineOpacity = 85 * 255 / 100;
	}

	/**
	 * @return pen size
	 */
	public int getPenSize() {
		return penSize;
	}

	/**
	 * @param lineOpacity
	 *            Opacity
	 */
	public void setPenOpacity(int lineOpacity) {
		if (this.lineOpacity != lineOpacity) {
			startNewStroke = true;
		}
		this.lineOpacity = lineOpacity;
		setPenColor(penColor.deriveWithAlpha(lineOpacity));
	}

	/**
	 * @return pen size + 1
	 */
	public int getLineThickness() {
		return lineThickness + 1;
	}

	/**
	 * @param penSize
	 *            pen size
	 */
	public void setPenSize(int penSize) {
		if (this.penSize != penSize) {
			startNewStroke = true;
		}
		this.penSize = penSize;
		lineThickness = penSize;
	}

	/**
	 * @return pen line style
	 */
	public int getPenLineStyle() {
		return penLineStyle;
	}

	/**
	 * @param penLineStyle
	 *            pen line style
	 */
	public void setPenLineStyle(int penLineStyle) {
		if (this.penLineStyle != penLineStyle) {
			startNewStroke = true;
		}
		this.penLineStyle = penLineStyle;
		lineDrawingStyle = penLineStyle;
	}

	/**
	 * @return pen color
	 */
	public GColor getPenColor() {
		return penColor;
	}

	/**
	 * use one point as first point of the created shape
	 *
	 * @param point
	 *            start point
	 * @param deletePoint
	 *            delete the point if no shape is found
	 */
	public void setInitialPoint(GeoPoint point, boolean deletePoint) {
		this.initialPoint = point;
		this.deleteInitialPoint = deletePoint;
	}

	/**
	 *
	 * @param e
	 *            event
	 * @return Is this MouseEvent an erasing Event.
	 */
	public boolean isErasingEvent(AbstractEvent e) {
		return app.isRightClick(e) && !isFreehand();
	}

	/**
	 * Make sure we start using a new polyline
	 */
	public void resetPenOffsets() {
		lastAlgo = null;
	}

	// ===========================================
	// Mouse Event Handlers
	// ===========================================

	/**
	 * Mouse dragged while in pen mode, decide whether erasing or new points.
	 *
	 * @param e
	 *            mouse event
	 */
	public void handleMouseDraggedForPenMode(AbstractEvent e) {
		view.setCursor(EuclidianCursor.TRANSPARENT);
		if (isErasingEvent(e)) {
			view.getEuclidianController().getDeleteMode()
					.handleMouseDraggedForDelete(e, true);
			app.getKernel().notifyRepaint();
		} else {
			// drawing in progress, so we need repaint
			needsRepaint = true;
			addPointPenMode(e);
		}
	}

	/**
	 * @param e
	 *            event
	 */
	public void handleMousePressedForPenMode(AbstractEvent e) {
		if (!isErasingEvent(e)) {
			timer.stop();

			penPoints.clear();
			addPointPenMode(e);
			view.cacheGraphics();
		}
	}

	/**
	 * Method to repaint the whole preview line
	 * 
	 * @param g2D
	 *            graphics for pen
	 */
	public void doRepaintPreviewLine(GGraphics2D g2D) {
		if (penPoints.size() < 2) {
			return;
		}

		g2D.setStroke(EuclidianStatic.getStroke(getLineThickness(),
				lineDrawingStyle, GBasicStroke.JOIN_ROUND));
		g2D.setColor(lineDrawingColor);
		penPreviewLine.drawPolyline(penPoints, g2D);
	}

	/**
	 * Method to repaint the whole preview line from (x, y) with a given width.
	 * 
	 * @param g2D
	 *            graphics for pen
	 * @param color
	 *            of the pen preview
	 * @param thickness
	 *            of the pen preview
	 * @param x
	 *            Start x coordinate
	 * @param y
	 *            Start y coordinate
	 * @param width
	 *            of the preview
	 */
	public void drawStylePreview(GGraphics2D g2D, GColor color, int thickness,
			int x, int y, int width) {
		GGeneralPath gp = AwtFactory.getPrototype().newGeneralPath();
		g2D.setStroke(EuclidianStatic.getStroke(thickness,
				EuclidianStyleConstants.LINE_TYPE_FULL));
		g2D.setColor(color);
		gp.reset();
		gp.moveTo(x, y);
		gp.lineTo(x + width, y);
		g2D.draw(gp);
	}

	/**
	 * add the saved points to the last stroke or create a new one
	 *
	 * @param e
	 *            event
	 */
	public void addPointPenMode(AbstractEvent e) {
		view.setCursor(EuclidianCursor.TRANSPARENT);

		GPoint newPoint = new GPoint(e.getX(), e.getY());

		addPointPenMode(newPoint);
	}

	/**
	 * Append point to the list
	 * 
	 * @param newPoint
	 *            new point
	 */
	protected void addPointPenMode(GPoint newPoint) {
		if (penPoints.size() == 0) {
			if (initialPoint != null) {
				// also add the coordinates of the initialPoint to the penPoints
				Coords coords = initialPoint.getCoords();
				// calculate the screen coordinates
				int locationX = (int) (view.getXZero()
						+ (coords.getX() / view.getInvXscale()));
				int locationY = (int) (view.getYZero()
						- (coords.getY() / view.getInvYscale()));

				GPoint p = new GPoint(locationX, locationY);
				penPoints.add(p);
				addPointRepaint();
			}
			penPoints.add(newPoint);
		} else {
			GPoint p1 = penPoints.get(penPoints.size() - 1);
			double dist = p1.distance(newPoint);
			if (isFreehand()) {
				if (dist > MIN_POINT_DIST) {
					penPoints.add(newPoint);
					addPointRepaint();
				}
				return;
			}
			GPoint p2 = penPoints.size() >= 2
					? penPoints.get(penPoints.size() - 2) : null;
			GPoint p3 = tailStart(newPoint);
			if (dist > MIN_POINT_DIST) {
				if (dist > MAX_POINT_DIST || p3 == null || p2 == null) {
					penPoints.add(newPoint);
					addPointRepaint();
				} else {
					p2.x = (p1.x + p2.x) / 2;
					p2.y = (p1.y + p2.y) / 2;
					p1.x = newPoint.x;
					p1.y = newPoint.y;
					addPointRepaint();
				}
			}
		}

	}

	private GPoint tailStart(GPoint newPoint) {
		for (int i = 3; i < penPoints.size(); i++) {
			GPoint current = penPoints.get(penPoints.size() - i);
			if (current.distance(newPoint) > 2 * MAX_POINT_DIST) {
				return null;
			}
			boolean anglesOK = true;
			for (int j = 1; j < i; j++) {
				if (angle(newPoint, penPoints.get(penPoints.size() - j),
						current, MAX_POINT_COS)) {
					anglesOK = false;
				}
			}
			if (anglesOK) {
				return current;
			}
		}
		return null;
	}

	private void addPointRepaint() {
		needsRepaint = true;
		view.repaintView();
	}

	private static boolean angle(GPoint a, GPoint b, GPoint c, double max) {
		if (a == null || b == null || c == null) {
			return true;
		}
		double dx1 = a.x - b.x;
		double dx2 = c.x - b.x;
		double dy1 = a.y - b.y;
		double dy2 = c.y - b.y;
		double ret = Math.abs(dx1 * dx2 + dy1 * dy2) / Math.hypot(dx1, dy1)
				/ Math.hypot(dx2, dy2);
		return Double.isNaN(ret) || ret < max;
	}

	/**
	 * Clean up the pen mode stuff, add points.
	 *
	 * @param right
	 *            true for right click
	 * @param x
	 *            x-coord
	 * @param y
	 *            y-coord
	 *
	 * @return true if a GeoElement was created
	 *
	 */
	public boolean handleMouseReleasedForPenMode(boolean right, int x, int y,
												 boolean isPinchZooming) {
		if (right && !isFreehand()) {
			return false;
		}

		if (isPinchZooming && penPoints.size() < 2) {
			penPoints.clear();
		}

		timer.start();

		app.setDefaultCursor();

		addPointsToPolyLine(penPoints);

		penPoints.clear();
		// drawing done, so no need for repaint
		needsRepaint = false;

		return true;
	}

	/**
	 * start timer to check if polyline is same stroke
	 */
	public void startTimer() {
		timer.start();
	}

	/**
	 * Reset the first point
	 */
	protected void resetInitialPoint() {
		if (this.deleteInitialPoint && this.initialPoint != null) {
			this.initialPoint.remove();
		}
		this.initialPoint = null;
	}

	private void addPointsToPolyLine(ArrayList<GPoint> penPoints) {
		Construction cons = app.getKernel().getConstruction();
		if (startNewStroke) {
			lastAlgo = null;
			startNewStroke = false;
		}

		ArrayList<MyPoint> newPts = new ArrayList<>(penPoints.size());
		for (GPoint p : penPoints) {
			double x = view.toRealWorldCoordX(p.getX());
			double y = view.toRealWorldCoordY(p.getY());

			// change -2.4600000000000004 to -2.46 for smaller XML
			newPts.add(new MyPoint(DoubleUtil.checkDecimalFraction(x),
					DoubleUtil.checkDecimalFraction(y)));
		}

		// don't set label
		if (lastAlgo != null) {
			lastAlgo.getPenStroke().appendPointArray(newPts);
			lastAlgo.getOutput(0).updateRepaint();
			return;
		}

		AlgoLocusStroke newPolyLine = new AlgoLocusStroke(cons, newPts);
		// set label
		newPolyLine.getOutput(0).setLabel(null);
		newPolyLine.getOutput(0).setTooltipMode(GeoElementND.TOOLTIP_OFF);
		lastAlgo = newPolyLine;

		GeoElement poly = newPolyLine.getOutput(0);

		poly.setLineThickness(penSize * PEN_SIZE_FACTOR);
		poly.setLineType(penLineStyle);
		poly.setLineOpacity(lineOpacity);
		poly.setObjColor(penColor);

		app.getSelectionManager().clearSelectedGeos(false);
		app.getSelectionManager().addSelectedGeo(poly);

		poly.setSelected(false);
		poly.updateRepaint();

		// app.storeUndoInfo() will be called from wrapMouseReleasedND
	}

	/**
	 * @param color
	 *            pen color
	 */
	public void setPenColor(GColor color) {
		if (!this.penColor.equals(color)) {
			startNewStroke = true;
		}
		this.penColor = color;
		lineDrawingColor = color;
	}

	/**
	 * Update state of the pen after geo is removed
	 * 
	 * @param geo
	 *            removed element
	 */
	public void remove(GeoElement geo) {
		if (geo.getParentAlgorithm() == this.lastAlgo) {
			lastAlgo = null;
		}

	}

	@Override
	public void onRun() {
		startNewStroke = true;
	}

	/**
	 * @return whether this is freehand pen tool
	 */
	public boolean isFreehand() {
		return false;
	}

	/**
	 * Paint on graphics if needed
	 * @param g2 graphics
	 */
	public void repaintIfNeeded(GGraphics2D g2) {
		if (needsRepaint) {
			doRepaintPreviewLine(g2);
		}
	}
}
