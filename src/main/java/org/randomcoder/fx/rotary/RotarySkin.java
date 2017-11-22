package org.randomcoder.fx.rotary;

import java.util.concurrent.atomic.AtomicBoolean;

import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class RotarySkin extends SkinBase<Rotary> {

	private static final double H_W_RATIO = 0.9;
	
	private static final double PREFERRED_HEIGHT = 200;
	private static final double PREFERRED_WIDTH = PREFERRED_HEIGHT * H_W_RATIO;
	private static final double MINIMUM_HEIGHT = 20;
	private static final double MINIMUM_WIDTH = MINIMUM_HEIGHT * H_W_RATIO;
	private static final double MAXIMUM_HEIGHT = 1024;
	private static final double MAXIMUM_WIDTH = MAXIMUM_HEIGHT * H_W_RATIO;

	private double size;
	private double width;
	private double height;

	private Rotary control;
	private StackPane pane;
	private Arc arcBack;
	private Arc arcFore;
	private Line pointer;
	private Label label;

	private final AtomicBoolean dragging = new AtomicBoolean(false);

	public RotarySkin(final Rotary control) {
		super(control);
		this.control = control;
		init();
		initGraphics();
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
		if (unset(control.getMaxWidth()) || unset(control.getMaxHeight())) {
			control.setMaxSize(MAXIMUM_WIDTH, MAXIMUM_HEIGHT);
		}
	}

	private void initGraphics() {
		label = new Label();
		label.getStyleClass().addAll("rotary-label");

		arcBack = new Arc();
		arcBack.getStyleClass().addAll("rotary-arc", "rotary-arc-background");

		arcFore = new Arc();
		arcFore.getStyleClass().addAll("rotary-arc", "rotary-arc-foreground");

		pointer = new Line();
		pointer.getStyleClass().addAll("rotary-pointer");

		pane = new StackPane(arcBack, arcFore, pointer, label);
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
		control.percentageProperty().addListener((o, ov, nv) -> update());
		control.polarityProperty().addListener((o, ov, nv) -> update());
		arcFore.centerXProperty().addListener((o, ov, nv) -> update());
		arcFore.centerYProperty().addListener((o, ov, nv) -> update());
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

		control.setPercentage(offsetDegrees / 300);
	}

	private void resize() {
		width = control.getWidth();
		height = control.getHeight();
		size = Math.min(width, H_W_RATIO * height);

		if (width > 0 && height > 0) {
			pane.relocate((width - size) * 0.5, (height - size) * 0.5);

			double radius = size * 0.5;
			double padding = radius * 0.10;
			radius -= padding;

			double strokeWidth = radius * 0.10;
			radius -= strokeWidth;

			arcBack.setManaged(false);
			arcBack.setCenterX(width * 0.5);
			arcBack.setCenterY(radius + padding + strokeWidth);
			arcBack.setRadiusX(radius);
			arcBack.setRadiusY(radius);
			arcBack.setStrokeWidth(strokeWidth);
			arcBack.setStartAngle(-120);
			arcBack.setLength(-300);

			arcFore.setManaged(false);
			arcFore.setCenterX(width * 0.5);
			arcFore.setCenterY(radius + padding + strokeWidth);
			arcFore.setRadiusX(radius);
			arcFore.setRadiusY(radius);
			arcFore.setStrokeWidth(strokeWidth);
			arcFore.setStartAngle(-120);
			arcFore.setLength(-300);

			pointer.setManaged(false);
			pointer.setStrokeWidth(strokeWidth);

			label.setManaged(true);
			label.setTextAlignment(TextAlignment.CENTER);
			label.setWrapText(false);
			label.setMouseTransparent(false);
			label.setFont(Font.font("Tahoma", FontWeight.NORMAL, FontPosture.REGULAR, radius * 0.4));
			label.setAlignment(Pos.TOP_CENTER);
			label.setPrefSize(radius * 2, radius * 0.5);
			label.setMinWidth(Region.USE_PREF_SIZE);
			label.setMaxWidth(Region.USE_PREF_SIZE);
			label.setMinHeight(Region.USE_PREF_SIZE);
			label.setMaxHeight(Region.USE_PREF_SIZE);
			label.setTranslateY(radius * 2.2);
			label.setVisible(true);

			update();
		}
	}

	private void update() {
		double value = control.getPercentage();

		double r = Math.toRadians(value * 300 + 120);
		double dx = arcFore.getRadiusX() * Math.cos(r);
		double dy = arcFore.getRadiusY() * Math.sin(r);

		switch (control.getPolarity()) {
		case NORMAL:
			arcFore.setStartAngle(-120);
			arcFore.setLength(value * -300);
			arcFore.setVisible(true);
			break;
		case REVERSED:
			arcFore.setStartAngle(300);
			arcFore.setLength(300 - (value * 300));
			arcFore.setVisible(true);
			break;
		case NONE:
			arcFore.setStartAngle(-120);
			arcFore.setLength(value * -300);
			arcFore.setVisible(false);
			break;
		}

		pointer.setStartX(arcBack.getCenterX());
		pointer.setEndX(arcBack.getCenterX() + dx);
		pointer.setStartY(arcBack.getCenterY());
		pointer.setEndY(arcBack.getCenterY() + dy);

		label.setText(control.getCurrentValueText());
	}

}