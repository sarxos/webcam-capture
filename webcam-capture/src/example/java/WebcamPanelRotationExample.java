import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.util.jh.JHFlipFilter;


/**
 * This example demonstrates how to rotate image in {@link WebcamPanel} without rotating original
 * image which can be obtained with {@link Webcam#getImage()}.
 * 
 * @author Bartosz Firyn (sarxos)
 *
 */
public class WebcamPanelRotationExample {

	public static void main(String[] args) throws InterruptedException {

		final Webcam webcam = Webcam.getDefault();
		webcam.setViewSize(WebcamResolution.QVGA.getSize());

		final WebcamPanel panel = new WebcamPanel(webcam);
		panel.setFPSDisplayed(true);
		panel.setImageSizeDisplayed(true);

		// you may wonder what is this below - why new operator is invoked on panel instance, so let
		// me clarify this - since DefaultPainter class is an inner non-static class it has to be
		// invoked from the panel context - this is because an instance of a non-static inner class
		// holds a reference to it's owner object (the instance of the outer class that created it)

		final WebcamPanel.Painter painter = panel.new DefaultPainter() {

			final JHFlipFilter rotate = new JHFlipFilter(JHFlipFilter.FLIP_90CW);

			@Override
			public void paintImage(WebcamPanel owner, BufferedImage image, Graphics2D g2) {
				super.paintImage(owner, rotate.filter(image, null), g2);
			}
		};

		panel.setPainter(painter);

		final JFrame window = new JFrame("Test Rotation");
		window.add(panel);
		window.setResizable(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.pack();
		window.setVisible(true);
	}
}
