import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamListener;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.util.AdaptiveSizeWriter;


/**
 * This class demonstrate how you can use {@link AdaptiveSizeWriter} to compress video frame to JPEG
 * with a given max number of bytes.
 */
public class AdaptiveSizeWriterExample extends JFrame implements ChangeListener, WebcamListener {

	/**
	 * Serial.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Lets assume we want to have our JPEG frames to have max size of 40 KiB.
	 */
	private static final int MAX_BYTES = 20 * 1024;

	/**
	 * Lets assume we want to have our JPEG frames to have min size of 5 KiB.
	 */
	private static final int MIN_BYTES = 6 * 1024;

	/**
	 * Webcam resolkution to use.
	 */
	private static final Dimension RESOLUTION = WebcamResolution.VGA.getSize();

	final JSlider slider = new JSlider(JSlider.VERTICAL, MIN_BYTES, MAX_BYTES, MIN_BYTES + (MAX_BYTES - MIN_BYTES) / 2);
	final Webcam webcam = Webcam.getDefault();
	final ImagePanel panel = new ImagePanel();
	final AdaptiveSizeWriter writer = new AdaptiveSizeWriter(slider.getValue());

	public AdaptiveSizeWriterExample() {

		slider.addChangeListener(this);
		slider.setMajorTickSpacing(2 * 1024);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

		panel.setPreferredSize(RESOLUTION);
		panel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLoweredBevelBorder(),
			BorderFactory.createEmptyBorder(10, 10, 10, 10)));

		webcam.setViewSize(RESOLUTION);
		webcam.addWebcamListener(this);
		webcam.open(true);

		final JPanel root = new JPanel();
		root.setLayout(new BorderLayout());
		root.add(slider, BorderLayout.WEST);
		root.add(panel, BorderLayout.CENTER);
		root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		setContentPane(root);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		writer.setSize(slider.getValue());
	}

	@Override
	public void webcamOpen(WebcamEvent we) {
	}

	@Override
	public void webcamClosed(WebcamEvent we) {
	}

	@Override
	public void webcamDisposed(WebcamEvent we) {
	}

	@Override
	public void webcamImageObtained(WebcamEvent we) {
		panel.setImage(writer.write(we.getImage()));
	}

	public static void main(String[] args) throws IOException {
		new AdaptiveSizeWriterExample();
	}

	private class ImagePanel extends JPanel {

		private static final long serialVersionUID = 1L;
		private BufferedImage image;

		@Override
		protected void paintComponent(Graphics g) {
			g.drawImage(image, 0, 0, this);
		}

		public void setImage(byte[] bytes) {

			try (InputStream is = new ByteArrayInputStream(bytes)) {
				this.image = ImageIO.read(is);
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					repaint();
				}
			});
		}
	}
}
