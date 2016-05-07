import java.awt.Dimension;
import java.awt.GridLayout;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamPanel.DrawMode;
import com.github.sarxos.webcam.ds.ipcam.IpCamDriver;
import com.github.sarxos.webcam.ds.ipcam.IpCamStorage;


/**
 * Dasding is a radio from Germany. They have few network cameras available to
 * be viewed online. Here in this example we are creating IP camera device
 * working in PULL mode to request static JPEG images.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class JpegDasdingStudioExample {

	/**
	 * Remember to add IP camera driver JAR to the application classpath!
	 * Otherwise you will not be able to use IP camera driver features. Driver
	 * has to be set at the very beginning, before any webcam-capture related
	 * method is being invoked.
	 * 
	 * Here we are additionally using cameras storage which is based on simple
	 * XML file where all cameras which will be used are listed. By using
	 * cameras storage you do not have to register devices neither thru the
	 * registry nor driver instance. It's the simplest to deal with multiple
	 * cameras when you do not have to add/remove cameras in runtime.
	 */
	static {
		Webcam.setDriver(new IpCamDriver(new IpCamStorage("src/examples/resources/cameras.xml")));
	}

	public static void main(String[] args) throws MalformedURLException {

		JFrame f = new JFrame("Dasding Studio Live IP Cameras Demo");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLayout(new GridLayout(0, 3, 1, 1));

		List<WebcamPanel> panels = new ArrayList<WebcamPanel>();

		for (Webcam webcam : Webcam.getWebcams()) {

			WebcamPanel panel = new WebcamPanel(webcam, new Dimension(256, 144), false);
			panel.setDrawMode(DrawMode.FIT);
			panel.setFPSLimited(true);
			panel.setFPSLimit(0.2); // 0.2 FPS = 1 frame per 5 seconds
			panel.setBorder(BorderFactory.createEmptyBorder());

			f.add(panel);
			panels.add(panel);
		}

		f.pack();
		f.setVisible(true);

		for (WebcamPanel panel : panels) {
			panel.start();
		}
	}
}
