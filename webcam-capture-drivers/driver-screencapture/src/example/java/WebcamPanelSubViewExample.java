import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamCompositeDriver;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamPanel.DrawMode;
import com.github.sarxos.webcam.WebcamPanel.Painter;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.ds.buildin.WebcamDefaultDriver;
import com.github.sarxos.webcam.ds.screencapture.ScreenCaptureDriver;


public class WebcamPanelSubViewExample {

	static {
		Webcam.setDriver(new WebcamCompositeDriver(new WebcamDefaultDriver(), new ScreenCaptureDriver()));
	}

	private final Webcam webcam = Webcam.getWebcams().get(0);
	private final Webcam screen = Webcam.getWebcams().get(1);
	private final Dimension webcamSize = WebcamResolution.QVGA.getSize();
	private final Dimension screenSize = WebcamResolution.QHD.getSize();
	private final WebcamPanel panel = new WebcamPanel(screen, false);
	private final Painter dp = panel.getDefaultPainter();
	private final JFrame window = new JFrame("Test");

	private final class SubViewPainter implements Painter {

		private final int x = screenSize.width - webcamSize.width - 5;
		private final int y = screenSize.height - webcamSize.height - 21;
		private final int w = webcamSize.width;
		private final int h = webcamSize.height;

		@Override
		public void paintImage(WebcamPanel owner, BufferedImage image, Graphics2D g2) {

			dp.paintImage(owner, image, g2);

			g2.setColor(Color.BLACK);
			g2.drawRect(x - 1, y - 1, w + 1, h + 1);
			g2.drawImage(webcam.getImage(), x, y, null);
		}

		@Override
		public void paintPanel(WebcamPanel panel, Graphics2D g2) {
			dp.paintPanel(panel, g2);
		};
	};

	public WebcamPanelSubViewExample() {

		screen.setCustomViewSizes(screenSize);
		screen.setViewSize(screenSize);
		screen.open(true);

		panel.setFPSLimit(30);
		panel.setFPSDisplayed(true);
		panel.setDrawMode(DrawMode.NONE);
		panel.setImageSizeDisplayed(true);
		panel.setPainter(new SubViewPainter());
		panel.setPreferredSize(screenSize);

		webcam.setViewSize(webcamSize);
		webcam.open(true);

		window.setPreferredSize(screenSize);
		window.setResizable(false);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLayout(null);
		window.setContentPane(panel);
		window.pack();
		window.setVisible(true);

		panel.start();
	}

	public static void main(String[] args) {
		new WebcamPanelSubViewExample();
	}
}
