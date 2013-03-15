package com.github.sarxos.webcam.ds.buildin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.bridj.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDiscoverySupport;
import com.github.sarxos.webcam.WebcamDriver;
import com.github.sarxos.webcam.WebcamTask;
import com.github.sarxos.webcam.ds.buildin.natives.Device;
import com.github.sarxos.webcam.ds.buildin.natives.DeviceList;
import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;


/**
 * Default build-in webcam driver based on natives from OpenIMAJ framework. It
 * can be widely used on various systems - Mac OS, Linux (x86, x64, ARM),
 * Windows (win32, win64).
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class WebcamDefaultDriver implements WebcamDriver, WebcamDiscoverySupport {

	static {
		if (!"true".equals(System.getProperty("webcam.debug"))) {
			System.setProperty("bridj.quiet", "true");
		}
	}

	private static class WebcamNewGrabberTask extends WebcamTask {

		private AtomicReference<OpenIMAJGrabber> grabber = new AtomicReference<OpenIMAJGrabber>();

		public WebcamNewGrabberTask(WebcamDriver driver) {
			super(driver, null);
		}

		public OpenIMAJGrabber newGrabber() {
			try {
				process();
			} catch (InterruptedException e) {
				LOG.error("Processor has been interrupted");
				return null;
			}
			return grabber.get();
		}

		@Override
		protected void handle() {
			grabber.set(new OpenIMAJGrabber());
		}
	}

	private static class GetDevicesTask extends WebcamTask {

		private volatile List<WebcamDevice> devices = null;
		private volatile OpenIMAJGrabber grabber = null;

		public GetDevicesTask(WebcamDriver driver) {
			super(driver, null);
		}

		/**
		 * Return camera devices.
		 * 
		 * @param grabber the native grabber to use for search
		 * @return Camera devices.
		 */
		public List<WebcamDevice> getDevices(OpenIMAJGrabber grabber) {

			this.grabber = grabber;

			try {
				process();
			} catch (InterruptedException e) {
				LOG.error("Processor has been interrupted");
				return Collections.emptyList();
			}

			return devices;
		}

		@Override
		protected void handle() {

			devices = new ArrayList<WebcamDevice>();

			Pointer<DeviceList> pointer = grabber.getVideoDevices();
			DeviceList list = pointer.get();

			for (Device device : list.asArrayList()) {
				devices.add(new WebcamDefaultDevice(device));
			}
		}
	}

	/**
	 * Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(WebcamDefaultDriver.class);

	private static OpenIMAJGrabber grabber = null;

	@Override
	public List<WebcamDevice> getDevices() {

		LOG.debug("Searching devices");

		if (grabber == null) {

			WebcamNewGrabberTask task = new WebcamNewGrabberTask(this);
			grabber = task.newGrabber();

			if (grabber == null) {
				return Collections.emptyList();
			}
		}

		List<WebcamDevice> devices = new GetDevicesTask(this).getDevices(grabber);

		if (LOG.isDebugEnabled()) {
			for (WebcamDevice device : devices) {
				LOG.debug("Found device {}", device.getName());
			}
		}

		return devices;
	}

	@Override
	public long getScanInterval() {
		return 3000;
	}

	@Override
	public boolean isScanPossible() {
		return true;
	}

	@Override
	public boolean isThreadSafe() {
		return false;
	}
}
