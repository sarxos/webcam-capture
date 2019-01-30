package com.github.sarxos.webcam.ds.raspistill;

import static com.github.sarxos.webcam.ds.raspistill.AppThreadGroup.threadGroup;
import static com.github.sarxos.webcam.ds.raspistill.AppThreadGroup.threadId;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamResolution;

/**
 * 
 * ClassName: RaspistillDevice <br/>
 * Function: device descriptor of raspistil <br/>
 * date: Jan 23, 2019 9:57:03 AM <br/>
 * 
 * @author maoanapex88@163.com (alexmao86)
 */
class RaspistillDevice implements WebcamDevice, WebcamDevice.Configurable, Constants {

	private static final String THREAD_NAME_PREFIX = "raspistill-device-";
	private static final int THREAD_POOL_SIZE = 4;
	private final static Logger LOGGER = LoggerFactory.getLogger(RaspistillDevice.class);
	/**
	 * Artificial view sizes. raspistill can handle flex dimensions less than QSXGA,
	 * if the dimension is too high, respberrypi CPU can not afford the computing
	 */
	private final static Dimension[] DIMENSIONS = new Dimension[] { WebcamResolution.QQVGA.getSize(),
			WebcamResolution.HQVGA.getSize(), WebcamResolution.QVGA.getSize(), WebcamResolution.WQVGA.getSize(),
			WebcamResolution.HVGA.getSize(), WebcamResolution.VGA.getSize(), WebcamResolution.WVGA.getSize(),
			WebcamResolution.FWVGA.getSize(), WebcamResolution.SVGA.getSize(), WebcamResolution.DVGA.getSize(),
			WebcamResolution.WSVGA1.getSize(), WebcamResolution.WSVGA2.getSize(), WebcamResolution.XGA.getSize(),
			WebcamResolution.XGAP.getSize(), WebcamResolution.WXGA1.getSize(), WebcamResolution.WXGAP.getSize(),
			WebcamResolution.SXGA.getSize() };
	/**
	 * raspistill keypress mode, send new line to make capture
	 */
	private final static char CAPTRE_TRIGGER_INPUT = '\n';
	private final static char CAPTRE_TERMINTE_INPUT = 'x';

	private final int cameraSelect;

	private byte r = (byte) (Math.random() * Byte.MAX_VALUE);
	private byte g = (byte) (Math.random() * Byte.MAX_VALUE);
	private byte b = (byte) (Math.random() * Byte.MAX_VALUE);
	private Map<String, String> arguments;
	private volatile boolean isOpen = false;
	private Dimension dimension;
	private ExecutorService service;
	private Process process;
	private OutputStream out;
	private InputStream in;
	private InputStream err;
	private Queue<BufferedImage> frameBuffer = new CircularListCache<>(2);// 2 double buffer

	private final Options options;

	/**
	 * Creates a new instance of RaspistillDevice.
	 * 
	 * @param cameraSelect
	 *            the camera selection, default is zero
	 * @param arguments
	 *            the whole process argument list
	 */
	RaspistillDevice(final int cameraSelect, final Options options, final Map<String, String> arguments) {
		this.cameraSelect = cameraSelect;
		this.options = options;
		this.arguments = arguments;
	}

