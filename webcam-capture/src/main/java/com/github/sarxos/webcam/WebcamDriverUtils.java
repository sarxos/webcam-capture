package com.github.sarxos.webcam;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WebcamDriverUtils {

	private static final Logger LOG = LoggerFactory.getLogger(WebcamDriverUtils.class);

	private WebcamDriverUtils() {
	}

	/**
	 * Find webcam driver. Scan packages to search drivers specified in the
	 * argument.
	 * 
	 * @param drivers array of driver names to search for
	 * @return Driver if found or throw exception
	 * @throw WebcamException
	 */
	protected static WebcamDriver findDriver(String[] drivers) {

		if (LOG.isInfoEnabled()) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < drivers.length; i++) {
				sb.append(drivers[i]).append(i < drivers.length - 1 ? ", " : "");
			}
			LOG.info("Searching for one of the webcam drivers [" + sb.toString() + "]");
		}

		for (String name : drivers) {

			String pkgname = "com.github.sarxos.webcam.ds." + name;

			WebcamDriver driver = null;
			Class<?>[] classes = WebcamDriverUtils.getClasses(pkgname, true);

			if (LOG.isDebugEnabled()) {
				LOG.debug("Searching for classes in " + pkgname + ", found " + classes.length);
			}

			if (classes.length == 0) {
				continue;
			}

			for (Class<?> clazz : classes) {
				if (WebcamDriver.class.isAssignableFrom(clazz)) {
					try {
						driver = (WebcamDriver) clazz.newInstance();
					} catch (InstantiationException e) {
						throw new RuntimeException(e);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
					break;
				}
			}

			LOG.info("Webcam driver has been found: " + name);

			return driver;
		}

		LOG.error("Webcam driver has not been found! Please add one to the classpath!");

		throw new WebcamException("No webcam driver available");
	}

	/**
	 * Scans all classes accessible from the context class loader which belong
	 * to the given package and subpackages.
	 * 
	 * @param packageName The base package
	 * @param flat scan only one package level, do not dive into subdirectories
	 * @return The classes
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	protected static Class<?>[] getClasses(String pkgname, boolean flat) {

		List<File> dirs = new ArrayList<File>();
		List<Class<?>> classes = new ArrayList<Class<?>>();

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		String path = pkgname.replace('.', '/');

		Enumeration<URL> resources = null;
		try {
			resources = classLoader.getResources(path);
		} catch (IOException e) {
			throw new RuntimeException("Cannot read path " + path, e);
		}

		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}

		for (File directory : dirs) {
			try {
				classes.addAll(findClasses(directory, pkgname, flat));
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Class not found", e);
			}
		}

		return classes.toArray(new Class<?>[classes.size()]);
	}

	/**
	 * Recursive method used to find all classes in a given directory and
	 * subdirectories.
	 * 
	 * @param dir base directory
	 * @param pkgname package name for classes found inside the base directory
	 * @param flat scan only one package level, do not dive into subdirectories
	 * @return Classes list
	 * @throws ClassNotFoundException
	 */
	private static List<Class<?>> findClasses(File dir, String pkgname, boolean flat) throws ClassNotFoundException {

		List<Class<?>> classes = new ArrayList<Class<?>>();
		if (!dir.exists()) {
			return classes;
		}

		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory() && !flat) {
				classes.addAll(findClasses(file, pkgname + "." + file.getName(), flat));
			} else if (file.getName().endsWith(".class")) {
				classes.add(Class.forName(pkgname + '.' + file.getName().substring(0, file.getName().length() - 6)));
			}
		}

		return classes;
	}
}
