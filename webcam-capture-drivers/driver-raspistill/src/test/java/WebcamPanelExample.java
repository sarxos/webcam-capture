import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamPanel.DrawMode;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.ds.raspistill.RaspistillDriver;

public class WebcamPanelExample {
	static {
		Webcam.setAutoOpenMode(false);
		Webcam.setDriver(new RaspistillDriver());
	}

	public static void main(String[] args) throws InterruptedException {
		final JFrame window = new JFrame("Raspistill Capture Example");
		window.setResizable(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.getContentPane().setLayout(new FlowLayout());
		
		final Dimension resolution = WebcamResolution.QVGA.getSize();

		List<Webcam> cams=Webcam.getWebcams();
		for (final Webcam webcam : cams) {
			webcam.setCustomViewSizes(resolution);
			webcam.setViewSize(resolution);
			webcam.open();

			final WebcamPanel panel = new WebcamPanel(webcam);
			panel.setFPSDisplayed(true);
			panel.setDrawMode(DrawMode.FIT);
			panel.setImageSizeDisplayed(true);
			panel.setPreferredSize(resolution);

			window.getContentPane().add(panel);
		}
		
		OptionsPanel optionsPanel=new OptionsPanel(cams);
		window.getContentPane().add(optionsPanel);

		window.pack();
		window.setVisible(true);
	}
}