	@Override
	public void close() {
		if (!isOpen) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.warn(MSG_NOT_RUNNING_WARN);
			}
			return;
		}

		AtomicInteger counter = new AtomicInteger(5);

		service.shutdownNow();
		counter.getAndDecrement();

		try {
			out.write(CAPTRE_TERMINTE_INPUT);
			out.flush();
			out.close();
			counter.getAndDecrement();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			this.in.close();
			counter.getAndDecrement();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			err.close();
			counter.getAndDecrement();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			process.destroy();
			counter.getAndDecrement();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (counter.get() != 0) {
			LOGGER.debug(MSG_NOT_GRACEFUL_DOWN);
		}
		frameBuffer.clear();
		isOpen = false;
	}

	@Override
	public void dispose() {
		arguments = null;
		service = null;
		process = null;
	}

	@Override
	public BufferedImage getImage() {
		return frameBuffer.peek();
	}

	@Override
	public String getName() {
		return DEVICE_NAME_PREFIX + this.cameraSelect;
	}

	@Override
	public Dimension getResolution() {
		if (dimension == null) {
			dimension = new Dimension(Integer.parseInt(arguments.get(OPT_WIDTH)),
					Integer.parseInt(arguments.get(OPT_HEIGHT)));
		}
		return dimension;
	}

	@Override
	public Dimension[] getResolutions() {
		return DIMENSIONS;
	}

	@Override
	public boolean isOpen() {
		return isOpen;
	}

	/**
	 * start raspistill process to open devices
	 * <ul>
	 * <li>step 1: check given arguments will introduce native windows. keep
	 * raspistill run in quietly</li>
	 * 
	 * <li>step 2: create thread fixed size pool(size=2), one for read, one for
	 * write</li>
	 * 
	 * <li>step 3: override some illegal parameters</li>
	 * 
	 * <li>step 4: start process and begin communication and consume IO</li>
	 * </ul>
	 * 
	 * @see com.github.sarxos.webcam.WebcamDevice#open()
	 */
	@Override
	public void open() {
		if (isOpen()) {
			return;
		}

		// add one loading image
		BufferedImage loadingImage = this.drawLoadingImage();
		frameBuffer.add(loadingImage);

		service = Executors.newFixedThreadPool(THREAD_POOL_SIZE, (Runnable r) -> {
			Thread thread = new Thread(threadGroup(), r, THREAD_NAME_PREFIX + threadId());
			thread.setPriority(7);// high priority to acquire CPU
			return thread;
		});
		/*
		 * I interchanged the order of "-t 0" and "-s" and tested it. Code: Select all
		 * raspistill -t 0 -s -o test.jpg And signal driven event works now as expected!
		 * Y E A H :D
		 */
		// no preview window,
		arguments.remove(OPT_PREVIEW);
		arguments.remove(OPT_FULLSCREEN);
		arguments.remove(OPT_OPACITY);
		arguments.remove(OPT_HELP);
		arguments.remove(OPT_SETTINGS);

		// override some arguments
		arguments.put(OPT_ENCODING, "png");
		arguments.put(OPT_NOPREVIEW, "");
		arguments.put(OPT_CAMSELECT, Integer.toString(this.cameraSelect));
		arguments.put(OPT_OUTPUT, "-");// must be this, then image will be in console!
		try {
			process = launch();
		} catch (IOException e) {
			LOGGER.error(e.toString(), e);
			e.printStackTrace();
		}

		out = process.getOutputStream();
		in = process.getInputStream();
		err = process.getErrorStream();

		// send new line char to output to trigger capture stream by mocked fps
		service.submit(new CaptureWorker());
		// error must be consumed, if not, too much data blocking will crash process or
		// blocking IO
		service.submit(new ErrorConsumeWorker());

		isOpen = true;
	}

	/**
	 * @throws IOException
	 * 
	 */
	private Process launch() throws IOException {
		StringBuilder command = new StringBuilder(12 + arguments.size() * 8);
		command.append(COMMAND_CAPTURE).append(" ");
		for (Entry<String, String> entry : this.arguments.entrySet()) {
			command.append("--").append(entry.getKey()).append(" ");
			if (entry.getValue() != null) {
				command.append(entry.getValue()).append(" ");
			}
		}

		String commandString = command.toString();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(commandString);
		}
		StringTokenizer st = new StringTokenizer(commandString);

		String[] cmdarray = new String[st.countTokens()];
		for (int i = 0; st.hasMoreTokens(); i++)
			cmdarray[i] = st.nextToken();

		return new ProcessBuilder(cmdarray).directory(new File(".")).redirectErrorStream(true).start();
	}

	@Override
	public void setResolution(Dimension dimension) {
		this.dimension = dimension;
		this.arguments.put(OPT_WIDTH, dimension.getWidth() + "");
		this.arguments.put(OPT_HEIGHT, dimension.getHeight() + "");
	}
	
	/**
	 * support change FPS at runtime.
	 */
	@Override
	public void setParameters(Map<String, ?> map) {
		if (isOpen) {
			throw new UnsupportedOperationException(MSG_CANNOT_CHANGE_PROP);
		}

		for (Entry<String, ?> entry : map.entrySet()) {
			if (options.hasOption(entry.getKey())) {
				this.arguments.put(entry.getKey(), entry.getValue().toString());
			} else {
				throw new UnsupportedOperationException(MSG_WRONG_ARGUMENT);
			}
		}
	}

	private void drawRect(Graphics2D g2, int w, int h) {

		int rx = (int) (w * Math.random() / 1.5);
		int ry = (int) (h * Math.random() / 1.5);
		int rw = (int) (w * Math.random() / 1.5);
		int rh = (int) (w * Math.random() / 1.5);

		g2.setColor(new Color((int) (Integer.MAX_VALUE * Math.random())));
		g2.fillRect(rx, ry, rw, rh);
	}

	/**
	 * draw dummy wating images
	 * 
	 * @return
	 */
	private BufferedImage drawLoadingImage() {
		Dimension resolution = getResolution();

		int w = resolution.width;
		int h = resolution.height;

		String s = getName();

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
		BufferedImage bi = gc.createCompatibleImage(w, h);

		Graphics2D g2 = ge.createGraphics(bi);
		g2.setBackground(new Color(Math.abs(r++), Math.abs(g++), Math.abs(b++)));
		g2.clearRect(0, 0, w, h);

		drawRect(g2, w, h);
		drawRect(g2, w, h);
		drawRect(g2, w, h);
		drawRect(g2, w, h);
		drawRect(g2, w, h);

		Font font = new Font("sans-serif", Font.BOLD, 16);

		g2.setFont(font);

		FontMetrics metrics = g2.getFontMetrics(font);
		int sw = (w - metrics.stringWidth(s)) / 2;
		int sh = (h - metrics.getHeight()) / 2 + metrics.getHeight() / 2;

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(Color.BLACK);
		g2.drawString(s, sw + 1, sh + 1);
		g2.setColor(Color.WHITE);
		g2.drawString(s, sw, sh);

		g2.dispose();
		bi.flush();

		return bi;
	}

	// inner classes
	class CaptureWorker implements Runnable {
		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					out.write(CAPTRE_TRIGGER_INPUT);
					out.flush();

					BufferedImage frame = new PNGDecoder(in).decode();
					in.skip(16);// each time of decode, there will be 16 bytes should be skipped
					frameBuffer.add(frame);
				} catch (IOException e) {
					LOGGER.error(e.toString(), e);
				}
			}
		}
	}

	class ErrorConsumeWorker implements Runnable {
		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					LOGGER.warn(e.toString(), e);
					break;
				}
				try {
					int ret = -1;
					StringBuilder builder = new StringBuilder();
					do {
						ret = err.read();
						if (ret != -1) {
							builder.append((char) ret);
						}
					} while (ret != -1);
				} catch (IOException e) {
					LOGGER.error(e.toString(), e);
				}
			}
		}
	}
}
