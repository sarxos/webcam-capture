package com.github.sarxos.webcam;

public interface WebcamDiscoveryListener {

	void webcamFound(WebcamDiscoveryEvent event);

	void webcamGone(WebcamDiscoveryEvent event);

}
