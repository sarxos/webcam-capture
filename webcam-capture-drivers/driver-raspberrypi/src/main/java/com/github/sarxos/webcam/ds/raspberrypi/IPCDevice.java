package com.github.sarxos.webcam.ds.raspberrypi;

import static com.github.sarxos.webcam.ds.raspberrypi.RaspiThreadGroup.threadGroup;
import static com.github.sarxos.webcam.ds.raspberrypi.RaspiThreadGroup.threadId;

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamResolution;

/**
 * ClassName: IPCDevice <br/>
 * date: Jan 31, 2019 10:50:46 AM <br/>
 * interactive process communication abstraction. This class is designed to
 * reduce the number of methods that must be implemented by subclasses.
 * 
 * the process IO management and lifecycle are prepared in this process.
 * https://www.raspberrypi.org/documentation/raspbian/applications/camera.md
 * 
 * @author maoanapex88@163.com (alexmao86)
 * @version
 * @since JDK 1.8
 */
public abstract class IPCDevice implements WebcamDevice, WebcamDevice.Configurable, WebcamDevice.BufferAccess, Constants {
	private final static Logger LOGGER = LoggerFactory.getLogger(IPCDevice.class);
	/**
	 * raspi keypress mode, send new line to make capture
	 */
	protected final static char CAPTRE_TRIGGER_INPUT = '\n';
	protected final static char CAPTRE_TERMINTE_INPUT = 'x';

	private static final String THREAD_NAME_PREFIX = "raspistill-device-";
	private static final int DEFAULT_THREADPOOL_SIZE = 2;
	
	protected int width = 320;
	protected int height = 240;
	
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
	protected final IPCDriver driver;

	public IPCDevice(int camSelect, Map<String, String> parameters, IPCDriver driver) {
		super();
		this.camSelect = camSelect;
		this.parameters = parameters;
		this.driver = driver;
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
		this.parameters.put(OPT_WIDTH, (int) dimension.getWidth() + "");
		this.parameters.put(OPT_HEIGHT, (int) dimension.getHeight() + "");
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
				String longKey = this.driver.getOptions().getOption(entry.getKey()).getLongOpt();
				this.parameters.put(longKey, entry.getValue() == null ? "" : entry.getValue().toString());
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

		service = newExecutorService();
		validateParameters();
		try {
			process = launch();
		} catch (IOException e) {
			LOGGER.error(e.toString(), e);
			return;
		}

		out = process.getOutputStream();
		in = process.getInputStream();
		err = process.getErrorStream();

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
		parameters.remove(OPT_VERBOSE);
		parameters.remove(OPT_DEMO);
	}

	/*
	 * thread content to consume stderr inputstream
	 */
	protected Runnable newErrorConsumeWorker() {
		return new ErrorConsumeWorker();
	}

	protected ExecutorService newExecutorService() {
		return Executors.newFixedThreadPool(DEFAULT_THREADPOOL_SIZE, (Runnable r) -> {
			Thread thread = new Thread(threadGroup(), r, THREAD_NAME_PREFIX + threadId());
			thread.setPriority(7);// high priority to acquire CPU
			return thread;
		});
	}

	/*
	 * @throws IOException
	 * 
	 */
	private Process launch() throws IOException {
		StringBuilder command = new StringBuilder(12 + (parameters.size() << 3));
		command.append(this.driver.getCommand()).append(" ");
		for (Entry<String, String> entry : parameters.entrySet()) {
			command.append("--").append(entry.getKey()).append(" ");
			if (entry.getValue() != null) {
				command.append(entry.getValue()).append(" ");
			}
		}

		String commandString = command.toString();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(commandString);
		}
		return Runtime.getRuntime().exec(commandString);
	}
	
	protected synchronized final void readFully(byte[] buffer) throws IOException {
		for(int i=0;i<buffer.length;i++) {
			buffer[i]=(byte)in.read();
		}
	}

	@Override
	public ByteBuffer getImageBytes() {
		byte[] bytes = new byte[width * height * 3];// must new each time!
		try {
			readFully(bytes);
		} catch (IOException e) {
			LOGGER.error("can not access camera", e);
			throw new RuntimeException(e);
		}
		return ByteBuffer.wrap(bytes);
	}

	@Override
	public void getImageBytes(ByteBuffer buffer) {
		buffer.put(getImageBytes().array());
	}

	class ErrorConsumeWorker implements Runnable {
		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					LOGGER.info(e.toString(), e);
					break;
				}
				try {
					int ret = -1;
					do {
						ret = err.read();
					} while (ret != -1);
				} catch (IOException e) {
					LOGGER.info(e.toString(), e);
				}
			}
		}
	}
}
