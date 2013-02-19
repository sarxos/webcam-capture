package com.github.sarxos.webcam;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;


/**
 * Deallocator which goal is to release all devices resources when SIGTERM
 * signal is detected.
 * 
 * @author Bartosz Firyn (SarXos)
 */
final class WebcamDeallocator {

	private static final WebcamSignalHandler HANDLER = new WebcamSignalHandler();

	private final Webcam[] webcams;

	/**
	 * This constructor is used internally to create new deallocator for the
	 * given devices array.
	 * 
	 * @param devices the devices to be stored in deallocator
	 */
	private WebcamDeallocator(Webcam[] devices) {
		this.webcams = devices;
	}

	/**
	 * Store devices to be deallocated when TERM signal has been received.
	 * 
	 * @param webcams the webcams array to be stored in deallocator
	 */
	protected static void store(Webcam[] webcams) {
		if (HANDLER.get() == null) {
			HANDLER.set(new WebcamDeallocator(webcams));
		} else {
			throw new IllegalStateException("Deallocator is already set!");
		}
	}

	protected static void unstore() {
		HANDLER.reset();
	}

	protected void deallocate() {
		for (Webcam w : webcams) {
			try {
				w.dispose();
			} catch (Throwable t) {
				caugh(t);
			}
		}
	}

	private void caugh(Throwable t) {
		File f = new File(String.format("webcam-capture-hs-%s", System.currentTimeMillis()));
		PrintStream ps = null;
		try {
			t.printStackTrace(ps = new PrintStream(f));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (ps != null) {
				ps.close();
			}
		}
	}

}
