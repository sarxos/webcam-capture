package com.github.sarxos.webcam;

import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;


/**
 * Example demonstrating how to use webcam image transformer feature.
 *
 * @author Bartosz Firyn (SarXos)
 */
public class WebcamImageTransformerExample implements WebcamImageTransformer {

	private static final BufferedImage IMAGE_FRAME = getImage("frame.png");

	private Webcam webcam = Webcam.getDefault();

	public WebcamImageTransformerExample() {

		webcam.setViewSize(WebcamResolution.VGA.getSize());
		webcam.setImageTransformer(this);
		webcam.open();

		JFrame window = new JFrame("Test Transformer");

		WebcamPanel panel = new WebcamPanel(webcam);
		panel.setFPSDisplayed(true);
		panel.setFillArea(true);

		JButton button = new JButton(new AbstractAction("capture") {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					ImageIO.write(webcam.getImage(), "PNG", new File("capture.png"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		window.setLayout(new FlowLayout(FlowLayout.CENTER));
		window.add(panel);
		window.add(button);
		window.pack();
		window.setVisible(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	@Override
	public BufferedImage transform(BufferedImage image) {

		int w = image.getWidth();
		int h = image.getHeight();

		BufferedImage modified = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

		Graphics2D g2 = modified.createGraphics();
		g2.drawImage(image, null, 0, 0);
		g2.drawImage(IMAGE_FRAME, null, 0, 0);
		g2.dispose();

		modified.flush();

		return modified;
	}

	private static final BufferedImage getImage(String image) {
		try {
			return ImageIO.read(WebcamImageTransformerExample.class.getResourceAsStream("/" + image));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) throws IOException {

		JFrame.setDefaultLookAndFeelDecorated(true);

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}
				new WebcamImageTransformerExample();
			}
		});
	}
}
