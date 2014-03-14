package com.github.sarxos.webcam.ds.fswebcam;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamExceptionHandler;
import com.github.sarxos.webcam.WebcamResolution;


public class FsWebcamDevice implements WebcamDevice, WebcamDevice.BufferAccess {

	public static final class ExecutorThreadFactory implements ThreadFactory {

		private final AtomicInteger number = new AtomicInteger(0);

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setName(String.format("process-reader-%d", number.incrementAndGet()));
			t.setUncaughtExceptionHandler(WebcamExceptionHandler.getInstance());
			t.setDaemon(true);
			return t;
		}
	}

	public static final class StreamReader implements Runnable {

		private final BufferedReader br;
		private final boolean err;

		public StreamReader(InputStream is, boolean err) {
			LOG.debug("New stream reader");
			this.br = new BufferedReader(new InputStreamReader(is));
			this.err = err;
		}

		@Override
		public void run() {
			try {
				try {
					String line;
					while ((line = br.readLine()) != null) {
						LOG.debug("FsWebcam: {} {}", err ? "ERROR" : "", line);
					}
				} catch (IOException e) {
					LOG.debug(String.format("Exception when reading %s output", err ? "STDERR" : "stdout"), e);
				}
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					LOG.error("Exception when closing buffered reader", e);
				}
			}
		}
	}

	private static final Logger LOG = LoggerFactory.getLogger(FsWebcamDevice.class);
	private static final Runtime RT = Runtime.getRuntime();
	private static final ExecutorThreadFactory THREAD_FACTORY = new ExecutorThreadFactory();
	private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(THREAD_FACTORY);

	private static final Dimension[] RESOLUTIONS = new Dimension[] {
		WebcamResolution.QQVGA.getSize(),
		WebcamResolution.QVGA.getSize(),
		WebcamResolution.VGA.getSize(),
	};

	private final File vfile;
	private final String name;

	private Dimension resolution = null;
	private Process process = null;
	private File pipe = null;
	private ByteArrayOutputStream baos = new ByteArrayOutputStream();
	private DataInputStream dis = null;

	private AtomicBoolean open = new AtomicBoolean(false);
	private AtomicBoolean disposed = new AtomicBoolean(false);

	protected FsWebcamDevice(File vfile) {
		this.vfile = vfile;
		this.name = vfile.getAbsolutePath();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Dimension[] getResolutions() {
		return RESOLUTIONS;
	}

	@Override
	public Dimension getResolution() {
		if (resolution == null) {
			resolution = getResolutions()[0];
		}
		return resolution;
	}

	private String getResolutionString() {
		Dimension d = getResolution();
		return String.format("%dx%d", d.width, d.height);
	}

	@Override
	public void setResolution(Dimension resolution) {
		this.resolution = resolution;
	}

	private synchronized byte[] readBytes() {

		if (!open.get()) {
			return null;
		}

		baos.reset();

		int b, c;
		try {

			// search for SOI
			while (true) {
				if ((b = dis.readUnsignedByte()) == 0xFF) {
					if ((c = dis.readUnsignedByte()) == 0xD8) {
						baos.write(b);
						baos.write(c);
						break; // SOI found
					}
				}
			}

			// read until EOI
			do {
				baos.write(c = dis.readUnsignedByte());
				if (c == 0xFF) {
					baos.write(c = dis.readUnsignedByte());
					if (c == 0xD9) {
						break; // EOI found
					}
				}
			} while (true);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return baos.toByteArray();

	}

	@Override
	public synchronized ByteBuffer getImageBytes() {
		if (!open.get()) {
			return null;
		}
		return ByteBuffer.wrap(readBytes());
	}

	@Override
	public BufferedImage getImage() {

		if (!open.get()) {
			return null;
		}

		//@formatter:off
		String[] cmd = new String[] { 
			"/usr/bin/fswebcam", 
			"--no-banner",                      // only image - no texts, banners, etc
			"--no-shadow",
			"--no-title",
			"--no-subtitle",
			"--no-timestamp",
			"--no-info",
			"--no-underlay",
			"--no-overlay",
			"-d", vfile.getAbsolutePath(),      // input video file
			"-r", getResolutionString(),        // resolution
			pipe.getAbsolutePath(),             // output file (pipe)
			LOG.isDebugEnabled() ? "-v" : "",   // enable verbosity if debug mode is enabled
		};
		//@formatter:on

		if (LOG.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder();
			for (String c : cmd) {
				sb.append(c).append(' ');
			}
			LOG.debug("Invoking command: {}", sb.toString());
		}

		BufferedImage image = null;

		try {

			process = RT.exec(cmd);

			// print process output
			EXECUTOR.execute(new StreamReader(process.getInputStream(), false));
			EXECUTOR.execute(new StreamReader(process.getErrorStream(), true));

			try {
				dis = new DataInputStream(new FileInputStream(pipe));
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}

			ByteArrayInputStream bais = new ByteArrayInputStream(readBytes());
			try {
				image = ImageIO.read(bais);
			} catch (IOException e) {
				process.destroy();
				throw new RuntimeException(e);
			} finally {
				try {
					bais.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			process.waitFor();

		} catch (IOException e) {
			LOG.error("Process IO exception", e);
		} catch (InterruptedException e) {
			process.destroy();
		} finally {

			try {
				dis.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			// w/a for bug in java 1.6 - waitFor requires Thread.interrupted()
			// call in finally block to reset thread flags

			if (Thread.interrupted()) {
				throw new RuntimeException("Thread has been interrupted");
			}
		}

		return image;
	}

	@Override
	public synchronized void open() {

		if (disposed.get()) {
			return;
		}

		if (!open.compareAndSet(false, true)) {
			return;
		}

		pipe = new File("/tmp/fswebcam-pipe-" + vfile.getName() + ".mjpeg");

		if (pipe.exists()) {
			if (!pipe.delete()) {
				throw new RuntimeException("Cannot remove streaming pipe " + pipe);
			}
		}

		LOG.debug("Creating pipe: mkfifo {}", pipe.getAbsolutePath());

		Process p = null;
		try {
			p = RT.exec(new String[] { "mkfifo", pipe.getAbsolutePath() });

			EXECUTOR.execute(new StreamReader(p.getInputStream(), false));
			EXECUTOR.execute(new StreamReader(p.getErrorStream(), true));

			p.waitFor();

		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			return;
		} finally {
			p.destroy();
		}
	}

	@Override
	public synchronized void close() {

		if (!open.compareAndSet(true, false)) {
			return;
		}

		if (dis != null) {
			try {
				dis.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		if (process != null) {
			process.destroy();
		}

		try {
			process.waitFor();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		if (!pipe.delete()) {
			pipe.deleteOnExit();
		}
	}

	@Override
	public void dispose() {
		if (disposed.compareAndSet(false, true) && open.get()) {
			close();
		}
	}

	@Override
	public boolean isOpen() {
		return open.get();
	}

	@Override
	public String toString() {
		return "video device " + name;
	}
}
