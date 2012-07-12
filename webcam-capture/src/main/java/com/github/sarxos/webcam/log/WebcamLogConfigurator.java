package com.github.sarxos.webcam.log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;


/**
 * Configure loggers.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class WebcamLogConfigurator {

	/**
	 * Logger instance.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(WebcamLogConfigurator.class);

	/**
	 * Configure SLF4J.
	 * 
	 * @param is input stream to logback configuration xml
	 */
	public static void configure(InputStream is) {

		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(context);
		context.reset();

		try {
			configurator.doConfigure(is);
		} catch (JoranException e) {
			LOG.error("Joran configuration exception", e);
			e.printStackTrace();
		}
	}

	/**
	 * Configure SLF4J.
	 * 
	 * @param file logback configuration file
	 */
	public static void configure(File file) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			configure(fis);
		} catch (FileNotFoundException e) {
			LOG.error("File not found " + file, e);
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					LOG.error("Cannot close file " + file, e);
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Configure SLF4J.
	 * 
	 * @param file logback configuration file path
	 */
	public static void configure(String file) {
		configure(new File(file));
	}
}
