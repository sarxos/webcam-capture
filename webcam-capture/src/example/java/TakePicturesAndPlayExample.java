import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.github.sarxos.webcam.Webcam;


public class TakePicturesAndPlayExample {

	private static final class PlayerPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		private final Vector<BufferedImage> images;
		private final Dimension size;
		private int offset = 0;

		public PlayerPanel(Vector<BufferedImage> images) {
			super();
			this.images = images;
			this.size = new Dimension(images.get(0).getWidth(), images.get(0).getHeight());
			setPreferredSize(size);
		}

		public void play() {
			Thread t = new Thread() {

				@Override
				public void run() {
					do {
						repaint();
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							return;
						}
					} while (++offset < images.size());
				}
			};
			t.setDaemon(true);
			t.start();
		}

		@Override
		protected void paintComponent(Graphics g) {
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.drawImage(images.get(offset), 0, 0, null);
		}
	}

	public static void main(String[] args) {

		Webcam w = Webcam.getDefault();
		w.open(true);

		Vector<BufferedImage> images = new Vector<BufferedImage>();

		System.out.println("recording, please wait");

		for (int i = 0; i < 100; i++) {
			images.add(w.getImage());
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				return;
			}
		}

		w.close();

		System.out.println("play");

		PlayerPanel panel = new PlayerPanel(images);

		JFrame f = new JFrame("Take pictures and play example");
		f.add(panel);
		f.pack();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);

		panel.play();

		try {
			Thread.sleep(100 * images.size());
		} catch (InterruptedException e) {
			return;
		}
	}
}
