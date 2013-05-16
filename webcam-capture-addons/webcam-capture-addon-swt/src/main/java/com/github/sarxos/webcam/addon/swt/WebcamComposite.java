package com.github.sarxos.webcam.addon.swt;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.WritableRaster;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.WebcamExceptionHandler;
import com.github.sarxos.webcam.WebcamListener;


public class WebcamComposite extends Composite implements WebcamListener, PaintListener {

	private static final class CompositeThreadFactory implements ThreadFactory {

		private static final AtomicInteger number = new AtomicInteger(0);

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, String.format("webcam-composite-scheduled-executor-%d", number.incrementAndGet()));
			t.setUncaughtExceptionHandler(WebcamExceptionHandler.getInstance());
			t.setDaemon(true);
			return t;
		}

	}

	/**
	 * Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(WebcamComposite.class);

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
	private static final ThreadFactory THREAD_FACTORY = new CompositeThreadFactory();

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

				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						redraw();
					}
				});

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

		private GC gc = null;

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

			if (!running.get()) {
				return;
			}

			if (!webcam.isOpen()) {
				return;
			}

			if (paused) {
				return;
			}

			BufferedImage bi = null;
			try {
				bi = webcam.getImage();
			} catch (Throwable t) {
				LOG.error("Exception when getting image", t);
			}

			if (bi == null) {
				LOG.debug("Image is null, ignore");
				return;
			}

			ComponentColorModel model = (ComponentColorModel) bi.getColorModel();
			PaletteData palette = new PaletteData(0x0000FF, 0x00FF00, 0xFF0000);
			ImageData data = new ImageData(bi.getWidth(), bi.getHeight(), model.getPixelSize(), palette);

			// this is valid because we are using a 3-byte data model without
			// transparent pixels
			data.transparentPixel = -1;

			WritableRaster raster = bi.getRaster();

			int[] rgb = new int[3];

			int x = 0;
			int y = 0;

			for (x = 0; x < data.width; x++) {
				for (y = 0; y < data.height; y++) {
					raster.getPixel(x, y, rgb);
					data.setPixel(x, y, palette.getPixel(new RGB(rgb[0], rgb[1], rgb[2])));
				}
			}

			Image previous = image;

			try {
				image = new Image(Display.getDefault(), data);
			} finally {
				if (previous != null) {
					previous.dispose();
				}
			}

			setBackgroundImage(image);

			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					if (image == null) {
						System.out.println("image is null");
						return;
					}

					redraw();
				}
			});
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
	private Image image = null;

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

	// /**
	// * Painter used to draw image in panel.
	// *
	// * @see #setPainter(Painter)
	// * @see #getPainter()
	// */
	// private Painter painter = new DefaultPainter();

	/**
	 * Preferred panel size.
	 */
	private Dimension size = null;

	public WebcamComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout(SWT.HORIZONTAL));
		setSize(640, 480);
		setVisible(true);
		setBackground(new Color(Display.getDefault(), new RGB(124, 23, 56)));
	}

	public void setWebcam(Webcam webcam) {
		if (webcam == null) {
			throw new IllegalArgumentException("Webcam cannot be null");
		}
		this.webcam = webcam;
	}

	@Override
	public void dispose() {
		if (image != null) {
			image.dispose();
		}
		super.dispose();
	}

	public static void main(String[] args) {

		Display display = Display.getDefault();
		Shell shell = new Shell(display);

		shell.setLayout(new FillLayout());
		shell.setText("Test");
		shell.setSize(640, 480);
		shell.setBackground(new Color(Display.getDefault(), new RGB(65, 120, 45)));

		WebcamComposite wc = new WebcamComposite(shell, SWT.EMBEDDED);
		wc.setWebcam(Webcam.getDefault());
		wc.start();

		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		System.out.println("done");

		wc.dispose();
		display.dispose();
	}

	@Override
	public void paintControl(PaintEvent e) {
		e.gc.drawImage(image, 0, 0);
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
			Dimension resolution = webcam.getViewSize();
			setSize(resolution.width, resolution.height);
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

			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					redraw();
				}
			});

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

			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					redraw();
				}
			});

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
	public double getFPS() {
		return frequency;
	}

	/**
	 * Set rendering frequency (in Hz or FPS). Minimum frequency is 0.016 (1
	 * frame per minute) and maximum is 25 (25 frames per second).
	 * 
	 * @param frequency the frequency
	 */
	public void setFPS(double frequency) {
		if (frequency > MAX_FREQUENCY) {
			frequency = MAX_FREQUENCY;
		}
		if (frequency < MIN_FREQUENCY) {
			frequency = MIN_FREQUENCY;
		}
		this.frequency = frequency;
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
}
