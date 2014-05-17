package main;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Main extends Application {
	
	private Stage stage;
	
	@Override
	public void start(Stage s) throws Exception {
		stage = s;
		s.setTitle("A Space Odyssey jk help me think of a good title");
		Group root = new Group();
		stage.setScene(new Scene(root));
		GUI g = new GUI(s);
		root.getChildren().addAll(g);
		s.show();
	}
	
	public static void main(String[] args) {
		Application.launch(args);
	}
}
