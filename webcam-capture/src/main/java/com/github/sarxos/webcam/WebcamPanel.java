package com.github.sarxos.webcam;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_INTERPOLATION;
import static java.awt.RenderingHints.KEY_RENDERING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_OFF;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR;
import static java.awt.RenderingHints.VALUE_RENDER_SPEED;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Simply implementation of JPanel allowing users to render pictures taken with webcam.
 *
 * @author Bartosz Firyn (SarXos)
 */
public class WebcamPanel extends JPanel implements WebcamListener, PropertyChangeListener {

	/**
	 * This enum is to control of how image will be drawn in the panel bounds.
	 *
	 * @author Sylwia Kauczor
	 */
	public static enum DrawMode {

		/**
		 * Do not resize image - paint it as it is. This will make the image to go off out the
		 * bounds if panel is smaller than image size.
		 */
		NONE,

		/**
		 * Will resize image to the panel bounds. This mode does not care of the image scale, so the
		 * final image may be disrupted.
		 */
		FILL,

		/**
		 * Will fir image into the panel bounds. This will resize the image and keep both x and y
		 * scale factor.
		 */
		FIT,
	}

	/**
	 * Interface of the painter used to draw image in panel.
	 *
	 * @author Bartosz Firyn (SarXos)
	 */
	public static interface Painter {

		/**
		 * Paint panel without image.
		 *
		 * @param panel the webcam panel to paint on
		 * @param g2 the graphics 2D object used for drawing
		 */
		void paintPanel(WebcamPanel panel, Graphics2D g2);

		/**
		 * Paint webcam image in panel.
		 *
		 * @param panel the webcam panel to paint on
		 * @param image the image from webcam
		 * @param g2 the graphics 2D object used for drawing
		 */
		void paintImage(WebcamPanel panel, BufferedImage image, Graphics2D g2);
	}

	/**
	 * Default painter used to draw image in panel.
	 *
	 * @author Bartosz Firyn (SarXos)
	 * @author Sylwia Kauczor
	 */
	public class DefaultPainter implements Painter {

		/**
		 * Webcam device name.
		 */
		private String name = null;

		/**
		 * Lat repaint time, uset for debug purpose.
		 */
		private long lastRepaintTime = -1;

		/**
		 * Buffered image resized to fit into panel drawing area.
		 */
		private BufferedImage resizedImage = null;

		@Override
		public void paintPanel(WebcamPanel owner, Graphics2D g2) {

			assert owner != null;
			assert g2 != null;

			Object antialiasing = g2.getRenderingHint(KEY_ANTIALIASING);

			g2.setRenderingHint(KEY_ANTIALIASING, isAntialiasingEnabled() ? VALUE_ANTIALIAS_ON : VALUE_ANTIALIAS_OFF);
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
			g2.setRenderingHint(KEY_ANTIALIASING, antialiasing);
		}

		@Override
		public void paintImage(WebcamPanel owner, BufferedImage image, Graphics2D g2) {

			assert owner != null;
			assert image != null;
			assert g2 != null;

			int pw = getWidth();
			int ph = getHeight();
			int iw = image.getWidth();
			int ih = image.getHeight();

			Object antialiasing = g2.getRenderingHint(KEY_ANTIALIASING);
			Object rendering = g2.getRenderingHint(KEY_RENDERING);

			g2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_OFF);
			g2.setRenderingHint(KEY_RENDERING, VALUE_RENDER_SPEED);
			g2.setBackground(Color.BLACK);
			g2.setColor(Color.BLACK);
			g2.fillRect(0, 0, pw, ph);

			// resized image position and size
			int x = 0;
			int y = 0;
			int w = 0;
			int h = 0;

			switch (drawMode) {
				case NONE:
					w = image.getWidth();
					h = image.getHeight();
					break;
				case FILL:
					w = pw;
					h = ph;
					break;
				case FIT:
					double s = Math.max((double) iw / pw, (double) ih / ph);
					double niw = iw / s;
					double nih = ih / s;
					double dx = (pw - niw) / 2;
					double dy = (ph - nih) / 2;
					w = (int) niw;
					h = (int) nih;
					x = (int) dx;
					y = (int) dy;
					break;
			}

