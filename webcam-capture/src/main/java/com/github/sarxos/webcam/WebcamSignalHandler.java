package com.github.sarxos.webcam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.Signal;
import sun.misc.SignalHandler;


/**
 * Primitive signal handler. This class is using undocumented classes from
 * sun.misc.* and therefore should be used with caution.
 * 
 * @author Bartosz Firyn (SarXos)
 */
@SuppressWarnings("restriction")
final class WebcamSignalHandler implements SignalHandler {

	private static final Logger LOG = LoggerFactory.getLogger(WebcamSignalHandler.class);

	private WebcamDeallocator deallocator = null;

	private SignalHandler handler = null;

	public WebcamSignalHandler() {
		handler = Signal.handle(new Signal("TERM"), this);
	}

	@Override
	public void handle(Signal signal) {

		LOG.warn("Detected signal {} {}, calling deallocator", signal.getName(), signal.getNumber());

		// do nothing on "signal default" or "signal ignore"
		if (handler == SIG_DFL || handler == SIG_IGN) {
			return;
		}

		try {
			deallocator.deallocate();
		} finally {
			handler.handle(signal);
		}
	}

	public void set(WebcamDeallocator deallocator) {
		this.deallocator = deallocator;
	}

	public WebcamDeallocator get() {
		return this.deallocator;
	}

	public void reset() {
		this.deallocator = null;
	}
}
