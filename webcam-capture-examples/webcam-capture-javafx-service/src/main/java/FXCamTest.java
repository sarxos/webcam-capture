import com.github.sarxos.webcam.Webcam;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class FXCamTest extends Application {
	
	private WebCamService service ;
	
	@Override
	public void init() {
		
		// note this is in init as it **must not** be called on the FX Application Thread:

		Webcam cam = Webcam.getWebcams().get(0);
		service = new WebCamService(cam);	
	}

	@Override
	public void start(Stage primaryStage) {

		Button startStop = new Button();
		startStop.textProperty().bind(Bindings.
				when(service.runningProperty()).
				then("Stop").
				otherwise("Start"));
		
		startStop.setOnAction(e -> {
			if (service.isRunning()) {
				service.cancel();
			} else {
				service.restart();
			}
		});
		
		WebCamView view = new WebCamView(service);
		
		BorderPane root = new BorderPane(view.getView());
		BorderPane.setAlignment(startStop, Pos.CENTER);
		BorderPane.setMargin(startStop, new Insets(5));
		root.setBottom(startStop);
		
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	


	public static void main(String[] args) {
		launch(args);
	}
}

