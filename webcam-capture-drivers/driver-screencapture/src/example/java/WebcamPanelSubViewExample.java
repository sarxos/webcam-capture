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
import com.github.sarxos.webcam.ds.gstreamer.ScreenCaptureDriver;


public class WebcamPanelSubViewExample {

	static {
		Webcam.setDriver(new WebcamCompositeDriver(new WebcamDefaultDriver(), new ScreenCaptureDriver()));
	}

	private final Dimension size = WebcamResolution.QQVGA.getSize();
	private final Webcam screen = Webcam.getWebcamByName(":0.1");
	private final WebcamPanel panel = new WebcamPanel(screen);
	private final Painter dp = panel.getDefaultPainter();
	private final JFrame window = new JFrame("Test");

	private final class SubViewPainter implements Painter {

		private final Webcam webcam = Webcam.getDefault();
		private final int x = 619;
		private final int y = 437;
		private final int w = size.width;
		private final int h = size.height;

		public SubViewPainter() {
			webcam.setViewSize(size);
			webcam.open();
		}

		@Override
		public void paintImage(WebcamPanel owner, BufferedImage image, Graphics2D g2) {
			dp.paintImage(owner, image, g2);
			g2.setColor(Color.BLACK);
			g2.drawRect(x - 1, y - 1, w + 1, h + 1);
			g2.drawImage(webcam.getImage(), x, y, w, h, null);
		}

		@Override
		public void paintPanel(WebcamPanel panel, Graphics2D g2) {
			dp.paintPanel(panel, g2);
		};
	};

	public WebcamPanelSubViewExample() {

		screen.open(true);

		panel.setFPSDisplayed(true);
		panel.setDrawMode(DrawMode.FIT);
		panel.setImageSizeDisplayed(true);
		panel.setFPSDisplayed(true);
		panel.setPainter(new SubViewPainter());
		panel.setPreferredSize(new Dimension(800, 600));

		window.setResizable(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setContentPane(panel);
		window.pack();
		window.setVisible(true);

	}

	public static void main(String[] args) {
		new WebcamPanelSubViewExample();
	}
}
