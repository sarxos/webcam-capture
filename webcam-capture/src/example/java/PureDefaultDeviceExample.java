

import org.bridj.Pointer;

import com.github.sarxos.webcam.ds.buildin.natives.Device;
import com.github.sarxos.webcam.ds.buildin.natives.DeviceList;
import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;


public class PureDefaultDeviceExample {

	public static void main(String[] args) {

		/**
		 * This example show how to use native OpenIMAJ API to capture raw bytes
		 * data as byte[] array. It also calculates current FPS.
		 */

		OpenIMAJGrabber grabber = new OpenIMAJGrabber();

		Device device = null;
		Pointer<DeviceList> devices = grabber.getVideoDevices();
		for (Device d : devices.get().asArrayList()) {
			device = d;
			break;
		}

		boolean started = grabber.startSession(320, 240, 30, Pointer.pointerTo(device));
		if (!started) {
			throw new RuntimeException("Not able to start native grabber!");
		}

		long t1 = System.currentTimeMillis();

		int n = 1000;
		int i = 0;
		do {
			grabber.nextFrame();
			grabber.getImage().getBytes(320 * 240 * 3); // byte[]
		} while (++i < n);

		long t2 = System.currentTimeMillis();

		System.out.println("Capturing time: " + (t2 - t1));

		grabber.stopSession();
	}
}
