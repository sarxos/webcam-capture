package com.sarxos.poc.webcam;

public interface WebcamListener {

	public void webcamOpen(WebcamEvent we);

	public void webcamClosed(WebcamEvent we);

}
