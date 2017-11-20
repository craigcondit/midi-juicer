package org.randomcoder.fx.rotary;

import java.util.concurrent.atomic.AtomicBoolean;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Line;

public class RotarySkin extends SkinBase<Rotary> {

	private static final double PREFERRED_WIDTH = 500;
	private static final double PREFERRED_HEIGHT = 500;
	private static final double MINIMUM_WIDTH = 50;
	private static final double MINIMUM_HEIGHT = 50;
	private static final double MAXIMUM_WIDTH = 1024;
	private static final double MAXIMUM_HEIGHT = 1024;

	private double size;
	private double width;
	private double height;

	private Rotary control;
	private StackPane pane;
	private Arc arcBack;
	private Arc arcFore;
	private Line pointer;

	private final AtomicBoolean dragging = new AtomicBoolean(false);

	public RotarySkin(final Rotary control) {
		super(control);
		this.control = control;
		init();
		initGraphics();
		resize();
		registerListeners();
		registerHandlers();
	}

	private boolean unset(double prop) {
		return Double.compare(prop, 0.0) <= 0;
	}

	private void init() {
		if (unset(control.getPrefWidth()) || unset(control.getPrefHeight()) || unset(control.getWidth())
				|| unset(control.getHeight())) {
			if (control.getPrefWidth() > 0 && control.getPrefHeight() > 0) {
				control.setPrefSize(control.getPrefWidth(), control.getPrefHeight());
			} else {
				control.setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
			}
		}
		if (unset(control.getMinWidth()) || unset(control.getMinHeight())) {
			control.setMinSize(MINIMUM_WIDTH, MINIMUM_HEIGHT);
		}
		if (unset(control.getMaxWidth()) || unset(control.getMinHeight())) {
			control.setMaxSize(MAXIMUM_WIDTH, MAXIMUM_HEIGHT);
		}
	}

	private void initGraphics() {
		arcBack = new Arc();
		arcBack.getStyleClass().addAll("rotary-arc", "rotary-arc-background");

		arcFore = new Arc();
		arcFore.getStyleClass().addAll("rotary-arc", "rotary-arc-foreground");

		pointer = new Line();
		pointer.getStyleClass().addAll("rotary-pointer");

		pane = new StackPane(arcBack, arcFore, pointer);
		pane.getStylesheets().addAll(control.getStylesheets());
		pane.getStyleClass().addAll("rotary-container");
		pane.setPrefSize(control.getPrefWidth(), control.getPrefHeight());
		pane.setMinSize(control.getMinWidth(), control.getMinHeight());
		pane.setMaxSize(control.getMaxWidth(), control.getMaxHeight());

		getChildren().setAll(pane);
	}

	private void registerListeners() {
		control.widthProperty().addListener(o -> resize());
		control.heightProperty().addListener(o -> resize());
		control.valueProperty().addListener((o, ov, nv) -> reposition());
		control.polarityProperty().addListener((o, ov, nv) -> reposition());
		arcFore.centerXProperty().addListener((o, ov, nv) -> reposition());
		arcFore.centerYProperty().addListener((o, ov, nv) -> reposition());
	}

	private void registerHandlers() {
		pane.onMousePressedProperty().set(this::mousePressed);
		pane.onMouseReleasedProperty().set(this::mouseReleased);
		pane.onMouseDraggedProperty().set(this::mouseDragged);
	}

	private void mousePressed(MouseEvent e) {
		// verify that we're pressed within the bounding box of the arc
		if (inBounds(e, pane)) {
			e.setDragDetect(true);
			dragging.set(true);
			mouseDragged(e);
		}
	}

	private Point2D getLocalCoords(MouseEvent e, Node c) {
		return c.sceneToLocal(e.getSceneX(), e.getSceneY());
	}

	private boolean inBounds(MouseEvent e, Node c) {
		return c.getBoundsInLocal().contains(getLocalCoords(e, c));
	}

	private void mouseReleased(MouseEvent e) {
		dragging.set(false);
	}

	private void mouseDragged(MouseEvent e) {
		if (!dragging.get()) {
			return;
		}

		double sceneX = e.getSceneX();
		double sceneY = e.getSceneY();
		double centerX = arcFore.getCenterX();
		double centerY = arcFore.getCenterY();
		Point2D coords = arcFore.sceneToLocal(sceneX, sceneY);
		double localX = coords.getX();
		double localY = coords.getY();
		double deltaX = centerX - localX;
		double deltaY = localY - centerY;
		double r = Math.atan2(deltaX, deltaY);
		double deg = Math.toDegrees(r);

		// convert negative degrees to positive
		if (deg < 0) {
			deg += 360;
		}

		// offset and normalize
		double offsetDegrees = deg - 30;

		if (offsetDegrees < 0) {
			offsetDegrees = 0;
		}
		if (offsetDegrees > 300) {
			offsetDegrees = 300;
		}

		control.setValue(offsetDegrees / 300);
	}

	private void resize() {
		width = control.getWidth();
		height = control.getHeight();

		size = Math.min(width, height);

		if (width > 0 && height > 0) {
			pane.setMaxSize(width, height);
			pane.relocate((width - size) * 0.5, (height - size) * 0.5);

			double radius = size * 0.5;
			double padding = radius * 0.10;
			radius -= padding;

			double strokeWidth = radius * 0.15;
			radius -= strokeWidth;

			arcBack.setManaged(false);
			arcBack.setCenterX(width * 0.5);
			arcBack.setCenterY(height * 0.5);
			arcBack.setRadiusX(radius);
			arcBack.setRadiusY(radius);
			arcBack.setStrokeWidth(strokeWidth);
			arcBack.setStartAngle(-120);

			arcFore.setManaged(false);
			arcFore.setCenterX(width * 0.5);
			arcFore.setCenterY(height * 0.5);
			arcFore.setRadiusX(radius);
			arcFore.setRadiusY(radius);
			arcFore.setStrokeWidth(strokeWidth);

			pointer.setManaged(false);
			pointer.setStrokeWidth(strokeWidth);

			reposition();
		}
	}

	private void reposition() {

		double r = Math.toRadians((control.getValue() * 300) + 120);
		double dx = arcFore.getRadiusX() * Math.cos(r);
		double dy = arcFore.getRadiusY() * Math.sin(r);

		arcBack.setLength(-300);

		switch (control.getPolarity()) {
		case NORMAL:
			arcFore.setStartAngle(-120);
			arcFore.setLength(control.getValue() * -300);
			arcFore.setVisible(true);
			break;
		case REVERSED:
			arcFore.setStartAngle(300);
			arcFore.setLength(300 - (control.getValue() * 300));
			arcFore.setVisible(true);
			break;
		case NONE:
			arcFore.setVisible(false);
			break;
		}

		pointer.setStartX(arcFore.getCenterX());
		pointer.setEndX(arcFore.getCenterX() + dx);
		pointer.setStartY(arcFore.getCenterY());
		pointer.setEndY(arcFore.getCenterY() + dy);
	}

}