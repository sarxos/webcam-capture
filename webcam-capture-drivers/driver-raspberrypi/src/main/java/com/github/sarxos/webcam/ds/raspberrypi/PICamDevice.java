package com.github.sarxos.webcam.ds.raspberrypi;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamResolution;

import uk.co.caprica.picam.ByteArrayPictureCaptureHandler;
import uk.co.caprica.picam.Camera;
import uk.co.caprica.picam.CameraConfiguration;
import uk.co.caprica.picam.CameraException;
import uk.co.caprica.picam.enums.Encoding;
import uk.co.caprica.picam.enums.ExposureMode;

public class PICamDevice implements WebcamDevice, WebcamDevice.Configurable, WebcamDevice.BufferAccess {
	private final static Logger LOGGER=LoggerFactory.getLogger(PICamDevice.class);
	private CameraConfiguration cameraConfiguration = CameraConfiguration.cameraConfiguration().quality(85).encoding(Encoding.JPEG).width(640).height(480);
	private Integer cameraIndex = 0;
	private Camera camera;
	
	private volatile boolean opened = false;
	private ByteArrayPictureCaptureHandler pictureCaptureHandler;
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
	
	public PICamDevice(int cameraIndex) {
		super();
		this.cameraIndex = cameraIndex;
	}
	
	@Override
	public ByteBuffer getImageBytes() {
		try {
			pictureCaptureHandler.begin();
			camera.takePicture(pictureCaptureHandler);
			pictureCaptureHandler.end();
			return ByteBuffer.wrap(pictureCaptureHandler.result());
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}

	@Override
	public void getImageBytes(ByteBuffer buffer) {
		try {
			pictureCaptureHandler.begin();
			camera.takePicture(pictureCaptureHandler);
			pictureCaptureHandler.end();
			buffer.put(pictureCaptureHandler.result());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setParameters(Map<String, ?> params) {
		if (opened) {
			throw new java.lang.UnsupportedOperationException("You cannot change parameter after camera opened");
		}
		for (Entry<String, ?> entry : params.entrySet()) {
			if("width".equalsIgnoreCase(entry.getKey())){
				cameraConfiguration.width((Integer)entry.getValue());
			}
			else if("height".equalsIgnoreCase(entry.getKey())){
				cameraConfiguration.height((Integer)entry.getValue());
			}
			else if("quality".equalsIgnoreCase(entry.getKey())){
				cameraConfiguration.quality((Integer)entry.getValue());
			}
			else if("brightness".equalsIgnoreCase(entry.getKey())){
				cameraConfiguration.brightness((Integer)entry.getValue());
			}
			else if("contrast".equalsIgnoreCase(entry.getKey())){
				cameraConfiguration.contrast((Integer)entry.getValue());
			}
			else if("saturation".equalsIgnoreCase(entry.getKey())){
				cameraConfiguration.saturation((Integer)entry.getValue());
			}
			else if("sharpness".equalsIgnoreCase(entry.getKey())){
				cameraConfiguration.sharpness((Integer)entry.getValue());
			}
			else if("shutterSpeed".equalsIgnoreCase(entry.getKey())){
				cameraConfiguration.shutterSpeed((Integer)entry.getValue());
			}
			else if("iso".equalsIgnoreCase(entry.getKey())){
				cameraConfiguration.iso((Integer)entry.getValue());
			}
			else if("exposureCompensation".equalsIgnoreCase(entry.getKey())){
				cameraConfiguration.exposureCompensation((Integer)entry.getValue());
			}
			else if("encoding".equalsIgnoreCase(entry.getKey())){
				cameraConfiguration.encoding(Encoding.valueOf(entry.getValue().toString()));
			}
			else if("vdieoStabilsation".equalsIgnoreCase(entry.getKey())){
				cameraConfiguration.vdieoStabilsation((Boolean)entry.getValue());
			}
			else if("exposureMode".equalsIgnoreCase(entry.getKey())){
				cameraConfiguration.exposureMode(ExposureMode.valueOf(entry.getValue().toString()));
			}
			/*else if("stereoscopicMode".equalsIgnoreCase(entry.getKey())){
				cameraConfiguration.stereoscopicMode(StereoscopicMode.valueOf(entry.getValue().toString()));
			}*/
			else {
				LOGGER.error("unsupported param {}->{}", entry.getKey(), entry.getValue());
			}
		}
	}

	@Override
	public void close() {
		if(opened){
			camera.close();
		}
	}

	@Override
	public void dispose() {
		LOGGER.debug("dispose of picam do nothing");
	}

	@Override
	public BufferedImage getImage() {
		try {
			pictureCaptureHandler.begin();
			camera.takePicture(pictureCaptureHandler);
			InputStream in=new ByteArrayInputStream(pictureCaptureHandler.result());
			pictureCaptureHandler.end();
			BufferedImage img=ImageIO.read(in);
			in.close();
			return img;
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}

	@Override
	public String getName() {
		return "PICAM "+this.cameraIndex;
	}

	@Override
	public Dimension getResolution() {
		return new Dimension(this.cameraConfiguration.width(), this.cameraConfiguration.height());
	}

	@Override
	public Dimension[] getResolutions() {
		//TODO
		LOGGER.warn("getResolutions does not support very well, return a fixed list of dims");
		return DIMENSIONS;
	}

	@Override
	public boolean isOpen() {
		return opened;
	}

	@Override
	public void open() {
		if(opened){
			return ;
		}
		try {
			camera = new Camera(this.cameraConfiguration);
			opened=true;
			pictureCaptureHandler=new ByteArrayPictureCaptureHandler(this.cameraConfiguration.width()*this.cameraConfiguration.height()*3);
		} catch (CameraException e) {
			e.printStackTrace();
			LOGGER.error("Open PICAM failed", e);
		}
	}

	@Override
	public void setResolution(Dimension dim) {
		if (opened) {
			throw new java.lang.UnsupportedOperationException("You cannot change parameter after camera opened");
		}
		cameraConfiguration.width(dim.width);
		cameraConfiguration.height(dim.height);
	}

}
