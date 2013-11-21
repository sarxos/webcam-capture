import java.awt.Dimension;

import javax.swing.JApplet;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;


public class WebcamAppletExample extends JApplet {

	private static final long serialVersionUID = 3517366452510566924L;

	private Dimension size = WebcamResolution.QVGA.getSize();
	private Webcam webcam = null;
	private WebcamPanel panel = null;

	public WebcamAppletExample() {
		super();
		System.out.println("Construct");
	}

	@Override
	public void start() {

		System.out.println("Start");

		super.start();

		webcam = Webcam.getDefault();
		webcam.setViewSize(size);

		panel = new WebcamPanel(webcam, false);
		panel.setFPSDisplayed(true);

		add(panel);

		if (webcam.isOpen()) {
			webcam.close();
		}

		int i = 0;
		do {
			if (webcam.getLock().isLocked()) {
				System.out.println("Waiting for lock to be released " + i);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
					return;
				}
			} else {
				break;
			}
		} while (i++ < 3);

		webcam.open();
		panel.start();
	}

	@Override
	public void destroy() {
		System.out.println("Destroy");
		webcam.close();
		Webcam.shutdown();
		System.out.println("Destroyed");
	}

	@Override
	public void stop() {
		System.out.println("Stop");
		webcam.close();
		System.out.println("Stopped");
	}

	@Override
	public void init() {
		System.out.println("Init");
	}
}
