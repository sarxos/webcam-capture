import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.ds.vlcj.VlcjDriver;

import uk.co.caprica.vlcj.medialist.MediaListItem;

public class WebcamRtspExample {

	static {
		String name = "Big Buck Bunny";
		String rtsp = "rtsp://184.72.239.149/vod/mp4:BigBuckBunny_175k.mov";
		Webcam.setDriver(new VlcjDriver(Arrays.asList(new MediaListItem(name, rtsp, new ArrayList<MediaListItem>()))));
	}

	public static void main(String[] args) throws InterruptedException, IOException {

		Webcam webcam = Webcam.getWebcams().get(0);
		webcam.setViewSize(WebcamResolution.VGA.getSize());
		WebcamPanel panel = new WebcamPanel(webcam);

		JFrame window = new JFrame("Webcam Panel");
		window.add(panel);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.pack();
		window.setVisible(true);
	}
}
