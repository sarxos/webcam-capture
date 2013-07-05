package com.github.sarxos.webcam.log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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

		ClassLoader cl = Thread.currentThread().getContextClassLoader();

		try {

			String[] names = {
				"ch.qos.logback.classic.LoggerContext",
				"ch.qos.logback.classic.joran.JoranConfigurator",
			};
			for (String name : names) {
				Class.forName(name, false, cl);
			}

			ch.qos.logback.classic.LoggerContext context = (ch.qos.logback.classic.LoggerContext) LoggerFactory.getILoggerFactory();
			ch.qos.logback.classic.joran.JoranConfigurator configurator = new ch.qos.logback.classic.joran.JoranConfigurator();
			configurator.setContext(context);
			context.reset();
			configurator.doConfigure(is);

		} catch (ClassNotFoundException e) {
			System.err.println("WLogC: Logback JARs are missing in classpath");
		} catch (NoClassDefFoundError e) {
			System.err.println("WLogC: Logback JARs are missing in classpath");
		} catch (Throwable e) {
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
