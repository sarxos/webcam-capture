package com.github.sarxos.webcam;

public class WebcamLockException extends WebcamException {

	private static final long serialVersionUID = 1L;

	public WebcamLockException(String message, Throwable cause) {
		super(message, cause);
	}

	public WebcamLockException(String message) {
		super(message);
	}

	public WebcamLockException(Throwable cause) {
		super(cause);
	}
}
