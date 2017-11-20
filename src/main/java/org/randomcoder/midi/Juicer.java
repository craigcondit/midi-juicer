package org.randomcoder.midi;

import org.randomcoder.fx.rotary.Rotary;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Juicer extends Application {

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Rotary rotary = new Rotary();
		Slider slider = new Slider(0, 100, 50);

		slider.valueProperty().addListener((o, oldVal, newVal) -> rotary.setValue(newVal.doubleValue() / 100));
		rotary.valueProperty().addListener((o, oldVal, newVal) -> slider.setValue(newVal.doubleValue() * 100));
		VBox main = new VBox(1, rotary, slider);
		rotary.setValue(slider.getValue() / 100);
		Scene scene = new Scene(main);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Rotary control test");
		primaryStage.show();
	}

}
