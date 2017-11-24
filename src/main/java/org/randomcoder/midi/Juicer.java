package org.randomcoder.midi;

import org.randomcoder.fx.rotary.Rotary;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;

public class Juicer extends Application {

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		GridPane pane = new GridPane();

		int columns = 5;
		int rows = 3;

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				Rotary rotary = new Rotary();
				rotary.setMinValue(0);
				rotary.setMaxValue(16383);
				rotary.setCurrentValue(8191);
				rotary.setLabelValueGenerator(r -> String.format("%.2f %%", r.getPercentage() * 100));
				rotary.setLabelValueHandler((r, s) -> {
					StringBuilder buf = new StringBuilder();
					for (char c : s.toCharArray()) {
						if ((c >= '0' && c <= '9') || c == '.' || c == '-') {
							buf.append(c);
						}
					}
					r.setPercentage(Double.parseDouble(buf.toString()) / 100);
				});
				rotary.setAutomated((i * columns + j) % 3 == 0);

				pane.add(rotary, j, i);
			}
		}

		for (int i = 0; i < columns; i++) {
			ColumnConstraints cc = new ColumnConstraints();
			cc.setHgrow(Priority.ALWAYS);
			pane.getColumnConstraints().add(cc);
		}

		for (int i = 0; i < rows; i++) {
			RowConstraints rc = new RowConstraints();
			rc.setVgrow(Priority.ALWAYS);
			pane.getRowConstraints().add(rc);
		}

		Scene scene = new Scene(pane);

		primaryStage.setScene(scene);
		primaryStage.setTitle("Rotary control test");
		primaryStage.show();

	}

}
