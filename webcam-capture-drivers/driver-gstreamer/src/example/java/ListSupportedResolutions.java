import java.awt.Dimension;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.ds.gstreamer.GStreamerDriver;


public class ListSupportedResolutions {

	// uncomment if you would like debug prints to be visible, and don't
	// forget to add logback JAR and XML file as well
	static {
		// WebcamLogConfigurator.configure("src/example/resources/logback.xml");
	}

	static {
		Webcam.setDriver(new GStreamerDriver());
	}

	public static void main(String[] args) {
		for (Webcam webcam : Webcam.getWebcams()) {
			System.out.println("webcam --- " + webcam.getName());
			for (Dimension size : webcam.getViewSizes()) {
				System.out.println("supported resolution: " + size.width + "x" + size.height);
			}
		}
	}
}
