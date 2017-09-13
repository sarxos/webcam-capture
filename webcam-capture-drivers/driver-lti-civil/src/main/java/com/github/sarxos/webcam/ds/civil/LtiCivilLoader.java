package com.github.sarxos.webcam.ds.civil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LtiCivilLoader {

	private static final Logger LOG = LoggerFactory.getLogger(LtiCivilLoader.class);

	/**
	 * Will be called on JVM shutdown.
	 * 
	 * @author Bartosz Firyn (SarXos)
	 */
	private static class Deleter extends Thread {

		private File file = null;

		public Deleter(File file) {
			super("lti-civil-binary-deleter");
			this.file = file;
		}

		@Override
		public void run() {
			super.run();
			if (file.exists()) {
				if (!file.delete()) {
					LOG.warn("JVM was not able to remove file {}", file);
				}
			}
		}
	}

	private LtiCivilLoader() {
		// singleton
	}

	/**
	 * Copy bytes from a large (over 2GB) InputStream to an OutputStream.
	 * 
	 * @param input the InputStream to read from
	 * @param output the OutputStream to write to
	 * @return the number of bytes copied
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException if an I/O error occurs
	 */
	private static long copy(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[1024 * 4];
		long count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	protected static void load(String lib) {
		LOG.info("Loading native library: {}", lib);
		try {
			System.loadLibrary(lib);
			LOG.info("DLL has been loaded from memory: {}", lib);
		} catch (UnsatisfiedLinkError e) {
			try {
				load("civil-" + System.currentTimeMillis(), lib);
			} catch (Exception e2) {
				LOG.error("Exception when loading DLL library", e2);
				throw new RuntimeException(e2);
			}
		}
	}

	protected static void load(String path, String name) {

		String libroot = "/META-INF/lib";
		String libpath = null;
		String libfile = null;

		boolean arch64 = System.getProperty("os.arch").indexOf("64") != -1;
		boolean linux = System.getProperty("os.name").toLowerCase().indexOf("linux") != -1;

		if (linux) {
			libpath = libroot + (arch64 ? "/linux64/" : "/linux32/");
			libfile = "lib" + name + ".so";
		} else {
			libpath = libroot + (arch64 ? "/win64/" : "/win32/");
			libfile = name + ".dll";
		}

		File parent = new File(System.getProperty("java.io.tmpdir") + "/" + path);
		if (!parent.exists()) {
			if (!parent.mkdirs()) {
				throw new RuntimeException("Cannot create directory: " + parent.getAbsolutePath());
			}
		}

		File file = new File(parent, libfile);
		if (!file.exists()) {

			boolean created = false;
			try {
				created = file.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException("Not able to create file: " + file, e);
			}
			if (!created) {
				throw new RuntimeException("File cannot be created: " + file);
			}

			Runtime.getRuntime().addShutdownHook(new Deleter(file));
		}

		String resource = libpath + libfile;

		LOG.debug("Library resource in JAR is {}", resource);

		InputStream in = LtiCivilDriver.class.getResourceAsStream(resource);
		if (in == null) {
			throw new RuntimeException("Resource not found: " + resource);
		}

		FileOutputStream fos = null;

		try {
			fos = new FileOutputStream(file);
			copy(in, fos);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("File not found " + file, e);
		} catch (IOException e) {
			throw new RuntimeException("IO exception", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					throw new RuntimeException("Cannot close input stream", e);
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					throw new RuntimeException("Cannot close file output stream", e);
				}
			}
		}

		LOG.debug("Loading library from file {}", file);

		try {
			System.load(file.getAbsolutePath());
		} catch (UnsatisfiedLinkError e) {
			throw new RuntimeException("Library file cannot be loaded: " + file, e);
		}
	}
}
