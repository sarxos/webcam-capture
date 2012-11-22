package com.github.sarxos.webcam.ds.buildin;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;


/**
 * This class is to ensure that all <i>native</i> calls will be executed by a
 * single, well synchronized thread. The problem with grabber which is being
 * used to perform native calls is the fact it is completely not ready to be
 * used in well-written concurrent application (such as Swing for example).
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class WebcamGrabberProcessor {

	/**
	 * Thread factory for processor.
	 * 
	 * @author Bartosz Firyn (SarXos)
	 */
	private static final class ProcessorThreadFactory implements ThreadFactory {

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, getClass().getSimpleName());
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

		@Override
		public void run() {
			while (true) {
				WebcamGrabberTask task = null;
				try {
					(task = tasks.take()).handle();
				} catch (InterruptedException e) {
					throw new WebcamException("Take interrupted", e);
				} finally {
					synchronized (task) {
						task.notifyAll();
					}
				}
			}
		}
	}

	/**
	 * Internal task used to create new grabber. Yeah, native grabber
	 * construction also has to be super-synchronized.
	 * 
	 * @author Bartosz Firyn (SarXos)
	 */
	private static final class NewGrabberTask extends WebcamGrabberTask {

		private volatile OpenIMAJGrabber grabber = null;

		public OpenIMAJGrabber getGrabber() {
			return grabber;
		}

		@Override
		protected void handle() {
			grabber = new OpenIMAJGrabber();
		}
	}

	/**
	 * Synchronous queue used to exchange tasks between threads and static
	 * processor.
	 */
	private static final SynchronousQueue<WebcamGrabberTask> tasks = new SynchronousQueue<WebcamGrabberTask>(true);

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
	 * Process task.
	 * 
	 * @param task the task to be processed
	 */
	protected void process(WebcamGrabberTask task) {

		// construct grabber if not available yet

		synchronized (this) {
			if (grabber == null) {
				NewGrabberTask grabberTask = new NewGrabberTask();
				tasks.offer(grabberTask);
				try {
					synchronized (grabberTask) {
						grabberTask.wait();
						grabber = grabberTask.getGrabber();
					}
				} catch (InterruptedException e) {
					throw new WebcamException("Grabber creation interrupted", e);
				}
			}
		}

		// run task and wait for it to be completed

		try {
			synchronized (task) {
				task.setGrabber(grabber);
				tasks.offer(task, 30, TimeUnit.MINUTES);
				task.wait();
			}
		} catch (InterruptedException e) {
			throw new WebcamException("Offer interrupted", e);
		}
	}
}
