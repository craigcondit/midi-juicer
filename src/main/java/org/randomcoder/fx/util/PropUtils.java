package org.randomcoder.fx.util;

import java.util.function.Consumer;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;

public class PropUtils {

    public static DoubleProperty doubleProperty(Object bean, String name, double initialValue) {
	return new DoublePropertyBase(initialValue) {

	    @Override
	    public String getName() {
		return name;
	    }

	    @Override
	    public Object getBean() {
		return bean;
	    }
	};
    }

    public static BooleanProperty booleanProperty(Object bean, String name, boolean initialValue) {
	return new BooleanPropertyBase(initialValue) {

	    @Override
	    public String getName() {
		return name;
	    }

	    @Override
	    public Object getBean() {
		return bean;
	    }
	};
    }

    public static <T> ObjectProperty<T> objectProperty(
	    Object bean, String name, T initialValue) {

	return new ObjectPropertyBase<T>(initialValue) {

	    @Override
	    public String getName() {
		return name;
	    }

	    @Override
	    public Object getBean() {
		return bean;
	    }
	};
    }

    public static <T> ObjectProperty<T> objectProperty(
	    Object bean, String name, T initialValue, Consumer<T> invalidated) {

	return new ObjectPropertyBase<T>(initialValue) {

	    @Override
	    protected void invalidated() {
		invalidated.accept(get());
	    }

	    @Override
	    public String getName() {
		return name;
	    }

	    @Override
	    public Object getBean() {
		return bean;
	    }
	};
    }

}
