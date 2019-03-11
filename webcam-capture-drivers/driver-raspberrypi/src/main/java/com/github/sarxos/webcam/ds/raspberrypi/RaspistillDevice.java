package com.github.sarxos.webcam.ds.raspberrypi;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * ClassName: RaspistillDevice <br/>
 * date: Jan 31, 2019 12:16:12 PM <br/>
 * 
 * @author maoanapex88@163.com alexmao86
 * @version
 * @since JDK 1.8
 */
public class RaspistillDevice extends IPCDevice {
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

	/**
	 * One github story: I interchanged the order of "-t 0" and "-s" and tested it.
	 * Code: Select all raspistill -t 0 -s -o test.jpg And signal driven event works
	 * now as expected! Y E A H :D
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

	@Override
	public BufferedImage getImage() {
		try {
			BufferedImage frame = new PNGDecoder(in).decode();
			return frame;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public ByteBuffer getImageBytes() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void getImageBytes(ByteBuffer buffer) {
		throw new UnsupportedOperationException();
	}
}
