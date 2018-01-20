import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamListener;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.ds.ffmpegcli.FFmpegCliDriver;


/**
 * Proof of concept of how to handle webcam video stream from Java with FFmpeg.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class WebcamViewerExample extends JFrame implements Runnable, WebcamListener, WindowListener {

	private static final long serialVersionUID = 1L;

	// IMPORTANT! Replace default driver by FFmpegCliDriver.
	static {
		Webcam.setDriver(new FFmpegCliDriver());
	}

	private Webcam webcam = null;
	private WebcamPanel viewer = null;

	@Override
	public void run() {

		setTitle("Java FFmpeg Capture");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(this);

		webcam = Webcam.getDefault();
		if (webcam == null) {
			System.out.println("No webcams has been found...");
			System.exit(1);
		}

		webcam.setViewSize(WebcamResolution.QVGA.getSize());
		webcam.addWebcamListener(WebcamViewerExample.this);

		viewer = new WebcamPanel(webcam, false);
		viewer.setFPSDisplayed(true);

		setContentPane(viewer);
		pack();
		setVisible(true);

		Thread t = new Thread() {

			@Override
			public void run() {
				viewer.start();
			}
		};
		t.setDaemon(true);
		t.start();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new WebcamViewerExample());
	}

	@Override
	public void webcamOpen(WebcamEvent we) {
		System.out.println("webcam open");
	}

	@Override
	public void webcamClosed(WebcamEvent we) {
		System.out.println("webcam closed");
	}

	@Override
	public void webcamDisposed(WebcamEvent we) {
		System.out.println("webcam disposed");
	}

	@Override
	public void webcamImageObtained(WebcamEvent we) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
		webcam.close();
	}

	@Override
	public void windowClosing(WindowEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		System.out.println("webcam viewer resumed");
		viewer.resume();
	}

	@Override
	public void windowIconified(WindowEvent e) {
		System.out.println("webcam viewer paused");
		viewer.pause();
	}
}
