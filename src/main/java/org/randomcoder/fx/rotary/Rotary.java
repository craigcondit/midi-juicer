package org.randomcoder.fx.rotary;

import org.randomcoder.fx.util.PropUtils;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class Rotary extends Control {

	public static final PseudoClass POL_NORMAL_PSEUDO_CLASS = PseudoClass.getPseudoClass("pol-normal");
	public static final PseudoClass POL_REVERSED_PSEUDO_CLASS = PseudoClass.getPseudoClass("pol-reversed");
	public static final PseudoClass POL_NONE_PSEUDO_CLASS = PseudoClass.getPseudoClass("pol-none");

	private DoubleProperty value;
	private ObjectProperty<Polarity> polarity;

	public Rotary() {
		getStyleClass().add("rotary");
		getStylesheets().add(getClass().getResource("rotary.css").toExternalForm());
	}

	@Override
	protected Skin<Rotary> createDefaultSkin() {
		return new RotarySkin(this);
	}

	public DoubleProperty valueProperty() {
		if (value == null) {
			value = PropUtils.doubleProperty(this, "value", 0d);
		}
		return value;
	}

	public ObjectProperty<Polarity> polarityProperty() {
		if (polarity == null) {
			polarity = PropUtils.objectProperty(this, "polarity", Polarity.NORMAL, v -> {
				pseudoClassStateChanged(POL_NORMAL_PSEUDO_CLASS, v == Polarity.NORMAL);
				pseudoClassStateChanged(POL_REVERSED_PSEUDO_CLASS, v == Polarity.REVERSED);
				pseudoClassStateChanged(POL_NONE_PSEUDO_CLASS, v == Polarity.NONE);
			});
		}
		return polarity;
	}

	public double getValue() {
		return valueProperty().doubleValue();
	}

	public void setValue(double value) {
		this.valueProperty().set(value);
	}

	public Polarity getPolarity() {
		return polarityProperty().get();
	}

	public void setPolarity(Polarity polarity) {
		polarityProperty().set(polarity);
	}

}