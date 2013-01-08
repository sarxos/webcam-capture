package com.github.sarxos.webcam;

import java.util.Observable;
import java.util.Observer;

import sun.misc.Signal;
import sun.misc.SignalHandler;


/**
 * Primitive signal handler. This class is using undocumented classes from
 * sun.misc.* and therefore should be used with caution.
 * 
 * @author Bartosz Firyn (SarXos)
 */
@SuppressWarnings("restriction")
class WebcamSignalHandler extends Observable implements SignalHandler {

	private SignalHandler handler = null;

	public void listen(String signal, Observer observer) throws IllegalArgumentException {
		addObserver(observer);
		handler = Signal.handle(new Signal(signal), this);
	}

	@Override
	public void handle(Signal signal) {

		// do nothing on "signal default" or "signal ignore"
		if (handler == SIG_DFL || handler == SIG_IGN) {
			return;
		}

		setChanged();

		try {
			notifyObservers(signal);
		} finally {
			handler.handle(signal);
		}
	}
}