			if (resizedImage != null) {
				resizedImage.flush();
			}

			if (w == image.getWidth() && h == image.getHeight() && !mirrored) {
				resizedImage = image;
			} else {

				GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
				GraphicsConfiguration gc = genv.getDefaultScreenDevice().getDefaultConfiguration();

				Graphics2D gr = null;
				try {

					resizedImage = gc.createCompatibleImage(pw, ph);
					gr = resizedImage.createGraphics();
					gr.setComposite(AlphaComposite.Src);

					for (Map.Entry<RenderingHints.Key, Object> hint : imageRenderingHints.entrySet()) {
						gr.setRenderingHint(hint.getKey(), hint.getValue());
					}

					gr.setBackground(Color.BLACK);
					gr.setColor(Color.BLACK);
					gr.fillRect(0, 0, pw, ph);

					int sx1, sx2, sy1, sy2; // source rectangle coordinates
					int dx1, dx2, dy1, dy2; // destination rectangle coordinates

					dx1 = x;
					dy1 = y;
					dx2 = x + w;
					dy2 = y + h;

					if (mirrored) {
						sx1 = iw;
						sy1 = 0;
						sx2 = 0;
						sy2 = ih;
					} else {
						sx1 = 0;
						sy1 = 0;
						sx2 = iw;
						sy2 = ih;
					}

					gr.drawImage(image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);

				} finally {
					if (gr != null) {
						gr.dispose();
					}
				}
			}

			g2.drawImage(resizedImage, 0, 0, null);

			if (isFPSDisplayed()) {

				String str = String.format("FPS: %.1f", webcam.getFPS());

				int sx = 5;
				int sy = ph - 5;

				g2.setFont(getFont());
				g2.setColor(Color.BLACK);
				g2.drawString(str, sx + 1, sy + 1);
				g2.setColor(Color.WHITE);
				g2.drawString(str, sx, sy);
			}

			if (isImageSizeDisplayed()) {

				String res = String.format("%d\u2A2F%d px", iw, ih);

				FontMetrics metrics = g2.getFontMetrics(getFont());
				int sw = metrics.stringWidth(res);
				int sx = pw - sw - 5;
				int sy = ph - 5;

				g2.setFont(getFont());
				g2.setColor(Color.BLACK);
				g2.drawString(res, sx + 1, sy + 1);
				g2.setColor(Color.WHITE);
				g2.drawString(res, sx, sy);
			}

			if (isDisplayDebugInfo()) {

				if (lastRepaintTime < 0) {
					lastRepaintTime = System.currentTimeMillis();
				} else {

					long now = System.currentTimeMillis();
					String res = String.format("DEBUG: repaints per second: %.1f", (double) 1000 / (now - lastRepaintTime));
					lastRepaintTime = now;
					g2.setFont(getFont());
					g2.setColor(Color.BLACK);
					g2.drawString(res, 6, 16);
					g2.setColor(Color.WHITE);
					g2.drawString(res, 5, 15);
				}
			}

			g2.setRenderingHint(KEY_ANTIALIASING, antialiasing);
			g2.setRenderingHint(KEY_RENDERING, rendering);
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
	 * This runnable will do nothing more than repaint panel.
	 */
	private static final class SwingRepainter implements Runnable {

		private WebcamPanel panel = null;

		public SwingRepainter(WebcamPanel panel) {
			this.panel = panel;
		}

