package com.github.sarxos.webcam.ds.raspberrypi;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ClassName: RaspistillDevice <br/>
 * date: Jan 31, 2019 12:16:12 PM <br/>
 * 
 * @author maoanapex88@163.com alexmao86
 * @version
 * @since JDK 1.8
 */
public class RaspistillDevice extends IPCDevice {
	private final static Logger LOGGER = LoggerFactory.getLogger(RaspistillDevice.class);
	/**
	 * raspistill keypress mode, send new line to make capture
	 */
	private final static char CAPTRE_TRIGGER_INPUT = '\n';
	private final static char CAPTRE_TERMINTE_INPUT = 'x';

	/**
	 * Creates a new instance of RaspistillDevice.
	 * 
	 * @param camSelect
	 * @param parameters
	 * @param driver
	 */
	public RaspistillDevice(int camSelect, Map<String, String> parameters, IPCDriver driver) {
		super(camSelect, parameters, driver);
	}
	
	@Override
	protected void beforeClose() {
		try {
			out.write(CAPTRE_TERMINTE_INPUT);
			out.write(CAPTRE_TRIGGER_INPUT);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected Runnable newErrorConsumeWorker() {
		return new ErrorConsumeWorker();
	}
	
	@Override
	protected Runnable newCaptureWorker() {
		return new CaptureWorker();
	}
	/**
	 * One github story: I interchanged the order of "-t 0" and "-s" and tested it. Code: Select all
	 * raspistill -t 0 -s -o test.jpg And signal driven event works now as expected!
	 * Y E A H :D
	 */
	@Override
	protected void validateParameters() {
		super.validateParameters();
		
		// override some arguments
		parameters.put(OPT_ENCODING, "png");
		parameters.put(OPT_NOPREVIEW, "");
		parameters.put(OPT_CAMSELECT, Integer.toString(this.camSelect));
		parameters.put(OPT_OUTPUT, "-");// must be this, then image will be in console!
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
					frameBuffer.offer(frame);
				} catch (IOException e) {
					LOGGER.info(e.toString());
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
