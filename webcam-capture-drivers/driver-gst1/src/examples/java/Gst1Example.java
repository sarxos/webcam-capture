import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamViewer;
import com.github.sarxos.webcam.ds.gst1.Gst1Driver;


public class Gst1Example {

	static {
		Webcam.setDriver(new Gst1Driver());
	}

	public static void main(String[] args) throws InterruptedException {
		new WebcamViewer();
	}
}