		@Override
		public void run() {
			panel.repaint();
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

	public static final Map<RenderingHints.Key, Object> DEFAULT_IMAGE_RENDERING_HINTS = new HashMap<RenderingHints.Key, Object>();
	static {
		DEFAULT_IMAGE_RENDERING_HINTS.put(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR);
		DEFAULT_IMAGE_RENDERING_HINTS.put(KEY_RENDERING, VALUE_RENDER_SPEED);
		DEFAULT_IMAGE_RENDERING_HINTS.put(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
	}

	/**
	 * This runnable will do nothing more than repaint panel.
	 */
	private final Runnable repaint = new SwingRepainter(this);

	/**
	 * Rendering hints to be used when painting image to be displayed.
	 */
	private Map<RenderingHints.Key, Object> imageRenderingHints = new HashMap<RenderingHints.Key, Object>(DEFAULT_IMAGE_RENDERING_HINTS);

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
		 * Repaint scheduler schedule panel updates.
		 *
		 * @author Bartosz Firyn (sarxos)
		 */
		private class RepaintScheduler extends Thread {

			/**
			 * Repaint scheduler schedule panel updates.
			 */
			public RepaintScheduler() {
				setUncaughtExceptionHandler(WebcamExceptionHandler.getInstance());
				setName(String.format("repaint-scheduler-%s", webcam.getName()));
				setDaemon(true);
			}

			@Override
			public void run() {

				// do nothing when not running
				if (!running.get()) {
					return;
				}

				repaintPanel();

				// loop when starting, to wait for images
				while (starting) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}

				// schedule update when webcam is open, otherwise schedule
				// second scheduler execution

				try {

					// FPS limit means that panel rendering frequency is
					// limited to the specific value and panel will not be
					// rendered more often then specific value

					if (webcam.isOpen()) {

						// TODO: rename FPS value in panel to rendering
						// frequency

						if (isFPSLimited()) {
							executor.scheduleAtFixedRate(updater, 0, (long) (1000 / frequency), TimeUnit.MILLISECONDS);
						} else {
							executor.scheduleWithFixedDelay(updater, 100, 1, TimeUnit.MILLISECONDS);
						}
					} else {
						executor.schedule(this, 500, TimeUnit.MILLISECONDS);
					}
				} catch (RejectedExecutionException e) {

					// executor has been shut down, which means that someone
					// stopped panel / webcam device before it was actually
					// completely started (it was in "starting" timeframe)

					LOG.warn("Executor rejected paint update");
					LOG.trace("Executor rejected paint update because of", e);

					return;
				}
			}
		}

		/**
		 * Update scheduler thread.
		 */
		private Thread scheduler = null;

		/**
		 * Is repainter running?
		 */
		private AtomicBoolean running = new AtomicBoolean(false);

		/**
		 * Start repainter. Can be invoked many times, but only first call will take effect.
		 */
		public void start() {
			if (running.compareAndSet(false, true)) {
				executor = Executors.newScheduledThreadPool(1, THREAD_FACTORY);
				scheduler = new RepaintScheduler();
				scheduler.start();
			}
		}

		/**
		 * Stop repainter. Can be invoked many times, but only first call will take effect.
		 *
		 * @throws InterruptedException
		 */
		public void stop() throws InterruptedException {
			if (running.compareAndSet(true, false)) {
				executor.shutdown();
				executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
				scheduler.join();
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

		/**
		 * Perform single panel area update (repaint newly obtained image).
		 */
		private void update() {

			// do nothing when updater not running, when webcam is closed, or
			// panel repainting is paused

			if (!running.get() || !webcam.isOpen() || paused) {
				return;
			}

			// get new image from webcam

			BufferedImage tmp = webcam.getImage();
			boolean repaint = true;

			if (tmp != null) {

				// ignore repaint if image is the same as before
				if (image == tmp) {
					repaint = false;
				}

				errored = false;
				image = tmp;
			}

			if (repaint) {
				repaintPanel();
			}
		}
	}

	/**
	 * Resource bundle.
	 */
	private ResourceBundle rb = null;

	/**
	 * The mode of how the image will be resized to fit into panel bounds. Default is
	 * {@link DrawMode#FIT}
	 *
	 * @see DrawMode
	 */
	private DrawMode drawMode = DrawMode.FIT;

	/**
	 * Frames requesting frequency.
	 */
	private double frequency = 5; // FPS

	/**
	 * Is frames requesting frequency limited? If true, images will be fetched in configured time
	 * intervals. If false, images will be fetched as fast as camera can serve them.
	 */
	private boolean frequencyLimit = false;

	/**
	 * Display FPS.
	 */
	private boolean frequencyDisplayed = false;

	/**
	 * Display image size.
	 */
	private boolean imageSizeDisplayed = false;

	/**
	 * Is antialiasing enabled (true by default).
	 */
	private boolean antialiasingEnabled = true;

	/**
	 * Webcam object used to fetch images.
	 */
	private final Webcam webcam;

	/**
	 * Repainter is used to fetch images from camera and force panel repaint when image is ready.
	 */
	private final ImageUpdater updater;

	/**
	 * Image currently being displayed.
	 */
	private BufferedImage image = null;

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
	private final AtomicBoolean started = new AtomicBoolean(false);

	/**
	 * Default painter.
	 */
	private final Painter defaultPainter = new DefaultPainter();

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
	private Dimension defaultSize = null;

	/**
	 * If debug info should be displayed.
	 */
	private boolean displayDebugInfo = false;

	/**
	 * Is image mirrored.
	 */
	private boolean mirrored = false;

	/**
	 * Creates webcam panel and automatically start webcam.
	 *
	 * @param webcam the webcam to be used to fetch images
	 */
	public WebcamPanel(Webcam webcam) {
		this(webcam, true);
	}

	/**
	 * Creates new webcam panel which display image from camera in you your Swing application.
	 *
	 * @param webcam the webcam to be used to fetch images
	 * @param start true if webcam shall be automatically started
	 */
	public WebcamPanel(Webcam webcam, boolean start) {
		this(webcam, null, start);
	}

	/**
	 * Creates new webcam panel which display image from camera in you your Swing application. If
	 * panel size argument is null, then image size will be used. If you would like to fill panel
	 * area with image even if its size is different, then you can use
	 * {@link WebcamPanel#setFillArea(boolean)} method to configure this.
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

		this.defaultSize = size;
		this.webcam = webcam;
		this.updater = new ImageUpdater();
		this.rb = WebcamUtils.loadRB(WebcamPanel.class, getLocale());

		setDoubleBuffered(true);

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

		super.paintComponent(g);

		if (image == null) {
			painter.paintPanel(this, (Graphics2D) g);
		} else {
			painter.paintImage(this, image, (Graphics2D) g);
		}
	}

	/**
	 * Open webcam and start rendering.
	 */
	public void start() {

		if (!started.compareAndSet(false, true)) {
			return;
		}

		webcam.addWebcamListener(this);

		LOG.debug("Starting panel rendering and trying to open attached webcam");

		updater.start();

		starting = true;

		try {
			if (!webcam.isOpen()) {
				errored = !webcam.open();
			}
		} catch (WebcamException e) {
			errored = true;
			throw e;
		} finally {
			starting = false;
			repaintPanel();
		}
	}

	/**
	 * Stop rendering and close webcam.
	 */
	public void stop() {

		if (!started.compareAndSet(true, false)) {
			return;
		}

		webcam.removeWebcamListener(this);

		LOG.debug("Stopping panel rendering and closing attached webcam");

		try {
			updater.stop();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		image = null;

		try {
			if (webcam.isOpen()) {
				errored = !webcam.close();
			}
		} catch (WebcamException e) {
			errored = true;
			throw e;
		} finally {
			repaintPanel();
		}
	}

	/**
	 * Repaint panel in Swing asynchronous manner.
	 */
	private void repaintPanel() {
		SwingUtilities.invokeLater(repaint);
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
	 * Enable or disable frequency limit. Frequency limit should be used for <b>all IP cameras
	 * working in pull mode</b> (to save number of HTTP requests). If true, images will be fetched
	 * in configured time intervals. If false, images will be fetched as fast as camera can serve
	 * them.
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
	 * Set rendering frequency (in Hz or FPS). Minimum frequency is 0.016 (1 frame per minute) and
	 * maximum is 25 (25 frames per second).
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

	/**
	 * Is displaying of some debug information enabled.
	 *
	 * @return True if debug information are enabled, false otherwise
	 */
	public boolean isDisplayDebugInfo() {
		return displayDebugInfo;
	}

	/**
	 * Display some debug information on image surface.
	 *
	 * @param displayDebugInfo the value to control debug information
	 */
	public void setDisplayDebugInfo(boolean displayDebugInfo) {
		this.displayDebugInfo = displayDebugInfo;
	}

	/**
	 * This method return true in case if camera FPS is set to be displayed on panel surface.
	 * Default value returned is false.
	 *
	 * @return True if camera FPS is set to be displayed on panel surface
	 * @see #setFPSDisplayed(boolean)
	 */
	public boolean isFPSDisplayed() {
		return frequencyDisplayed;
	}

	/**
	 * This method is to control if camera FPS should be displayed on the webcam panel surface.
	 *
	 * @param displayed the value to control if camera FPS should be displayed
	 */
	public void setFPSDisplayed(boolean displayed) {
		this.frequencyDisplayed = displayed;
	}

	/**
	 * This method will return true in case when panel is configured to display image size. The
	 * string will be printed in the right bottom corner of the panel surface.
	 *
	 * @return True in case if panel is configured to display image size
	 */
	public boolean isImageSizeDisplayed() {
		return imageSizeDisplayed;
	}

	/**
	 * Configure panel to display camera image size to be displayed.
	 *
	 * @param imageSizeDisplayed
	 */
	public void setImageSizeDisplayed(boolean imageSizeDisplayed) {
		this.imageSizeDisplayed = imageSizeDisplayed;
	}

	/**
	 * Turn on/off antialiasing.
	 *
	 * @param antialiasing the true to enable, false to disable antialiasing
	 */
	public void setAntialiasingEnabled(boolean antialiasing) {
		this.antialiasingEnabled = antialiasing;
	}

	/**
	 * @return True is antialiasing is enabled, false otherwise
	 */
	public boolean isAntialiasingEnabled() {
		return antialiasingEnabled;
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

	public boolean isFitArea() {
		return drawMode == DrawMode.FIT;
	}

	/**
	 * This method will change the mode of panel area painting so the image will be resized and will
	 * keep scale factor to fit into drawable panel bounds. When set to false, the mode will be
	 * reset to {@link DrawMode#NONE} so image will be drawn as it is.
	 *
	 * @param fitArea the fit area mode enabled or disabled
	 */
	public void setFitArea(boolean fitArea) {
		this.drawMode = fitArea ? DrawMode.FIT : DrawMode.NONE;
	}

	/**
	 * Image will be resized to fill panel area if true. If false then image will be rendered as it
	 * was obtained from webcam instance.
	 *
	 * @param fillArea shall image be resided to fill panel area
	 */
	public void setFillArea(boolean fillArea) {
		this.drawMode = fillArea ? DrawMode.FILL : DrawMode.NONE;
	}

	/**
	 * Get value of fill area setting. Image will be resized to fill panel area if true. If false
	 * then image will be rendered as it was obtained from webcam instance.
	 *
	 * @return True if image is being resized, false otherwise
	 */
	public boolean isFillArea() {
		return drawMode == DrawMode.FILL;
	}

	/**
	 * Get default painter used to draw panel.
	 *
	 * @return Default painter
	 */
	public Painter getDefaultPainter() {
		return defaultPainter;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Locale lc = (Locale) evt.getNewValue();
		if (lc != null) {
			rb = WebcamUtils.loadRB(WebcamPanel.class, lc);
		}
	}

	@Override
	public void webcamOpen(WebcamEvent we) {

		// if default size has not been provided, then use the one from webcam
		// device (this will be current webcam resolution)

		if (defaultSize == null) {
			setPreferredSize(webcam.getViewSize());
		}
	}

	@Override
	public void webcamClosed(WebcamEvent we) {
		stop();
	}

	@Override
	public void webcamDisposed(WebcamEvent we) {
		stop();
	}

	@Override
	public void webcamImageObtained(WebcamEvent we) {
		// do nothing
	}

	/**
	 * This method returns true if image mirroring is enabled. The default value is false.
	 *
	 * @return True if image is mirrored, false otherwise
	 */
	public boolean isMirrored() {
		return mirrored;
	}

	/**
	 * Decide whether or not the image from webcam painted on panel surface will be mirrored. The
	 * image from camera itself is not modified.
	 *
	 * @param mirrored the parameter to control if image should be mirrored
	 */
	public void setMirrored(boolean mirrored) {
		this.mirrored = mirrored;
	}
}
