import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


/**
 * This example demonstrates how to use Webcam Capture API via FXML in a JavaFX
 * application.
 * 
 * @author Rakesh Bhatt (rakeshbhatt10)
 */
public class WebCamAppLauncher extends Application {

	@Override
	public void start(Stage primaryStage) {

		Parent root = null;
		try {
			root = FXMLLoader.load(getClass().getResource("/fxml/WebCamPreview.fxml"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Scene scene = new Scene(root, 900, 690);

		primaryStage.setTitle("WebCam Capture Sarxos API using JavaFx with FXML ");
		primaryStage.setScene(scene);
		primaryStage.centerOnScreen();
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
