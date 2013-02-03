package com.github.sarxos.webcam.ds.buildin;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.ds.buildin.cgt.NewGrabberTask;
import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;


/**
 * This class is to ensure that all <i>native</i> calls will be executed by a
 * single, well synchronized thread. The problem with grabber which is being
 * used to perform native calls is the fact it is completely not ready to be
 * used in well-written concurrent application (such as Swing for example).
 * 
 * @author Bartosz Firyn (SarXos)
 */
public final class WebcamGrabberProcessor {

	/**
	 * Thread factory for processor.
	 * 
	 * @author Bartosz Firyn (SarXos)
	 */
	private static final class ProcessorThreadFactory implements ThreadFactory {

		private static int number = 0;

		@Override
		public Thread newThread(Runnable r) {

			// should always be 1, but add some unique name for debugging
			// purpose just in case if there is some bug in my understanding

			Thread t = new Thread(r, "processor-" + (++number));
			t.setDaemon(true);

			return t;
		}
	}

	/**
	 * Heart of overall processing system. This class process all native calls
	 * wrapped in tasks.
	 * 
	 * @author Bartosz Firyn (SarXos)
	 */
	private static final class StaticProcessor implements Runnable {

		private class IO {

			public AtomicReference<WebcamGrabberTask> input = new AtomicReference<WebcamGrabberTask>();
			public AtomicReference<WebcamGrabberTask> output = new AtomicReference<WebcamGrabberTask>();

			public IO(WebcamGrabberTask task) {
				input.set(task);
			}
		}

		private final AtomicReference<IO> ioref = new AtomicReference<IO>();

		/**
		 * Process task.
		 * 
		 * @param task the task to be processed
		 * @return Processed task
		 * @throws InterruptedException when thread has been interrupted
		 */
		public WebcamGrabberTask process(WebcamGrabberTask task) throws InterruptedException {

			IO io = new IO(task);

			// submit task wrapped in IO pair
			synchronized (ioref) {
				while (!ioref.compareAndSet(null, io)) {
					ioref.wait();
				}
			}

			// obtain processed task
			synchronized (io.output) {
				while ((task = io.output.getAndSet(null)) == null) {
					io.output.wait();
				}
			}

			return task;
		}

		@Override
		public void run() {

			WebcamGrabberTask t = null;
			IO io = null;

			while (true) {

				if ((io = ioref.getAndSet(null)) != null) {

					synchronized (ioref) {
						ioref.notify();
					}

					(t = io.input.get()).handle();
					io.output.set(t);

					synchronized (io.output) {
						io.output.notify();
					}

				} else {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						throw new RuntimeException(e.getMessage(), e);
					}
				}
			}
		}
	}

	/**
	 * Execution service.
	 */
	private static final Executor runner = Executors.newSingleThreadExecutor(new ProcessorThreadFactory());

	/**
	 * Static processor.
	 */
	private static final StaticProcessor processor = new StaticProcessor();

	/**
	 * Is processor started?
	 */
	private static final AtomicBoolean started = new AtomicBoolean(false);

	/**
	 * Native grabber.
	 */
	private OpenIMAJGrabber grabber = null;

	/**
	 * Protected access so user is not able to create it.
	 */
	protected WebcamGrabberProcessor() {
		if (started.compareAndSet(false, true)) {
			runner.execute(processor);
		}
	}

	/**
	 * Create native grabber.
	 * 
	 * @return New grabber
	 */
	private OpenIMAJGrabber createGrabber() {
		NewGrabberTask ngt = new NewGrabberTask();
		try {
			return ((NewGrabberTask) processor.process(ngt)).getGrabber();
		} catch (InterruptedException e) {
			throw new WebcamException("Grabber creation interrupted", e);
		}
	}

	/**
	 * Process task.
	 * 
	 * @param task the task to be processed
	 */
	protected void process(WebcamGrabberTask task) {

		// construct grabber if not available yet

		if (grabber == null) {
			grabber = createGrabber();
		}

		// process task and wait for it to be completed

		task.setGrabber(grabber);

		try {
			processor.process(task);
		} catch (InterruptedException e) {
			throw new WebcamException("Processing interrupted", e);
		}
	}
}
