package com.github.sarxos.webcam.ds.raspberrypi;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.Map;

/** 
 * ClassName: RaspividDevice <br/>  
 * --raw-format rgb is still yuv values, see https://www.raspberrypi.org/forums/viewtopic.php?t=189830
 * @author maoanapex88@163.com alexmao86 
 */
public class RaspividDevice extends IPCDevice {
	private static final int DATA_TYPE = DataBuffer.TYPE_BYTE;
	private static final ColorSpace COLOR_SPACE = ColorSpace.getInstance(ColorSpace.CS_sRGB);
	private static int[] OFFSET = new int[] { 0 };
	private static final int[] BITS = { 8, 8, 8 };
	private static int[] BAND_OFFSETS = new int[] { 0, 1, 2 };
	
	private int width=320;
	private int height=240;
	private final ColorModel cmodel = new ComponentColorModel(COLOR_SPACE, BITS, false, false, Transparency.OPAQUE, DATA_TYPE);
	private SampleModel smodel ;
	/** 
	 * Creates a new instance of RaspividDevice. 
	 * 
	 * @param camSelect
	 * @param parameters
	 * @param driver 
	 */ 
	public RaspividDevice(int camSelect, Map<String, String> parameters, IPCDriver driver) {
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
	protected void beforeOpen() {
		super.beforeOpen();
		this.width=Integer.parseInt(parameters.get(OPT_WIDTH));
		this.height=Integer.parseInt(parameters.get(OPT_HEIGHT));
		smodel = new ComponentSampleModel(DATA_TYPE, width, height, 3, width * 3, BAND_OFFSETS);
	}
	@Override
	protected void validateParameters() {
		super.validateParameters();
		// override some arguments
		parameters.put(OPT_RAW_FORMAT, "rgb");//only support rgb
		parameters.put(OPT_NOPREVIEW, "");
		parameters.put(OPT_CAMSELECT, Integer.toString(this.camSelect));
		parameters.put(OPT_RAW, "-");// must be this, then image will be in console!
		parameters.put(OPT_OUTPUT, "/tmp/cache.h264");
	}
	
	@Override
	public BufferedImage getImage() {
		byte[] bytes = new byte[width * height * 3];// must new each time!
		
		try {
			this.in.read(bytes);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		byte[][] data = new byte[][] { bytes };
		DataBufferByte dbuf = new DataBufferByte(data, bytes.length, OFFSET);
		WritableRaster raster = Raster.createWritableRaster(smodel, dbuf, null);
		
		BufferedImage bi = new BufferedImage(cmodel, raster, false, null);
		bi.flush();
		
		return bi;
	}
}
