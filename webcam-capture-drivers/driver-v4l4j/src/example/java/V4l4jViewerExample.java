import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamViewer;
import com.github.sarxos.webcam.ds.v4l4j.V4l4jDriver;


public class V4l4jViewerExample {

	static {
		Webcam.setDriver(new V4l4jDriver());
	}

	public static void main(String[] args) {
		new WebcamViewer();
	}
}
