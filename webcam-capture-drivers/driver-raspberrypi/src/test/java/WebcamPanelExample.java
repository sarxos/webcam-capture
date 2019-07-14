import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;

import javax.swing.JButton;
import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamPanel.DrawMode;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.ds.raspberrypi.RaspividDriver;

public class WebcamPanelExample {
	static {
		/*
		 * Option one: you can pass raspi arguments here. all key starts with "raspi." will be accepted.
		 * or you can pass -Draspi.??? in your java startup argument list
		 */
		System.setProperty("raspi.framerate", "30");
		Webcam.setDriver(new RaspividDriver());
	}

	public static void main(String[] args) throws InterruptedException {
		final JFrame window = new JFrame("Raspberrypi Capture Example");
		window.setResizable(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.getContentPane().setLayout(new FlowLayout());

		final Dimension resolution = WebcamResolution.QVGA.getSize();

		for (final Webcam webcam : Webcam.getWebcams()) {
			webcam.setCustomViewSizes(resolution);
			webcam.setViewSize(resolution);
			/*
			 * option two: you can pass raspi arguments here. long or short argument name without dashes. Just name!
			 */
			webcam.setParameters(Collections.singletonMap("framerate", "30"));
			webcam.open();

			final WebcamPanel panel = new WebcamPanel(webcam);
			panel.setFPSDisplayed(true);
			panel.setDrawMode(DrawMode.FIT);
			panel.setImageSizeDisplayed(true);
			panel.setPreferredSize(resolution);

			window.getContentPane().add(panel);
		}

		OptionsPanel optionsPanel = new OptionsPanel();
		window.getContentPane().add(optionsPanel);

		JButton startButton = new JButton("Start");
		window.getContentPane().add(startButton);
		JButton stopButton = new JButton("Stop");
		window.getContentPane().add(stopButton);

		startButton.setEnabled(false);
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				WebcamPanel webcamPanel = (WebcamPanel) window.getContentPane().getComponent(0);
				if (!(webcamPanel.isStarted() || webcamPanel.isStarting())) {
					webcamPanel.getWebcam().setParameters(optionsPanel.getOptionMap());
					webcamPanel.start();
				}
				startButton.setEnabled(false);
				stopButton.setEnabled(true);
			}
		});

		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				WebcamPanel webcamPanel = (WebcamPanel) window.getContentPane().getComponent(0);
				if (webcamPanel.isStarted()) {
					webcamPanel.stop();
				}
				startButton.setEnabled(true);
				stopButton.setEnabled(false);
			}
		});

		window.pack();
		window.setVisible(true);
	}
}
