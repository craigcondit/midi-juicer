package org.randomcoder.midi;

import org.randomcoder.fx.rotary.Polarity;
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

		int columns = 6;
		int rows = 1;

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				Rotary rotary = new Rotary();
				rotary.setPercentage(0.3 + 0.05 * (i * columns + j));
				rotary.setPolarity(Polarity.values()[(i * columns + j) % 3]);
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
