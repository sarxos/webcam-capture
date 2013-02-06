package com.github.sarxos.webcam;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;


public class WebcamProcessor {

	/**
	 * Thread factory for processor.
	 * 
	 * @author Bartosz Firyn (SarXos)
	 */
	private static final class ProcessorThreadFactory implements ThreadFactory {

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, "atomic-processor");
			t.setDaemon(true);
			return t;
		}
	}

	/**
	 * Heart of overall processing system. This class process all native calls
	 * wrapped in tasks, by doing this all tasks executions are
	 * super-synchronized.
	 * 
	 * @author Bartosz Firyn (SarXos)
	 */
	private static final class AtomicProcessor implements Runnable {

		private SynchronousQueue<WebcamTask> inbound = new SynchronousQueue<WebcamTask>(true);
		private SynchronousQueue<WebcamTask> outbound = new SynchronousQueue<WebcamTask>(true);

		private volatile boolean running = true;

		/**
		 * Process task.
		 * 
		 * @param task the task to be processed
		 * @return Processed task
		 * @throws InterruptedException when thread has been interrupted
		 */
		public WebcamTask process(WebcamTask task) throws InterruptedException {
			inbound.put(task);
			return outbound.take();
		}

		public void stop() {
			running = false;
		}

		@Override
		public void run() {
			running = true;
			while (running) {
				WebcamTask t = null;
				try {
					(t = inbound.take()).handle();
				} catch (InterruptedException e) {
					return;
				} finally {
					try {
						if (t != null) {
							outbound.put(t);
						}
					} catch (InterruptedException e) {
						return;
					}
				}
			}
		}
	}

	/**
	 * Is processor started?
	 */
	private static final AtomicBoolean started = new AtomicBoolean(false);

	/**
	 * Execution service.
	 */
	private static final ExecutorService runner = Executors.newSingleThreadExecutor(new ProcessorThreadFactory());

	/**
	 * Static processor.
	 */
	private static final AtomicProcessor processor = new AtomicProcessor();

	public WebcamProcessor() {
		if (started.compareAndSet(false, true)) {
			runner.execute(processor);
		}
	}

	/**
	 * Process single webcam task.
	 * 
	 * @param task the task to be processed
	 */
	public void process(WebcamTask task) {
		try {
			processor.process(task);
		} catch (InterruptedException e) {
			throw new WebcamException("Processing interrupted", e);
		}
	}

	/**
	 * Stop processing thread.
	 */
	public void shutdown() {
		processor.stop();
		started.set(false);
	}
}
