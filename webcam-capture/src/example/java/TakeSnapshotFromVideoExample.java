import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;


@SuppressWarnings("serial")
public class TakeSnapshotFromVideoExample extends JFrame {

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

	private Dimension size = WebcamResolution.QQVGA.getSize();

	private List<Webcam> webcams = Webcam.getWebcams();
	private List<WebcamPanel> panels = new ArrayList<WebcamPanel>();

	private JButton btSnapMe = new JButton(new SnapMeAction());
	private JButton btStart = new JButton(new StartAction());
	private JButton btStop = new JButton(new StopAction());

	public TakeSnapshotFromVideoExample() {

		super("Test Snap Different Size");

		for (Webcam webcam : webcams) {
			webcam.setViewSize(size);
			WebcamPanel panel = new WebcamPanel(webcam, size, false);
			panel.setFPSDisplayed(true);
			panel.setFillArea(true);
			panels.add(panel);
		}

		// start application with disable snapshot button - we enable it when
		// webcam is started

		btSnapMe.setEnabled(false);
		btStop.setEnabled(false);

		setLayout(new FlowLayout());

		for (WebcamPanel panel : panels) {
			add(panel);
		}

		add(btSnapMe);
		add(btStart);
		add(btStop);

		pack();
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public static void main(String[] args) {
		new TakeSnapshotFromVideoExample();
	}
}
