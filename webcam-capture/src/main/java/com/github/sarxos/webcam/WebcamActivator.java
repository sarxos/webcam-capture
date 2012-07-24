package com.github.sarxos.webcam;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Simple OSGi bundle activator.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class WebcamActivator implements BundleActivator {

	private static final Logger LOG = LoggerFactory.getLogger(WebcamActivator.class);

	@Override
	public void start(BundleContext context) throws Exception {
		LOG.info("Webcam bundle started");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		LOG.info("Webcam bundle stopped");
	}

}
