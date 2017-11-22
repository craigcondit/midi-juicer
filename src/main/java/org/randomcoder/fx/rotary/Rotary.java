package org.randomcoder.fx.rotary;

import org.randomcoder.fx.util.PropUtils;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class Rotary extends Control {

	@FunctionalInterface
	public interface LabelMapper {
		public String map(Rotary rotary, double value);
	}

	public static final PseudoClass POL_NORMAL_PSEUDO_CLASS = PseudoClass.getPseudoClass("pol-normal");
	public static final PseudoClass POL_REVERSED_PSEUDO_CLASS = PseudoClass.getPseudoClass("pol-reversed");
	public static final PseudoClass POL_NONE_PSEUDO_CLASS = PseudoClass.getPseudoClass("pol-none");

	private final DoubleProperty percentage;
	private final DoubleProperty minValue;
	private final DoubleProperty maxValue;
	private final ObjectProperty<Polarity> polarity;
	private final ObjectProperty<LabelMapper> labelMapper;

	public Rotary() {
		getStyleClass().add("rotary");
		getStylesheets().add(getClass().getResource("rotary.css").toExternalForm());
		percentage = createPercentage();
		minValue = createMinValue();
		maxValue = createMaxValue();
		polarity = createPolarity();
		labelMapper = createLabelMapper();
	}

	private DoubleProperty createPercentage() {
		return PropUtils.doubleProperty(this, "percentage", 0d);
	}

	private DoubleProperty createMinValue() {
		return PropUtils.doubleProperty(this, "minValue", 0d);
	}

	private DoubleProperty createMaxValue() {
		return PropUtils.doubleProperty(this, "maxValue", 0d);
	}

	private ObjectProperty<Polarity> createPolarity() {
		return PropUtils.objectProperty(this, "polarity", Polarity.NORMAL, v -> {
			pseudoClassStateChanged(POL_NORMAL_PSEUDO_CLASS, v == Polarity.NORMAL);
			pseudoClassStateChanged(POL_REVERSED_PSEUDO_CLASS, v == Polarity.REVERSED);
			pseudoClassStateChanged(POL_NONE_PSEUDO_CLASS, v == Polarity.NONE);
		});
	}

	private ObjectProperty<LabelMapper> createLabelMapper() {
		return PropUtils.objectProperty(this, "labelMapper", (t, d) -> String.format("%d", (int) Math.round(d)));
	}

	@Override
	protected Skin<Rotary> createDefaultSkin() {
		return new RotarySkin(this);
	}

	public DoubleProperty percentageProperty() {
		return percentage;
	}

	public DoubleProperty minValueProperty() {
		return minValue;
	}

	public DoubleProperty maxValueProperty() {
		return maxValue;
	}

	public ObjectProperty<LabelMapper> labelMapperProperty() {
		return labelMapper;
	}

	public void setLabelMapper(LabelMapper labelMapper) {
		labelMapperProperty().set(labelMapper);
	}

	public LabelMapper getLabelMapper() {
		return labelMapperProperty().get();
	}

	protected double calculatePercentageFromCurrent(double currentValue) {
		double max = maxValueProperty().get();
		double min = minValueProperty().get();

		if (min == max) {
			return 0d;
		}

		// bounds check
		if (min < max) {
			currentValue = Math.min(Math.max(currentValue, min), max);
		} else {
			currentValue = Math.min(Math.max(currentValue, max), min);
		}

		double range = Math.abs(max - min);
		double scaled = currentValue - min;
		double percentage = scaled / range;

		return percentage;
	}

	protected double calculateCurrentFromPercentage() {
		double min = minValueProperty().get();
		double max = maxValueProperty().get();
		double percentage = percentageProperty().get();

		double range = max - min;
		double scaled = range * percentage;
		double offset = scaled + min;

		return offset;
	}

	public ObjectProperty<Polarity> polarityProperty() {
		return polarity;
	}

	public double getMinValue() {
		return minValueProperty().get();
	}

	public void setMinValue(double minValue) {
		minValueProperty().set(minValue);
	}

	public double getMaxValue() {
		return maxValueProperty().get();
	}

	public void setMaxValue(double maxValue) {
		maxValueProperty().set(maxValue);
	}

	public double getCurrentValue() {
		return calculateCurrentFromPercentage();
	}

	public String getCurrentValueText() {
		return labelMapperProperty().get().map(this, getCurrentValue());
	}

	public void setCurrentValue(double currentValue) {
		setPercentage(calculatePercentageFromCurrent(currentValue));
	}

	public double getPercentage() {
		return percentageProperty().get();
	}

	public void setPercentage(double percentage) {
		this.percentageProperty().set(Math.min(Math.max(percentage, 0d), 1d));
	}

	public Polarity getPolarity() {
		return polarityProperty().get();
	}

	public void setPolarity(Polarity polarity) {
		polarityProperty().set(polarity);
	}

}