package com.github.sarxos.webcam;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Observable;
import java.util.Observer;


/**
 * Deallocator which goal is to release all devices resources when SIGTERM
 * signal is detected.
 * 
 * @author Bartosz Firyn (SarXos)
 */
class WebcamDeallocator implements Observer {

	private Webcam[] webcams = null;
	private WebcamSignalHandler handler = new WebcamSignalHandler();

	/**
	 * This constructor is used internally to create new deallocator for the
	 * given devices array.
	 * 
	 * @param devices the devices to be stored in deallocator
	 */
	private WebcamDeallocator(Webcam[] devices) {
		if (devices != null && devices.length > 0) {
			this.webcams = devices;
			this.handler.listen("TERM", this);
		}
	}

	/**
	 * Store devices to be deallocated when TERM signal has been received.
	 * 
	 * @param devices the devices array to be stored by deallocator
	 */
	protected static final void store(Webcam[] devices) {
		new WebcamDeallocator(devices);
	}

	@Override
	public void update(Observable observable, Object object) {
		for (Webcam device : webcams) {
			try {
				device.dispose();
			} catch (Throwable t) {
				caugh(t);
			}
		}
	}

	public void caugh(Throwable e) {
		File f = new File(String.format("webcam-capture-hs-%s", System.currentTimeMillis()));
		PrintStream ps = null;
		try {
			e.printStackTrace(ps = new PrintStream(f));
		} catch (FileNotFoundException e2) {
			// ignore, stdout is not working, cannot do anything more
		} finally {
			if (ps != null) {
				ps.close();
			}
		}
	}
}
