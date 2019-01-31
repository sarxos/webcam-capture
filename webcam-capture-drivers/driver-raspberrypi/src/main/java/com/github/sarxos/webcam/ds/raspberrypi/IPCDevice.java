/*
 * Copyright (c) 2017, Robert Bosch (Suzhou) All Rights Reserved.
 * This software is property of Robert Bosch (Suzhou). 
 * Unauthorized duplication and disclosure to third parties is prohibited.
 */
package com.github.sarxos.webcam.ds.raspberrypi;

import static com.github.sarxos.webcam.ds.raspberrypi.RaspiThreadGroup.threadGroup;
import static com.github.sarxos.webcam.ds.raspberrypi.RaspiThreadGroup.threadId;

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
import java.util.StringTokenizer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamResolution;

/** 
 * ClassName: IPCDevice <br/> 
 * date: Jan 31, 2019 10:50:46 AM <br/> 
 * interactive process communication abstraction. This class is designed to reduce the number of methods that must
 * be implemented by subclasses.
 * 
 * the process IO management and lifecycle are prepared in this process.
 * https://www.raspberrypi.org/documentation/raspbian/applications/camera.md
 * 
 * @author maoanapex88@163.com (alexmao86)
 * @version  
 * @since JDK 1.8
 */
public abstract class IPCDevice implements WebcamDevice, WebcamDevice.Configurable, Constants {
	private static final int POLL_TIMEOUT = 40;
	private static final String THREAD_NAME_PREFIX = "raspistill-device-";
	private static final int DEFAULT_THREADPOOL_SIZE = 2;
	private final static Logger LOGGER = LoggerFactory.getLogger(IPCDevice.class);
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
	
	protected final int camSelect;
	protected Map<String, String> parameters;
	
	private volatile boolean isOpen = false;
	private Dimension dimension;
	private ExecutorService service;
	
	protected Process process;
	protected OutputStream out;
	protected InputStream in;
	protected InputStream err;
	protected final BlockingQueue<BufferedImage> frameBuffer = new ArrayBlockingQueue<>(2);
	protected final IPCDriver driver;
	
	public IPCDevice(int camSelect, Map<String, String> parameters, IPCDriver driver) {
		super();
		this.camSelect = camSelect;
		this.parameters = parameters;
		this.driver=driver;
	}
	
	@Override
	public BufferedImage getImage() {
		try {
			return frameBuffer.poll(POLL_TIMEOUT, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			return null;
		}
	}
	@Override
	public String getName() {
		return DEVICE_NAME_PREFIX + this.camSelect;
	}
	@Override
	public boolean isOpen() {
		return isOpen;
	}
	@Override
	public Dimension getResolution() {
		if (dimension == null) {
			dimension = new Dimension(Integer.parseInt(parameters.get(OPT_WIDTH)),
					Integer.parseInt(parameters.get(OPT_HEIGHT)));
		}
		return dimension;
	}
	@Override
	public void setResolution(Dimension dimension) {
		this.dimension = dimension;
		this.parameters.put(OPT_WIDTH, dimension.getWidth() + "");
		this.parameters.put(OPT_HEIGHT, dimension.getHeight() + "");
	}
	@Override
	public Dimension[] getResolutions() {
		return DIMENSIONS;
	}
	@Override
	public void dispose() {
		parameters = null;
		service = null;
		process = null;
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
			if (this.driver.getOptions().hasOption(entry.getKey())) {
				String longKey=this.driver.getOptions().getOption(entry.getKey()).getLongOpt();
				this.parameters.put(longKey, entry.getValue()==null?"":entry.getValue().toString());
			} else {
				throw new UnsupportedOperationException(MSG_WRONG_ARGUMENT);
			}
		}
	}
	@Override
	public void close() {
		if (!isOpen) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.warn(MSG_NOT_RUNNING_WARN);
			}
			return;
		}
		beforeClose();
		AtomicInteger counter = new AtomicInteger(5);

		service.shutdownNow();
		counter.getAndDecrement();

		try {
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
		afterClose();
	}
	protected void afterClose() {
		
	}

	protected void beforeClose() {
		
	}

	/**
	 * start raspi??? process to open devices
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
		beforeOpen();
		// add one loading image
		BufferedImage loadingImage = this.drawLoadingImage();
		frameBuffer.add(loadingImage);

		service = newExecutorService();
		validateParameters();
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
		service.submit(newCaptureWorker());
		// error must be consumed, if not, too much data blocking will crash process or
		// blocking IO
		service.submit(newErrorConsumeWorker());

		isOpen = true;
		afterOpen();
	}

	protected void afterOpen() {
	}

	protected void beforeOpen() {
	}

	protected void validateParameters() {
		// no preview window,
		parameters.remove(OPT_PREVIEW);
		parameters.remove(OPT_FULLSCREEN);
		parameters.remove(OPT_OPACITY);
		parameters.remove(OPT_HELP);
		parameters.remove(OPT_SETTINGS);

	}
	
	/**
	 * thread content to consume stderr inputstream
	 */
	protected abstract Runnable newErrorConsumeWorker();
	/**
	 * image capture worker, it is designed to consume stdout and stdin
	 */
	protected abstract Runnable newCaptureWorker();

	protected ExecutorService newExecutorService() {
		return Executors.newFixedThreadPool(DEFAULT_THREADPOOL_SIZE, (Runnable r) -> {
			Thread thread = new Thread(threadGroup(), r, THREAD_NAME_PREFIX + threadId());
			thread.setPriority(7);// high priority to acquire CPU
			return thread;
		});
	}
	
	/**
	 * @throws IOException
	 * 
	 */
	private Process launch() throws IOException {
		StringBuilder command = new StringBuilder(12 + parameters.size() * 8);
		command.append(this.driver.getCommand()).append(" ");
		for (Entry<String, String> entry : this.parameters.entrySet()) {
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

		return new ProcessBuilder(cmdarray).directory(new File(".")).redirectErrorStream(false).start();
	}
	
	//********************************************************
	private byte r = (byte) (Math.random() * Byte.MAX_VALUE);
	private byte g = (byte) (Math.random() * Byte.MAX_VALUE);
	private byte b = (byte) (Math.random() * Byte.MAX_VALUE);
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
	private void drawRect(Graphics2D g2, int w, int h) {

		int rx = (int) (w * Math.random() / 1.5);
		int ry = (int) (h * Math.random() / 1.5);
		int rw = (int) (w * Math.random() / 1.5);
		int rh = (int) (w * Math.random() / 1.5);

		g2.setColor(new Color((int) (Integer.MAX_VALUE * Math.random())));
		g2.fillRect(rx, ry, rw, rh);
	}
}
