package cv1;

import ij.ImagePlus;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

	@Override
	public void start(Stage primaryStage) {
		ImagePlus originalImage = new ImagePlus("images/lena_std.jpg");
		Process main = new Process(originalImage);
		originalImage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
