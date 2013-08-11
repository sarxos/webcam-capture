package com.github.sarxos.webcam;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.pushingpixels.substance.api.skin.SubstanceTwilightLookAndFeel;

import com.jhlabs.image.CrystallizeFilter;
import com.jhlabs.image.DitherFilter;
import com.jhlabs.image.ExposureFilter;
import com.jhlabs.image.FBMFilter;
import com.jhlabs.image.GammaFilter;
import com.jhlabs.image.GaussianFilter;
import com.jhlabs.image.GlowFilter;
import com.jhlabs.image.GrayscaleFilter;
import com.jhlabs.image.InvertFilter;
import com.jhlabs.image.KaleidoscopeFilter;
import com.jhlabs.image.LightFilter;
import com.jhlabs.image.NoiseFilter;
import com.jhlabs.image.SharpenFilter;
import com.jhlabs.image.SolarizeFilter;
import com.jhlabs.image.SphereFilter;
import com.jhlabs.image.ThresholdFilter;
import com.jhlabs.image.WaterFilter;


/**
 * Detect motion.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class CustomPainterExample extends JFrame {

	private static final long serialVersionUID = 1L;

	//@formatter:off
	private static final BufferedImageOp[] filters = new BufferedImageOp[] {
		new GrayscaleFilter(),
		new GammaFilter(),
		new NoiseFilter(),
		new LightFilter(),
		new KaleidoscopeFilter(),
		new GaussianFilter(10),
		new SharpenFilter(),
		new SolarizeFilter(),
		new ThresholdFilter(), 
		new WaterFilter(),
		new SphereFilter(),
		new InvertFilter(),
		new GlowFilter(),
		new ExposureFilter(),
		new FBMFilter(),
		new DitherFilter(),
		new CrystallizeFilter(),
	}; 
	//@formatter:on

	private static volatile BufferedImageOp currentFilter = filters[0];

	private static class MyPainter implements WebcamPanel.Painter {

		private static final Image noImage = getImage("image-missing-icon.png");

		@Override
		public void paintPanel(WebcamPanel wp, Graphics2D g2) {

			int w1 = wp.getSize().width;
			int h1 = wp.getSize().height;
			int w2 = noImage.getWidth(null);
			int h2 = noImage.getHeight(null);

			g2.setColor(wp.getBackground());
			g2.fillRect(0, 0, w1, h1);
			g2.drawImage(noImage, (w1 - w2) / 2, (h1 - h2) / 2, null);
		}

		@Override
		public void paintImage(WebcamPanel wp, BufferedImage image, Graphics2D g2) {

			BufferedImage image2 = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
			currentFilter.filter(image, image2);

			int w1 = wp.getSize().width;
			int h1 = wp.getSize().height;
			int w2 = image2.getWidth(null);
			int h2 = image2.getHeight(null);

			g2.drawImage(image2, (w1 - w2) / 2, (h1 - h2) / 2, null);

			image2.flush();

			Font font = wp.getFont();
			FontMetrics metrics = g2.getFontMetrics(font);
			String str = currentFilter.getClass().getSimpleName();

			int sw = metrics.stringWidth(str);

			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setFont(font);
			g2.setColor(wp.getForeground());
			g2.drawString(str, (w1 - sw) / 2, 40);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		}
	}

	private static class FilterMenuItem extends JMenuItem implements Runnable, ActionListener {

		private static final long serialVersionUID = 2332976764855349975L;

		private BufferedImageOp filter = null;

		public FilterMenuItem(BufferedImageOp filter) {
			super("Use " + filter.getClass().getSimpleName().replaceAll("Filter", " Filter"));
			addActionListener(this);
			this.filter = filter;
		}

		@Override
		public void run() {
			currentFilter = filter;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			EXECUTOR.execute(this);
		}
	}

	private class StartAction extends AbstractAction implements Runnable {

		private static final long serialVersionUID = -8121027263523549087L;

		public StartAction() {
			super("", new ImageIcon(getImage("play-icon.png")));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			start.setEnabled(false);
			stop.setEnabled(true);
			EXECUTOR.execute(this);
		}

		@Override
		public void run() {
			panel.start();
		}

	}

	private class StopAction extends AbstractAction implements Runnable {

		private static final long serialVersionUID = 8481593480989238171L;

		public StopAction() {
			super("", new ImageIcon(getImage("stop-icon.png")));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			start.setEnabled(true);
			stop.setEnabled(false);
			EXECUTOR.execute(this);
		}

		@Override
		public void run() {
			panel.stop();
		}
	}

	private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();

	private Webcam webcam = null;
	private WebcamPanel panel = null;
	private JButton start = null;
	private JButton stop = null;
	private JMenuBar menu = null;
	private JMenu filterMenu = new JMenu("Filter");

	public CustomPainterExample() {
		super();

		setTitle("Custom Painter Example");
		setLayout(new FlowLayout(FlowLayout.CENTER, 0, 5));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBackground(new Color(0, 0, 0, 0));
		// setPreferredSize(new Dimension(370, 497));

		menu = new JMenuBar();
		menu.add(filterMenu);

		for (BufferedImageOp filter : filters) {
			filterMenu.add(new FilterMenuItem(filter));
		}

		try {
			setIconImage(ImageIO.read(getClass().getResourceAsStream("/yellow-camera-icon.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}

		webcam = Webcam.getDefault();
		webcam.setViewSize(webcam.getViewSizes()[2]);

		Dimension size = webcam.getViewSizes()[0];

		panel = new WebcamPanel(webcam, false);
		panel.setPreferredSize(webcam.getViewSize());
		panel.setFont(new Font("Sans", Font.BOLD, 24));
		panel.setForeground(Color.WHITE);
		panel.setPainter(new MyPainter());

		start = new JButton(new StartAction());
		start.setFocusable(false);
		start.setPreferredSize(size);

		stop = new JButton(new StopAction());
		stop.setFocusable(false);
		stop.setPreferredSize(size);
		stop.setEnabled(false);

		setJMenuBar(menu);
		add(panel);
		add(start);
		add(stop);

		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private static final Image getImage(String image) {
		try {
			return ImageIO.read(StartAction.class.getResourceAsStream("/" + image));
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
					UIManager.setLookAndFeel(new SubstanceTwilightLookAndFeel());
				} catch (Exception e) {
					e.printStackTrace();
				}

				new CustomPainterExample();
			}
		});
	}
}
