import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamPanel.DrawMode;
import com.github.sarxos.webcam.ds.ipcam.IpCamDeviceRegistry;
import com.github.sarxos.webcam.ds.ipcam.IpCamDriver;
import com.github.sarxos.webcam.ds.ipcam.IpCamMode;

@SuppressWarnings("serial")
public class LignanoBeachPushModeIpCameraExample extends JFrame {

	// IMPORTANT! For IP camera you have to use IpCamDriver

	static {
		Webcam.setDriver(new IpCamDriver());
	}

	// IMPORTANT! IP cameras are not automatically discovered like USB, you have
	// to register them manually

	static {
		try {
			IpCamDeviceRegistry.register("Lignano Beach IP Camera", "http://195.31.81.138/mjpg/video.mjpg", IpCamMode.PUSH);
		} catch (MalformedURLException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Action to be performed when Snapshot JButton is clicked.
	 */
	private class SnapMeAction extends AbstractAction {

		public SnapMeAction() {
			super("Snapshot");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				for (int i = 0; i < webcams.size(); i++) {
					Webcam webcam = webcams.get(i);
					File file = new File(String.format("test-%d.jpg", i));
					ImageIO.write(webcam.getImage(), "JPG", file);
					System.out.format("Image for %s saved in %s \n", webcam.getName(), file);
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Action to be performed when Start JButton is clicked.
	 */
	private class StartAction extends AbstractAction implements Runnable {

		public StartAction() {
			super("Start");
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			btStart.setEnabled(false);
			btSnapMe.setEnabled(true);

			// remember to start panel asynchronously - otherwise GUI will be
			// blocked while OS is opening webcam HW (will have to wait for
			// webcam to be ready) and this causes GUI to hang, stop responding
			// and repainting

			executor.execute(this);
		}

		@Override
		public void run() {

			btStop.setEnabled(true);

			for (WebcamPanel panel : panels) {
				panel.start();
			}
		}
	}

	/**
	 * Action to be performed when Stop JButton is clicked.
	 */
	private class StopAction extends AbstractAction {

		public StopAction() {
			super("Stop");
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			btStart.setEnabled(true);
			btSnapMe.setEnabled(false);
			btStop.setEnabled(false);

			for (WebcamPanel panel : panels) {
				panel.stop();
			}
		}
	}

	private Executor executor = Executors.newSingleThreadExecutor();

	private List<Webcam> webcams = Webcam.getWebcams();
	private List<WebcamPanel> panels = new ArrayList<WebcamPanel>();

	private JButton btSnapMe = new JButton(new SnapMeAction());
	private JButton btStart = new JButton(new StartAction());
	private JButton btStop = new JButton(new StopAction());

	public LignanoBeachPushModeIpCameraExample() {

		super("Lignano Beach IP Camera Example");

		setLayout(new FlowLayout());

		JLabel label = new JLabel("Please wait... IP camera user interface initialization in progress");
		add(label);

		pack();
		setVisible(true);

		for (final Webcam webcam : webcams) {
			final Dimension size = webcam.getViewSizes()[0];
			webcam.setViewSize(size);
			final WebcamPanel panel = new WebcamPanel(webcam, size, false);
			panel.setFPSDisplayed(true);
			panel.setDrawMode(DrawMode.FIT);
			panels.add(panel);
		}

		// start application with disable snapshot button - we enable it when
		// webcam is started

		btSnapMe.setEnabled(false);
		btStop.setEnabled(false);

		for (WebcamPanel panel : panels) {
			add(panel);
		}

		add(btSnapMe);
		add(btStart);
		add(btStop);

		label.setVisible(false);

		pack();
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public static void main(String[] args) {
		new LignanoBeachPushModeIpCameraExample();
	}
}
