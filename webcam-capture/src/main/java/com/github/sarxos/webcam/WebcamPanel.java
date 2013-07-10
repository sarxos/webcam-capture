package com.github.sarxos.webcam;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Simply implementation of JPanel allowing users to render pictures taken with
 * webcam.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class WebcamPanel extends JPanel implements WebcamListener, PropertyChangeListener {

	/**
	 * Interface of the painter used to draw image in panel.
	 * 
	 * @author Bartosz Firyn (SarXos)
	 */
	public static interface Painter {

		/**
		 * Paints panel without image.
		 * 
		 * @param g2 the graphics 2D object used for drawing
		 */
		void paintPanel(WebcamPanel panel, Graphics2D g2);

		/**
		 * Paints webcam image in panel.
		 * 
		 * @param g2 the graphics 2D object used for drawing
		 */
		void paintImage(WebcamPanel panel, BufferedImage image, Graphics2D g2);
	}

	/**
	 * Default painter used to draw image in panel.
	 * 
	 * @author Bartosz Firyn (SarXos)
	 */
	public class DefaultPainter implements Painter {

		private String name = null;

		@Override
		public void paintPanel(WebcamPanel owner, Graphics2D g2) {

			assert owner != null;
			assert g2 != null;

			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setBackground(Color.BLACK);
			g2.fillRect(0, 0, getWidth(), getHeight());

			int cx = (getWidth() - 70) / 2;
			int cy = (getHeight() - 40) / 2;

			g2.setStroke(new BasicStroke(2));
			g2.setColor(Color.LIGHT_GRAY);
			g2.fillRoundRect(cx, cy, 70, 40, 10, 10);
			g2.setColor(Color.WHITE);
			g2.fillOval(cx + 5, cy + 5, 30, 30);
			g2.setColor(Color.LIGHT_GRAY);
			g2.fillOval(cx + 10, cy + 10, 20, 20);
			g2.setColor(Color.WHITE);
			g2.fillOval(cx + 12, cy + 12, 16, 16);
			g2.fillRoundRect(cx + 50, cy + 5, 15, 10, 5, 5);
			g2.fillRect(cx + 63, cy + 25, 7, 2);
			g2.fillRect(cx + 63, cy + 28, 7, 2);
			g2.fillRect(cx + 63, cy + 31, 7, 2);

			g2.setColor(Color.DARK_GRAY);
			g2.setStroke(new BasicStroke(3));
			g2.drawLine(0, 0, getWidth(), getHeight());
			g2.drawLine(0, getHeight(), getWidth(), 0);

			String str = null;

			final String strInitDevice = rb.getString("INITIALIZING_DEVICE");
			final String strNoImage = rb.getString("NO_IMAGE");
			final String strDeviceError = rb.getString("DEVICE_ERROR");

			if (!errored) {
				str = starting ? strInitDevice : strNoImage;
			} else {
				str = strDeviceError;
			}

			FontMetrics metrics = g2.getFontMetrics(getFont());
			int w = metrics.stringWidth(str);
			int h = metrics.getHeight();

			int x = (getWidth() - w) / 2;
			int y = cy - h;

			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			g2.setFont(getFont());
			g2.setColor(Color.WHITE);
			g2.drawString(str, x, y);

			if (name == null) {
				name = webcam.getName();
			}

			str = name;

			w = metrics.stringWidth(str);
			h = metrics.getHeight();

			g2.drawString(str, (getWidth() - w) / 2, cy - 2 * h);
		}

		@Override
		public void paintImage(WebcamPanel owner, BufferedImage image, Graphics2D g2) {

			int w = getWidth();
			int h = getHeight();

			if (fillArea && image.getWidth() != w && image.getHeight() != h) {

				BufferedImage resized = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
				Graphics2D gr = resized.createGraphics();
				gr.setComposite(AlphaComposite.Src);
				gr.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				gr.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				gr.drawImage(image, 0, 0, w, h, null);
				gr.dispose();
				resized.flush();

				image = resized;
			}

			g2.drawImage(image, 0, 0, null);

			if (isFPSDisplayed()) {

				String str = String.format("FPS: %.1f", webcam.getFPS());

				int x = 5;
				int y = getHeight() - 5;

				g2.setFont(getFont());
				g2.setColor(Color.BLACK);
				g2.drawString(str, x + 1, y + 1);
				g2.setColor(Color.WHITE);
				g2.drawString(str, x, y);
			}
		}
	}

	private static final class PanelThreadFactory implements ThreadFactory {

		private static final AtomicInteger number = new AtomicInteger(0);

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, String.format("webcam-panel-scheduled-executor-%d", number.incrementAndGet()));
			t.setUncaughtExceptionHandler(WebcamExceptionHandler.getInstance());
			t.setDaemon(true);
			return t;
		}

	}

	/**
	 * S/N used by Java to serialize beans.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(WebcamPanel.class);

	/**
	 * Minimum FPS frequency.
	 */
	public static final double MIN_FREQUENCY = 0.016; // 1 frame per minute

	/**
	 * Maximum FPS frequency.
	 */
	private static final double MAX_FREQUENCY = 50; // 50 frames per second

	/**
	 * Thread factory used by execution service.
	 */
	private static final ThreadFactory THREAD_FACTORY = new PanelThreadFactory();

	/**
	 * Scheduled executor acting as timer.
	 */
	private ScheduledExecutorService executor = null;

	/**
	 * Image updater reads images from camera and force panel to be repainted.
	 * 
	 * @author Bartosz Firyn (SarXos)
	 */
	private class ImageUpdater implements Runnable {

		/**
		 * Repainter updates panel when it is being started.
		 * 
		 * @author Bartosz Firyn (sarxos)
		 */
		private class RepaintScheduler extends Thread {

			public RepaintScheduler() {
				setUncaughtExceptionHandler(WebcamExceptionHandler.getInstance());
				setName(String.format("repaint-scheduler-%s", webcam.getName()));
				setDaemon(true);
			}

			@Override
			public void run() {

				if (!running.get()) {
					return;
				}

				repaint();

				while (starting) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}

				if (webcam.isOpen()) {
					if (isFPSLimited()) {
						executor.scheduleAtFixedRate(updater, 0, (long) (1000 / frequency), TimeUnit.MILLISECONDS);
					} else {
						executor.scheduleWithFixedDelay(updater, 100, 1, TimeUnit.MILLISECONDS);
					}
				} else {
					executor.schedule(this, 500, TimeUnit.MILLISECONDS);
				}
			}

		}

		private Thread scheduler = new RepaintScheduler();

		private AtomicBoolean running = new AtomicBoolean(false);

		public void start() {
			if (running.compareAndSet(false, true)) {
				executor = Executors.newScheduledThreadPool(1, THREAD_FACTORY);
				scheduler.start();
			}
		}

		public void stop() {
			if (running.compareAndSet(true, false)) {
				executor.shutdown();
			}
		}

		@Override
		public void run() {
			try {
				update();
			} catch (Throwable t) {
				errored = true;
				WebcamExceptionHandler.handle(t);
			}
		}

		private void update() {

			if (!running.get() || !webcam.isOpen() || paused) {
				return;
			}

			BufferedImage tmp = webcam.getImage();
			if (tmp != null) {
				errored = false;
				image = tmp;
			}

			repaint();
		}
	}

	/**
	 * Resource bundle.
	 */
	private ResourceBundle rb = null;

	/**
	 * Fit image into panel area.
	 */
	private boolean fillArea = false;

	/**
	 * Frames requesting frequency.
	 */
	private double frequency = 5; // FPS

	/**
	 * Is frames requesting frequency limited? If true, images will be fetched
	 * in configured time intervals. If false, images will be fetched as fast as
	 * camera can serve them.
	 */
	private boolean frequencyLimit = false;

	/**
	 * Display FPS.
	 */
	private boolean frequencyDisplayed = false;

	/**
	 * Webcam object used to fetch images.
	 */
	private Webcam webcam = null;

	/**
	 * Image currently being displayed.
	 */
	private BufferedImage image = null;

	/**
	 * Repainter is used to fetch images from camera and force panel repaint
	 * when image is ready.
	 */
	private volatile ImageUpdater updater = null;

	/**
	 * Webcam is currently starting.
	 */
	private volatile boolean starting = false;

	/**
	 * Painting is paused.
	 */
	private volatile boolean paused = false;

	/**
	 * Is there any problem with webcam?
	 */
	private volatile boolean errored = false;

	/**
	 * Webcam has been started.
	 */
	private AtomicBoolean started = new AtomicBoolean(false);

	private Painter defaultPainter = new DefaultPainter();

	/**
	 * Painter used to draw image in panel.
	 * 
	 * @see #setPainter(Painter)
	 * @see #getPainter()
	 */
	private Painter painter = defaultPainter;

	/**
	 * Preferred panel size.
	 */
	private Dimension size = null;

	/**
	 * Creates webcam panel and automatically start webcam.
	 * 
	 * @param webcam the webcam to be used to fetch images
	 */
	public WebcamPanel(Webcam webcam) {
		this(webcam, true);
	}

	/**
	 * Creates new webcam panel which display image from camera in you your
	 * Swing application.
	 * 
	 * @param webcam the webcam to be used to fetch images
	 * @param start true if webcam shall be automatically started
	 */
	public WebcamPanel(Webcam webcam, boolean start) {
		this(webcam, null, start);
	}

	/**
	 * Creates new webcam panel which display image from camera in you your
	 * Swing application. If panel size argument is null, then image size will
	 * be used. If you would like to fill panel area with image even if its size
	 * is different, then you can use {@link WebcamPanel#setFillArea(boolean)}
	 * method to configure this.
	 * 
	 * @param webcam the webcam to be used to fetch images
	 * @param size the size of panel
	 * @param start true if webcam shall be automatically started
	 * @see WebcamPanel#setFillArea(boolean)
	 */
	public WebcamPanel(Webcam webcam, Dimension size, boolean start) {

		if (webcam == null) {
			throw new IllegalArgumentException(String.format("Webcam argument in %s constructor cannot be null!", getClass().getSimpleName()));
		}

		this.size = size;
		this.webcam = webcam;
		this.webcam.addWebcamListener(this);

		rb = WebcamUtils.loadRB(WebcamPanel.class, getLocale());

		addPropertyChangeListener("locale", this);

		if (size == null) {
			Dimension r = webcam.getViewSize();
			if (r == null) {
				r = webcam.getViewSizes()[0];
			}
			setPreferredSize(r);
		} else {
			setPreferredSize(size);
		}

		if (start) {
			start();
		}
	}

	/**
	 * Set new painter. Painter is a class which pains image visible when
	 * 
	 * @param painter the painter object to be set
	 */
	public void setPainter(Painter painter) {
		this.painter = painter;
	}

	/**
	 * Get painter used to draw image in webcam panel.
	 * 
	 * @return Painter object
	 */
	public Painter getPainter() {
		return painter;
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		if (image == null) {
			painter.paintPanel(this, g2);
		} else {
			painter.paintImage(this, image, g2);
		}
	}

	@Override
	public void webcamOpen(WebcamEvent we) {

		// start image updater (i.e. start panel repainting)
		if (updater == null) {
			updater = new ImageUpdater();
			updater.start();
		}

		// copy size from webcam only if default size has not been provided
		if (size == null) {
			setPreferredSize(webcam.getViewSize());
		}
	}

	@Override
	public void webcamClosed(WebcamEvent we) {
		stop();
	}

	@Override
	public void webcamDisposed(WebcamEvent we) {
		webcamClosed(we);
	}

	@Override
	public void webcamImageObtained(WebcamEvent we) {
		// do nothing
	}

	/**
	 * Open webcam and start rendering.
	 */
	public void start() {

		if (!started.compareAndSet(false, true)) {
			return;
		}

		LOG.debug("Starting panel rendering and trying to open attached webcam");

		starting = true;

		if (updater == null) {
			updater = new ImageUpdater();
		}

		updater.start();

		try {
			errored = !webcam.open();
		} catch (WebcamException e) {
			errored = true;
			repaint();
			throw e;
		} finally {
			starting = false;
		}
	}

	/**
	 * Stop rendering and close webcam.
	 */
	public void stop() {

		if (!started.compareAndSet(true, false)) {
			return;
		}

		LOG.debug("Stopping panel rendering and closing attached webcam");

		updater.stop();
		updater = null;

		image = null;

		try {
			errored = !webcam.close();
		} catch (WebcamException e) {
			errored = true;
			repaint();
			throw e;
		}
	}

	/**
	 * Pause rendering.
	 */
	public void pause() {
		if (paused) {
			return;
		}

		LOG.debug("Pausing panel rendering");

		paused = true;
	}

	/**
	 * Resume rendering.
	 */
	public void resume() {

		if (!paused) {
			return;
		}

		LOG.debug("Resuming panel rendering");

		paused = false;
	}

	/**
	 * Is frequency limit enabled?
	 * 
	 * @return True or false
	 */
	public boolean isFPSLimited() {
		return frequencyLimit;
	}

	/**
	 * Enable or disable frequency limit. Frequency limit should be used for
	 * <b>all IP cameras working in pull mode</b> (to save number of HTTP
	 * requests). If true, images will be fetched in configured time intervals.
	 * If false, images will be fetched as fast as camera can serve them.
	 * 
	 * @param frequencyLimit
	 */
	public void setFPSLimited(boolean frequencyLimit) {
		this.frequencyLimit = frequencyLimit;
	}

	/**
	 * Get rendering frequency in FPS (equivalent to Hz).
	 * 
	 * @return Rendering frequency
	 */
	public double getFPSLimit() {
		return frequency;
	}

	/**
	 * Set rendering frequency (in Hz or FPS). Minimum frequency is 0.016 (1
	 * frame per minute) and maximum is 25 (25 frames per second).
	 * 
	 * @param fps the frequency
	 */
	public void setFPSLimit(double fps) {
		if (fps > MAX_FREQUENCY) {
			fps = MAX_FREQUENCY;
		}
		if (fps < MIN_FREQUENCY) {
			fps = MIN_FREQUENCY;
		}
		this.frequency = fps;
	}

	public boolean isFPSDisplayed() {
		return frequencyDisplayed;
	}

	public void setFPSDisplayed(boolean displayed) {
		this.frequencyDisplayed = displayed;
	}

	/**
	 * Is webcam panel repainting starting.
	 * 
	 * @return True if panel is starting
	 */
	public boolean isStarting() {
		return starting;
	}

	/**
	 * Is webcam panel repainting started.
	 * 
	 * @return True if panel repainting has been started
	 */
	public boolean isStarted() {
		return started.get();
	}

	/**
	 * Image will be resized to fill panel area if true. If false then image
	 * will be rendered as it was obtained from webcam instance.
	 * 
	 * @param fillArea shall image be resided to fill panel area
	 */
	public void setFillArea(boolean fillArea) {
		this.fillArea = fillArea;
	}

	/**
	 * Get value of fill area setting. Image will be resized to fill panel area
	 * if true. If false then image will be rendered as it was obtained from
	 * webcam instance.
	 * 
	 * @return True if image is being resized, false otherwise
	 */
	public boolean isFillArea() {
		return fillArea;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Locale lc = (Locale) evt.getNewValue();
		if (lc != null) {
			rb = WebcamUtils.loadRB(WebcamPanel.class, lc);
		}
	}

	public Painter getDefaultPainter() {
		return defaultPainter;
	}
}
