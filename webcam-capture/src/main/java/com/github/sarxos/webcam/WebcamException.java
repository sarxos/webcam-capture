package com.github.sarxos.webcam;

public class WebcamException extends RuntimeException {

	private static final long serialVersionUID = 4305046981807594375L;

	public WebcamException(String message) {
		super(message);
	}

	public WebcamException(String message, Throwable cause) {
		super(message, cause);
	}

	public WebcamException(Throwable cause) {
		super(cause);
	}

}
