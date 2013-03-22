

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamDiscoveryEvent;
import com.github.sarxos.webcam.WebcamDiscoveryListener;

public class WebcamDiscoveryListenerExample implements WebcamDiscoveryListener {

	public WebcamDiscoveryListenerExample() {
		for (Webcam webcam : Webcam.getWebcams()) {
			System.out.println("This webcam has been found in the system: " + webcam.getName());
		}
		Webcam.addDiscoveryListener(this);
		System.out.println("Now, please connect additional webcam, or disconnect already connected one.");
	}

	@Override
	public void webcamFound(WebcamDiscoveryEvent event) {
		System.out.println("Oh! Thou, webcam has been connected! " + event.getWebcam().getName());
	}

	@Override
	public void webcamGone(WebcamDiscoveryEvent event) {
		System.out.println("Did I miss something? Webcam has been disconnected! " + event.getWebcam().getName());
	}

	public static void main(String[] args) throws Throwable {
		new WebcamDiscoveryListenerExample();
		Thread.sleep(120000);
		System.out.println("Bye!");
	}
}
