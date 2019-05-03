package MPEG;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * This java class contains Main loader.
 * 
 * @author Yehor Safonov; id: 185942
 */

public class Main extends Application {

	@Override
	public void start(Stage primaryStage) {
		Parent root = null;
		try {
			root = FXMLLoader.load(getClass().getResource("Interface.fxml"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		Scene scene = new Scene(root);
		primaryStage.setResizable(false);
		primaryStage.setTitle("MPEG application (Yehor Safonov)");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
