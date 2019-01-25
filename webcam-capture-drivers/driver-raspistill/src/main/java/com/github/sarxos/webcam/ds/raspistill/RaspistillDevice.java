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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;

/**
 * 
 * ClassName: RaspistillDevice <br/>
 * Function: device descriptor of raspistil <br/>
 * date: Jan 23, 2019 9:57:03 AM <br/>
 * 
 * @author maoanapex88@163.com (alexmao86)
 */
class RaspistillDevice implements WebcamDevice, WebcamDevice.FPSSource, WebcamDevice.Configurable {
	private final static Logger LOGGER = LoggerFactory.getLogger(RaspistillDevice.class);
	private final int cameraSelect;
	private Map<String, String> arguments;
	private volatile boolean isOpen = false;
	private Dimension dimension;
	private ScheduledExecutorService service;
	private Process process;
	private OutputStream out;
	private InputStream in;
	private InputStream err;
	private Queue<BufferedImage> frameBuffer = new CircularListCache<>(2);

	/**
	 * Creates a new instance of RaspistillDevice.
	 * 
	 * @param cameraSelect
	 *            the camera selection, default is zero
	 * @param arguments
	 *            the whole process argument list
	 */
	RaspistillDevice(int cameraSelect, Map<String, String> arguments) {
		this.cameraSelect = cameraSelect;
		this.arguments = arguments;
	}

	@Override
	public void close() {
		if (!isOpen) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.warn(Constants.MSG_NOT_RUNNING_WARN);
			}
			return;
		}
		AtomicInteger counter = new AtomicInteger(5);
		try {
			out.close();
			counter.getAndDecrement();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			in.close();
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

		service.shutdownNow();
		counter.getAndDecrement();

		if (counter.get() != 0) {
			LOGGER.debug(Constants.MSG_NOT_GRACEFUL_DOWN);
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
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Now only one simple name retrieved, it is not real camera hardware vendor's name.");
		}
		return "raspistill Camera " + this.cameraSelect;
	}

	@Override
	public Dimension getResolution() {
		if (dimension == null) {
			dimension = new Dimension(Integer.parseInt(arguments.get("width")),
					Integer.parseInt(arguments.get("height")));
		}
		return dimension;
	}

	@Override
	public Dimension[] getResolutions() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(
					"now return one dummy dimension array of current setting, heardware supported dimension will retrieve in next release");
		}
		return new Dimension[] { getResolution() };
	}

	@Override
	public boolean isOpen() {
		return isOpen;
	}

	/**
	 * start raspistill process to open devices step 1: check given arguments will
	 * introduce native windows. keep raspistill run in quietly step 2: create
	 * thread fixed size pool(size=2), one for read, one for write step 3: override
	 * some illegal parameters step 4: start process and begin communication and
	 * consume IO
	 * 
	 * @see com.github.sarxos.webcam.WebcamDevice#open()
	 */
	@Override
	public void open() {
		if (isOpen()) {
			LOGGER.warn("device already opened");
			return;
		}

		// add one loading image
		BufferedImage loadingImage = this.drawLoadingImage();
		frameBuffer.add(loadingImage);

		service = Executors.newScheduledThreadPool(2, (Runnable r) -> {
			Thread thread = new Thread(threadGroup(), r, "raspistill-device-" + threadId());
			return thread;
		});
		/*
		 * I interchanged the order of "-t 0" and "-s" and tested it. Code: Select all
		 * raspistill -t 0 -s -o test.jpg And signal driven event works now as expected!
		 * Y E A H :D
		 */
		// no preview window,
		arguments.remove("preview");
		arguments.remove("fullscreen");
		arguments.remove("opacity");
		arguments.remove("help");
		arguments.remove("set");

		// override some arguments
		arguments.put("nopreview", "");
		arguments.put("camselect", Integer.toString(this.cameraSelect));
		arguments.put("output", "-");// must be this, then image will be in console!
		try {
			process = launch();
		} catch (IOException e) {
			if (process != null) {
				try {
					process.destroy();
				} catch (Exception e1) {
					LOGGER.warn("there maybe resource link, please report to issue list");
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
		}

		out = process.getOutputStream();
		in = process.getInputStream();
		err = process.getErrorStream();

		// this is one dummy work content that flash the process output stream
		service.scheduleAtFixedRate(() -> {
			try {
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}, 5, 5, TimeUnit.SECONDS);

		// error must be consumed, if not, too much data blocking will crash process or
		// blocking IO
		service.scheduleAtFixedRate(() -> {
			try {
				int ret = -1;
				int counter = 0;
				StringBuilder builder = new StringBuilder();
				do {
					ret = err.read();
					counter++;
					if (ret != -1) {
						builder.append((char) ret);
					}
				} while (ret != -1);

				if (LOGGER.isDebugEnabled() && counter > 1) {
					LOGGER.debug("raspistill stderr {0}", builder.toString());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}, 1, 3, TimeUnit.SECONDS);
		// image stream
		service.submit(() -> {
			while (!Thread.currentThread().isInterrupted()) {
				// TODO read console to split images
			}
		});

		isOpen = true;
	}

	/**
	 * @throws IOException
	 * 
	 */
	private Process launch() throws IOException {
		StringBuilder command = new StringBuilder(12 + arguments.size() * 8);
		command.append("raspistill ");
		for (Entry<String, String> entry : this.arguments.entrySet()) {
			command.append("--").append(entry.getKey()).append(" ");
			if (entry.getValue() != null) {
				command.append(entry.getValue()).append(" ");
			}
		}
		if (command.length() == 0)
			throw new IllegalArgumentException("Empty command");

		String commandString = command.toString();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("final raspistill command: {}", commandString);
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
		this.arguments.put("width", dimension.getWidth() + "");
		this.arguments.put("height", dimension.getHeight() + "");
	}

	/**
	 * calculate pseudo fps
	 * 
	 * @see com.github.sarxos.webcam.WebcamDevice.FPSSource#getFPS()
	 */
	@Override
	public double getFPS() {
		int timelapse = Integer.parseInt(arguments.get("timelapse"));
		return 1000d / timelapse;
	}

	@Override
	public void setParameters(Map<String, ?> map) {
		for (Entry<String, ?> entry : map.entrySet()) {
			this.arguments.put(entry.getKey(), entry.getValue().toString());
		}
	}

	//
	private byte r = (byte) (Math.random() * Byte.MAX_VALUE);
	private byte g = (byte) (Math.random() * Byte.MAX_VALUE);
	private byte b = (byte) (Math.random() * Byte.MAX_VALUE);

	private void drawRect(Graphics2D g2, int w, int h) {

		int rx = (int) (w * Math.random() / 1.5);
		int ry = (int) (h * Math.random() / 1.5);
		int rw = (int) (w * Math.random() / 1.5);
		int rh = (int) (w * Math.random() / 1.5);

		g2.setColor(new Color((int) (Integer.MAX_VALUE * Math.random())));
		g2.fillRect(rx, ry, rw, rh);
	}

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
}
